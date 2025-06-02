package de.theidler.create_mobile_packages.entities.robo_entity;

import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.Location;
import de.theidler.create_mobile_packages.blocks.BeePortStorage;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.states.AdjustRotationToTarget;
import de.theidler.create_mobile_packages.entities.robo_entity.states.LandingFinishState;
import de.theidler.create_mobile_packages.entities.robo_entity.states.LaunchPrepareState;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import java.util.*;

public class RoboEntity extends Mob {

    private static final EntityDataAccessor<Float> ROT_YAW = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> PACKAGE_HEIGHT_SCALE = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.FLOAT);

    private RoboEntityState state;
    private Vec3 targetVelocity = Vec3.ZERO;
    private Player targetPlayer;
    private BeePortBlockEntity targetBlockEntity;
    private BeePortalBlockEntity targetPortalEntity = null;
    private BeePortBlockEntity startBeePortBlockEntity;
    private BeePortalBlockEntity startBeePortalBlockEntity = null;
    private String targetAddress = "";
    private String activeTargetAddress = "";
    private boolean multidimensional = false;
    private int damageCounter;

    /**
     * Constructor for RoboEntity. Used for spawning the entity.
     *
     * @param type      The entity type.
     * @param level     The level in which the entity exists.
     * @param itemStack The ItemStack (package) used to determine the target.
     * @param spawnPos  The spawn position of the entity.
     */
    public RoboEntity(EntityType<? extends Mob> type, Level level, ItemStack itemStack, Location target, BlockPos spawnPos) {
        super(type, level);
        damageCounter = 0;
        CreateMobilePackages.ROBO_MANAGER.addRobo(this);
        if (target != null) {
            if (target.level() != level) {
                targetPortalEntity = getClosestBeePortal(level, position(), target.level());
                targetBlockEntity = level.getBlockEntity(target.position()) instanceof BeePortBlockEntity dpbe
                        ? dpbe
                        : null;
            } else {
                targetBlockEntity = level.getBlockEntity(target.position()) instanceof BeePortBlockEntity dpbe
                        ? dpbe
                        : null;
            }
        }

        setItemStack(itemStack);
        setTargetFromItemStack(itemStack);
        setPos(spawnPos.getCenter().subtract(0, 0.5, 0));


        BlockEntity spawn = level().getBlockEntity(spawnPos);
        if (spawn instanceof BeePortBlockEntity dpbe) startBeePortBlockEntity = dpbe;
        else if (spawn instanceof BeePortalBlockEntity dpbe) {
            startBeePortalBlockEntity = dpbe;
            startBeePortalBlockEntity.tryAddToLaunchingQueue(this);
        }

        if (!level().isClientSide()) entityData.set(ROT_YAW, (float) getSnapAngle(getAngleToTarget()));

        // don't fly out of the port if target is origin
        if (targetBlockEntity != null && (targetBlockEntity.equals(startBeePortBlockEntity))) {
            setState(new LandingFinishState());
            return;
        }

        if (startBeePortBlockEntity == null && startBeePortalBlockEntity == null) {
            setState(new AdjustRotationToTarget());
            return;
        }

        setState(new LaunchPrepareState());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ROT_YAW, getYRot());
        entityData.define(DATA_ITEM_STACK, ItemStack.EMPTY);
        entityData.define(PACKAGE_HEIGHT_SCALE, 0.0f);
    }

    /**
     * Sets the target for the RoboEntity based on the provided ItemStack.
     * If the ItemStack is not a package, the target is set to the closest drone port.
     * If the ItemStack is a package, it attempts to find a player or drone port
     * matching the address specified in the package.
     *
     * @param itemStack The ItemStack used to determine the target.
     */
    private void setTargetFromItemStack(ItemStack itemStack) {
        if (itemStack == null) return;
        targetAddress = PackageItem.getAddress(itemStack);
        updateTarget();
    }

    private void setTargetPlayerFromAddress() {
        String address = PackageItem.getAddress(getItemStack());
        Player tempTargetPlayer = level().players().stream()
                .filter(player -> player.getName().getString().equals(address))
                .findFirst().orElse(null);

        if (tempTargetPlayer == null) {
            IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server == null)
                return; // TODO: Add support for multiplayer

            // Check if the item can be sent to a player.
            Iterable<ServerLevel> serverLevels = server.getAllLevels();
            for (ServerLevel serverLevel : serverLevels)
                for (Player player : serverLevel.players()) {
                    if (player.getName().getString().equals(address)
                            && RoboEntity.isWithinRange(player.position(), position())) {
                        setTargetPlayer(player);
                        return;
                    }
                }
        } else
            setTargetPlayer(tempTargetPlayer);
    }

    public void setTargetPlayer(Player targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    public void updateTarget() {
        if (level().isClientSide) return;
        setTargetPlayerFromAddress();
        targetPortalEntity = null;
        if (targetPlayer != null) {
            if (targetPlayer.level().dimensionType() != level().dimensionType()) {
                targetPortalEntity = getClosestBeePortal(level(), position(), targetPlayer.level());
                targetPortalEntity.tryAddToLandingQueue(this);
            }
            return;
        }

        if (targetBlockEntity == null || !targetBlockEntity.canAcceptEntity(this, !getItemStack().isEmpty()) || !Objects.equals(activeTargetAddress, targetAddress)) {
            BeePortBlockEntity oldTarget = targetBlockEntity;
            activeTargetAddress = targetAddress;
            Vec3 pos = position();
            List<BeePortBlockEntity> allBEs = getMultidimensionalBeePorts(level(), Objects.equals(targetAddress, "") ? null : targetAddress, pos, this);
            if (!allBEs.isEmpty()) targetBlockEntity = allBEs.get(0);

            if (oldTarget != targetBlockEntity) {
                if (oldTarget != null)
                    oldTarget.tryRemoveFromLandingQueue(this);
                if (targetBlockEntity != null)
                    targetBlockEntity.tryRemoveFromLandingQueue(this);
            }
        } else {
            if (targetBlockEntity.getLevel() != level())
                targetPortalEntity = getClosestBeePortal(level(), position(), targetBlockEntity.getLevel());
        }

        if (targetPortalEntity != null)
            targetPortalEntity.tryAddToLandingQueue(this);
        if (targetBlockEntity != null)
            targetBlockEntity.tryAddToLandingQueue(this);
    }

    /**
     * Gets the position of the current target.
     * If no target is set, it defaults to the closest drone port.
     *
     * @return The block position of the target.
     */
    public Location getTargetLocation() {
        updateTarget();
        multidimensional = false;
        if (targetPlayer != null) {
            if (targetPlayer.level().dimensionType() != level().dimensionType()) {
                if (targetPortalEntity == null) return null;
                multidimensional = true;
                if (targetPortalEntity.getLevel() != null && isWithinRange(targetPortalEntity.getBlockPos().getCenter(), position()))
                    return new Location(targetPortalEntity.getBlockPos().above().above(), targetPortalEntity.getLevel());
            } else if (isWithinRange(targetPlayer.position(), position()))
                return new Location(targetPlayer.blockPosition().above().above(), targetPlayer.level());
        } else if (targetBlockEntity != null) {
            Level targetLevel = targetBlockEntity.getLevel();
            if (targetLevel == null) return null;
            if (targetLevel != level()) {
                multidimensional = true;
                if (targetPortalEntity == null) return null;
                if (isWithinRange(targetPortalEntity.getBlockPos().getCenter(), position()))
                    return new Location(targetPortalEntity.getBlockPos().above().above(), targetPortalEntity.getLevel());
            } else if (isWithinRange(targetBlockEntity.getBlockPos().getCenter(), position()))
                return new Location(targetBlockEntity.getBlockPos().above().above(), targetLevel);
        }

        return null;
    }

    public static boolean isWithinRange(Vec3 targetPos, Vec3 originPos) {
        int maxDistance = CMPConfigs.server().beeMaxDistance.get();
        if (targetPos == null || originPos == null) return false;
        if (maxDistance == -1) return true;
        return targetPos.distanceToSqr(originPos) <= maxDistance * maxDistance;
    }

    /**
     * Finds the closest BeePortBlockEntity to this RoboEntity, optionally filtered by an address.
     * <p>
     * This method searches for all available BeePortBlockEntity instances in the current level.
     * If an address is provided, only ports matching the address filter are considered.
     * All full ports are removed from the selection.
     * Finally, the closest port to this RoboEntity's position is determined.
     *
     * @param address The address to filter by, or {@code null} for no filtering.
     * @return The closest BeePortBlockEntity that matches the filter criteria, or {@code null} if none found.
     */
    public static BeePortBlockEntity getClosestBeePort(Level level, String address, Vec3 originPos, @Nullable RoboEntity re) {
        return getClosestBeePort(level, address, originPos, re == null || !re.getItemStack().isEmpty() || !Objects.equals(re.activeTargetAddress, re.targetAddress));
    }

    /**
     * Finds the closest BeePortBlockEntity to this RoboEntity, optionally filtered by an address.
     * <p>
     * This method searches for all available BeePortBlockEntity instances in the current level.
     * If an address is provided, only ports matching the address filter are considered.
     * All full ports are removed from the selection.
     * Finally, the closest port to this RoboEntity's position is determined.
     *
     * @param address The address to filter by, or {@code null} for no filtering.
     * @return The closest BeePortBlockEntity that matches the filter criteria, or {@code null} if none found.
     */
    public static BeePortBlockEntity getClosestBeePort(Level level, String address, Vec3 originPos, boolean hasPackage) {
        BeePortBlockEntity closest = null;
        if (level instanceof ServerLevel serverLevel) {
            BeePortStorage storage = BeePortStorage.get(serverLevel);
            List<BeePortBlockEntity> allBEs = storage.getPorts();
            allBEs.removeIf(BE -> !isWithinRange(BE.getBlockPos().getCenter(), originPos));
            if (address != null)
                allBEs.removeIf(BE -> !PackageItem.matchAddress(address, BE.addressFilter));

            allBEs.removeIf(BE -> !BE.canAcceptEntity(null, hasPackage));
            closest = allBEs.stream()
                    .min(Comparator.comparingDouble(a -> a.getBlockPos().getCenter().distanceToSqr(originPos)))
                    .orElse(null);
        }

        return closest;
    }

    /**
     * Finds all BeePortBlockEntity instance in all dimensions, filtered by an address.
     * <p>
     * This method searches for all available BeePortBlockEntity instances in all levels.
     * All full ports are removed from the selection.
     * Finally, the closest port to this RoboEntity's position is determined.
     *
     * @param address The address to filter by, or {@code null} for no filtering.
     * @return The closest BeePortBlockEntity that matches the filter criteria, or {@code null} if none found.
     */
    public static List<BeePortBlockEntity> getMultidimensionalBeePorts(Level level, String address, Vec3 originPos, RoboEntity re) {
        BeePortBlockEntity closest = getClosestBeePort(level, address, originPos, re);
        List<BeePortBlockEntity> result = new ArrayList<>();
        if (closest != null) {
            result.add(closest);
            return result;
        }

        if (level instanceof ServerLevel) {
            MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server == null) return result;
            Iterable<ServerLevel> levels = server.getAllLevels();
            levels.forEach(beLevel -> {
                if (beLevel != level) result.add(getClosestBeePort(beLevel, address, originPos, re));
            });
        }

        result.removeIf(Objects::isNull);
        return result;
    }

    /**
     * Finds the closest BeePortalBlockEntity to this RoboEntity.
     * <p>
     * This method searches for all available BeePortalBlockEntity instances in the current level.
     * All full ports are removed from the selection.
     * Finally, the closest port to this RoboEntity's position is determined.
     *
     * @return The closest BeePortalBlockEntity.
     */
    public static BeePortalBlockEntity getClosestBeePortal(Level level, Vec3 originPos, Level targetLevel) {
        BeePortalBlockEntity closest = null;
        if (level instanceof ServerLevel serverLevel) {
            BeePortStorage storage = BeePortStorage.get(serverLevel);
            List<BeePortalBlockEntity> allBEs = storage.getPortals().stream()
                    .filter(be -> isWithinRange(be.getBlockPos().getCenter(), originPos))
                    .toList();
            closest = allBEs.stream()
                    .min(Comparator.comparingDouble(be -> be.getBlockPos().getCenter().distanceToSqr(originPos)))
                    .orElse(null);
        }

        return closest;
    }

    @Override
    public void tick() {
    }

    public void roboMangerTick() {
        super.tick();
        CreateMobilePackages.ROBO_MANAGER.markDirty();
        if (state != null) state.tick(this);
        setDeltaMovement(targetVelocity);
        move(MoverType.SELF, targetVelocity);
        float rotYaw = entityData.get(ROT_YAW);
        setYRot(rotYaw);
        setYHeadRot(rotYaw);
        yBodyRot = rotYaw;
        updateNametag();
    }

    private void updateNametag() {
        if (level().isClientSide) return;
        if (!CMPConfigs.server().displayNametag.get()) {
            setCustomName(null);
            setCustomNameVisible(false);
        } else if (targetAddress != null && !targetAddress.isBlank()) {
            setCustomName(Component.literal("-> " + targetAddress));
            setCustomNameVisible(true);
        } else if (targetBlockEntity != null) {
            BlockPos pos = targetBlockEntity.getBlockPos();
            setCustomName(Component.literal("-> [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"));
            setCustomNameVisible(true);
        } else {
            setCustomName(null);
            setCustomNameVisible(false);
        }
    }

    /**
     * Sets the current state of the RoboEntity.
     *
     * @param state The new state to set.
     */
    public void setState(RoboEntityState state) {
        if (state == null) return;
        this.state = state;
    }

    public ItemStack getItemStack() {
        return entityData.get(DATA_ITEM_STACK);
    }

    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null) return;
        entityData.set(DATA_ITEM_STACK, itemStack);
    }

    public Float getPackageHeightScale() {
        return entityData.get(PACKAGE_HEIGHT_SCALE);
    }

    public void setPackageHeightScale(float scale) {
        if (scale < 0.0f || scale > 1.0f) return;
        entityData.set(PACKAGE_HEIGHT_SCALE, scale);
    }

    public BeePortBlockEntity getStartBeePortBlockEntity() {
        return startBeePortBlockEntity;
    }

    public BeePortalBlockEntity getStartBeePortalBlockEntity() {
        return startBeePortalBlockEntity;
    }

    public void setTargetVelocity(Vec3 targetVelocity) {
        if (targetVelocity == null) return;
        this.targetVelocity = targetVelocity;
    }

    /**
     * Calculates the snap angle for a given angle. (45, 135, 225, 315)
     *
     * @param angle The angle to snap.
     * @return The snapped angle.
     */
    public int getSnapAngle(double angle) {
        return (int) Math.abs(Math.round(angle / 90) * 90 - 45);
    }

    /**
     * Calculates the angle to the current target.
     *
     * @return The angle to the target.
     */
    public double getAngleToTarget() {
        Location targetLocation = getTargetLocation();
        if (targetLocation == null || targetLocation.level() != level())
            return 0;
        return targetLocation.position() != null
                ? Math.atan2(targetLocation.position().getZ() - getZ(), targetLocation.position().getX() - getX())
                : 0;
    }

    @Override
    public void remove(@NotNull RemovalReason pReason) {
        if (getStartBeePortBlockEntity() != null)
            getStartBeePortBlockEntity().tryRemoveFromLaunchingQueue(this);
        if (getStartBeePortalBlockEntity() != null)
            getStartBeePortalBlockEntity().tryRemoveFromLaunchingQueue(this);
        if (getTargetBlockEntity() != null)
            getTargetBlockEntity().tryRemoveFromLandingQueue(this);
        if (getTargetPortalEntity() != null)
            getTargetPortalEntity().tryRemoveFromLandingQueue(this);
        if (getExitPortal() != null)
            getExitPortal().tryRemoveFromLaunchingQueue(this);

        if (pReason != RemovalReason.CHANGED_DIMENSION)
            handleItemStackOnRemove();

        super.remove(pReason);
    }

    private void handleItemStackOnRemove() {
        if (!getItemStack().isEmpty()) {
            level().addFreshEntity(PackageEntity.fromItemStack(level(), position(), getItemStack()));
            setItemStack(ItemStack.EMPTY);
            if (targetPlayer != null)
                targetPlayer.displayClientMessage(Component.translatable("create_mobile_packages.robo_entity.death", Math.round(getX()), Math.round(getY()), Math.round(getZ()), level().dimensionTypeId().location().getPath(), targetPlayer.getName().getString()), false);
        }
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }

    public BeePortBlockEntity getTargetBlockEntity() {
        return targetBlockEntity;
    }

    @Nullable
    public BeePortalBlockEntity getTargetPortalEntity() {
        return targetPortalEntity;
    }

    @Nullable
    public BeePortalBlockEntity getExitPortal() {
        BeePortalBlockEntity targetPortal = getTargetPortalEntity();
        Level targetLevel = targetPlayer != null
                ? targetPlayer.level()
                : targetBlockEntity != null ? targetBlockEntity.getLevel() : null;
        if (!multidimensional || targetPortal == null || targetLevel == null) return null;
        return getExitPortal(targetLevel, targetPortal.getBlockPos().getCenter());
    }

    @Nullable
    public static BeePortalBlockEntity getExitPortal(Level targetLevel, Vec3 originPos) {
        Vec3 position = targetLevel.dimension() == Level.END
                ? new Vec3(100, 49, 0)
                : originPos.multiply(1 / 8d, 1, 1 / 8d);
        return getClosestBeePortal(targetLevel, position, null);
    }

    /**
     * Updates the display message for the specified player with the estimated time of arrival.
     *
     * @param player The player to update.
     */
    public void updateDisplay(Player player) {
        if (player == null) return;
        if (multidimensional) {
            BeePortalBlockEntity exitPortal = getExitPortal();
            if (exitPortal == null) return;
            player.displayClientMessage(Component.translatable("create_mobile_packages.robo_entity.eta", calcETA(this, exitPortal.getBlockPos().getCenter())), true);
        } else
            player.displayClientMessage(Component.translatable("create_mobile_packages.robo_entity.eta", calcETA(player.position(), position())), true);
    }

    /**
     * Calculates the estimated time of arrival (ETA) to the specified targetPosition.
     *
     * @param targetPos The Vec3 to calculate the ETA for.
     * @return The ETA in seconds.
     */
    public static int calcETA(Vec3 targetPos, Vec3 currentPos) {
        double distance = targetPos.distanceTo(currentPos);
        return (int) (distance / CMPConfigs.server().beeSpeed.get()) + 1;
    }

    /**
     * Calculates the estimated time of arrival (ETA) to the specified targetPosition of a different dimension.
     *
     * @param re            The RoboEntity that transports the package.
     * @param exitPortalPos The position of the BeePortal to enter the target dimension.
     * @return The ETA in seconds.
     */
    public static int calcETA(RoboEntity re, Vec3 exitPortalPos) {
        Vec3 targetPos = null;
        BeePortBlockEntity targetBlock = re.getTargetBlockEntity();
        Player targetPlayer = re.getTargetPlayer();
        if (targetBlock != null)
            targetPos = targetBlock.getBlockPos().getCenter();
        else if (targetPlayer != null)
            targetPos = targetPlayer.position();

        BeePortalBlockEntity targetPortal = re.getTargetPortalEntity();
        if (targetPortal == null || targetPos == null)
            return Integer.MAX_VALUE;
        return calcETA(re.position(), targetPortal.getBlockPos().getCenter()) + calcETA(exitPortalPos, targetPos);
    }

    /**
     * Instantly rotates the RoboEntity to look at its target.
     */
    public void lookAtTarget() {
        if (level().isClientSide()) return;
        Location targetLocation = getTargetLocation();
        if (targetLocation == null || targetLocation.level() != level()) return;
        if (targetLocation.position() != null) {
            Vec3 direction = new Vec3(targetLocation.position().getX(), targetLocation.position().getY(), targetLocation.position().getZ()).subtract(position()).normalize();
            entityData.set(ROT_YAW, (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90);
        }
    }

    /**
     * Rotates the RoboEntity to face its target.
     *
     * @return The number of ticks required to complete the rotation.
     */
    public int rotateLookAtTarget() {
        return rotateToAngle((float) getAngleToTarget() + 90);
    }

    /**
     * Rotates the RoboEntity to the nearest snap angle.
     *
     * @return The number of ticks required to complete the rotation.
     */
    public int rotateToSnap() {
        return rotateToAngle((float) getSnapAngle(getAngleToTarget()) + 90);
    }

    /**
     * Rotates the RoboEntity to a specified yaw angle.
     *
     * @param targetYaw The target yaw angle.
     * @return The number of ticks required to complete the rotation.
     */
    private int rotateToAngle(float targetYaw) {
        if (level().isClientSide()) return -1;
        float currentYaw = entityData.get(ROT_YAW);
        float deltaYaw = targetYaw - currentYaw;
        deltaYaw = (deltaYaw > 180) ? deltaYaw - 360 : (deltaYaw < -180) ? deltaYaw + 360 : deltaYaw;
        float rotationSpeed = CMPConfigs.server().beeRotationSpeed.get();
        if (Math.abs(deltaYaw) > rotationSpeed)
            currentYaw += (deltaYaw > 0) ? rotationSpeed : -rotationSpeed;
        else
            currentYaw = targetYaw;

        entityData.set(ROT_YAW, currentYaw);
        return (int) Math.ceil(Math.abs(deltaYaw) / rotationSpeed);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (!getItemStack().isEmpty()) {
            nbt.put("itemStack", getItemStack().save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("itemStack", Tag.TAG_COMPOUND)) {
            setItemStack(ItemStack.of(nbt.getCompound("itemStack")));
        } else {
            setItemStack(ItemStack.EMPTY);
        }

        setTargetFromItemStack(getItemStack());
    }

    @Override
    public void load(@NotNull CompoundTag pCompound) {
        super.load(pCompound);
        if (pCompound.contains("itemStack"))
            setItemStack(ItemStack.of(pCompound.getCompound("itemStack")));

        setTargetFromItemStack(getItemStack());
        if (!level().isClientSide() && !getItemStack().isEmpty())
            setPackageHeightScale(1.0f);
    }

    public void setTargetAddress(String address) {
        targetAddress = address;
        updateTarget();
    }

    @Override
    public void kill() {
        level().broadcastEntityEvent(this, (byte) 60);
        if (level() instanceof ServerLevel serverLevel) {
            ItemStack drop = new ItemStack(CMPItems.ROBO_BEE.get());
            Containers.dropItemStack(serverLevel, getX(), getY(), getZ(), drop);
        }

        discard();
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 60) {
            for (int i = 0; i < 3; i++) {
                level().addParticle(ParticleTypes.POOF, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
            }
        } else {
            super.handleEntityEvent(pId);
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        if (isInvulnerableTo(pSource)) return false;

        if (!level().isClientSide && !isRemoved()) {
            markHurt();
            damageCounter += (int) (pAmount * 10);
            if (damageCounter > 40) {
                handleItemStackOnRemove();
                discard();
                kill();
            }
        }

        return true;
    }

    public boolean multidimensional() {
        return multidimensional;
    }
}
