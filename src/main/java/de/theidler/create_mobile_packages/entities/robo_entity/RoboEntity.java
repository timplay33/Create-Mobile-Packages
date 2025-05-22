package de.theidler.create_mobile_packages.entities.robo_entity;

import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_port.ModCapabilities;
import de.theidler.create_mobile_packages.entities.robo_entity.states.AdjustRotationToTarget;
import de.theidler.create_mobile_packages.entities.robo_entity.states.LandingDescendFinishState;
import de.theidler.create_mobile_packages.entities.robo_entity.states.LaunchPrepareState;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class RoboEntity extends Mob {

    private static final EntityDataAccessor<Float> ROT_YAW = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> PACKAGE_HEIGHT_SCALE = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.FLOAT);

    private RoboEntityState state;
    private Vec3 targetVelocity = Vec3.ZERO;
    private Player targetPlayer;
    private BeePortBlockEntity targetBlockEntity;
    private BeePortBlockEntity startBeePortBlockEntity;
    private String targetAddress = "";
    private boolean pathing = true;

    private final List<ChunkPos> loadedChunks = new ArrayList<>();
    private int damageCounter;

    /**
     * Constructor for RoboEntity. Used for spawning the entity.
     *
     * @param type      The entity type.
     * @param level     The level in which the entity exists.
     * @param itemStack The ItemStack (package) used to determine the target.
     * @param spawnPos  The spawn position of the entity.
     */
    public RoboEntity(EntityType<? extends Mob> type, Level level, ItemStack itemStack, BlockPos targetPos, BlockPos spawnPos) {
        super(type, level);
        this.damageCounter = 0;
        if (targetPos != null) {
            this.targetBlockEntity = level.getBlockEntity(targetPos) instanceof BeePortBlockEntity dpbe ? dpbe : null;
            if (this.targetBlockEntity != null) {
                setState(new LaunchPrepareState());
            }
        }
        setItemStack(itemStack);
        //createPackageEntity(itemStack);
        setTargetFromItemStack(itemStack);
        this.setPos(spawnPos.getCenter().subtract(0, 0.5, 0));
        if (targetBlockEntity != null) {
            targetBlockEntity.trySetEntityOnTravel(this);
        }
        if (level().getBlockEntity(spawnPos) instanceof BeePortBlockEntity dpbe) {
            startBeePortBlockEntity = dpbe;
        }
        if (!level().isClientSide()) {
            this.entityData.set(ROT_YAW, (float) getSnapAngle(getAngleToTarget()));
        }
        // don't fly out of the port if target is origin
        if (targetBlockEntity != null && targetBlockEntity.equals(startBeePortBlockEntity)) {
            setState(new LandingDescendFinishState());
            return;
        }
        if (startBeePortBlockEntity == null) {
            setState(new AdjustRotationToTarget());
            return;
        }
        setState(new LaunchPrepareState());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROT_YAW, getYRot());
        this.entityData.define(DATA_ITEM_STACK, ItemStack.EMPTY);
        this.entityData.define(PACKAGE_HEIGHT_SCALE, 0.0f);
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

    private Player getTargetPlayerFromAddress() {
        return level().players().stream()
                .filter(player -> player.getName().getString().equals(targetAddress))
                .findFirst().orElse(null);
    }

    private void updateTarget() {
        if (level().isClientSide) {
            return;
        }
        targetPlayer = getTargetPlayerFromAddress();
        if (targetPlayer != null) {
            return;
        }
        if (targetBlockEntity == null || !targetBlockEntity.canAcceptEntity(this)) {
            BeePortBlockEntity oldTarget = targetBlockEntity;
            targetBlockEntity = getClosestBeePort(level(), Objects.equals(targetAddress, "") ? null : targetAddress, this.blockPosition(), this);
            if (oldTarget != targetBlockEntity) {
                if (oldTarget != null) {
                    oldTarget.trySetEntityOnTravel(null);
                }
                if (targetBlockEntity != null) {
                    targetBlockEntity.trySetEntityOnTravel(this);
                }
            }
        }
    }

    /**
     * Gets the position of the current target.
     * If no target is set, it defaults to the closest drone port.
     *
     * @return The block position of the target.
     */
    public BlockPos getTargetPosition() {
        updateTarget();
        if (targetPlayer != null) {
            return isWithinRange(targetPlayer.blockPosition(), this.blockPosition()) ? targetPlayer.blockPosition().above().above() : null;
        }
        if (targetBlockEntity != null) {
            return isWithinRange(targetBlockEntity.getBlockPos(), this.blockPosition()) ? targetBlockEntity.getBlockPos().above().above() : null;
        }
        return null;
    }

    public static boolean isWithinRange(BlockPos targetPos, BlockPos originPos) {
        int maxDistance = CMPConfigs.server().beeMaxDistance.get();
        if (targetPos == null || originPos == null) return false;
        if (maxDistance == -1) return true;
        return targetPos.distSqr(originPos) <= maxDistance * maxDistance;
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
    public static BeePortBlockEntity getClosestBeePort(Level level, String address, BlockPos origin, RoboEntity entity) {
        final BeePortBlockEntity[] closest = {null};
        level.getCapability(ModCapabilities.BEE_PORT_ENTITY_TRACKER_CAP).ifPresent(tracker -> {
            List<BeePortBlockEntity> allBEs = new ArrayList<>(tracker.getAll());
            allBEs.removeIf(dpbe -> !isWithinRange(dpbe.getBlockPos(), origin));
            if (address != null) {
                allBEs.removeIf(dpbe -> !PackageItem.matchAddress(address, dpbe.addressFilter));
            }
            allBEs.removeIf(dpbe -> !dpbe.canAcceptEntity(entity));
            closest[0] = allBEs.stream()
                    .min(Comparator.comparingDouble(a -> a.getBlockPos().distSqr(origin)))
                    .orElse(null);
        });
        return closest[0];
    }

    @Override
    public void tick() {
        super.tick();
        tickEntity(level(), this.blockPosition(), this.getX(), this.getZ());
        state.tick(this);
        this.setDeltaMovement(targetVelocity);
        this.move(MoverType.SELF, targetVelocity);
        float rotYaw = this.entityData.get(ROT_YAW);
        this.setYRot(rotYaw);
        this.setYHeadRot(rotYaw);
        this.yBodyRot = rotYaw;
        updateNametag();
    }

    private void updateNametag() {
        if (level().isClientSide) return;
        if (!CMPConfigs.server().displayNametag.get() || targetAddress == null || targetAddress.isBlank()) {
            setCustomName(null);
            setCustomNameVisible(false);
        } else {
            setCustomName(Component.literal("-> " + targetAddress));
            setCustomNameVisible(true);
        }
    }

    public void tickEntity(Level world, BlockPos ownerPos, double x, double z) {
        if (!(world instanceof ServerLevel serverLevel) || ownerPos == null) return;

        ChunkPos currentChunk = new ChunkPos((int) x >> 4, (int) z >> 4);

        loadedChunks.removeIf(loadedChunk -> {
            boolean isOutsideCurrentArea = Math.abs(loadedChunk.x - currentChunk.x) > 1 || Math.abs(loadedChunk.z - currentChunk.z) > 1;
            if (isOutsideCurrentArea) {
                ForgeChunkManager.forceChunk(serverLevel, CreateMobilePackages.MODID, ownerPos, loadedChunk.x, loadedChunk.z, false, false);
                return true;
            }
            return false;
        });

        forceArea(serverLevel, ownerPos, currentChunk.x, currentChunk.z);
    }

    private void forceArea(ServerLevel world, BlockPos owner, int cx, int cz) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos chunkPos = new ChunkPos(cx + dx, cz + dz);
                if (loadedChunks.contains(chunkPos)) continue;
                loadedChunks.add(chunkPos);
                ForgeChunkManager.forceChunk(world, CreateMobilePackages.MODID, owner, chunkPos.x, chunkPos.z, true, true);
            }
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
        return this.entityData.get(DATA_ITEM_STACK);
    }

    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null) return;
        this.entityData.set(DATA_ITEM_STACK, itemStack);
    }

    public Float getPackageHeightScale() {
        return this.entityData.get(PACKAGE_HEIGHT_SCALE);
    }

    public void setPackageHeightScale(float scale) {
        if (scale < 0.0f || scale > 1.0f) return;
        this.entityData.set(PACKAGE_HEIGHT_SCALE, scale);
    }

    public BeePortBlockEntity getStartBeePortBlockEntity() {
        return startBeePortBlockEntity;
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
        BlockPos targetPos = getTargetPosition();
        return targetPos != null
                ? Math.atan2(targetPos.getZ() - this.getZ(), targetPos.getX() - this.getX())
                : 0;
    }

    @Override
    public void remove(RemovalReason pReason) {
        handleItemStackOnRemove();
        if (getTargetBlockEntity() != null) {
            getTargetBlockEntity().trySetEntityOnTravel(null);
        }

        // unload all chunks
        loadedChunks.forEach(chunkPos -> {
            if (level() instanceof ServerLevel serverLevel) {
                ForgeChunkManager.forceChunk(serverLevel, CreateMobilePackages.MODID, this.blockPosition(), chunkPos.x, chunkPos.z, false, false);
            }
        });
        super.remove(pReason);
    }

    private void handleItemStackOnRemove() {
        if (!this.getItemStack().isEmpty()) {
            level().addFreshEntity(PackageEntity.fromItemStack(level(), this.position(), getItemStack()));
            setItemStack(ItemStack.EMPTY);
            if (targetPlayer != null) {
                targetPlayer.displayClientMessage(Component.translatable("create_mobile_packages.robo_entity.death", Math.round(this.getX()), Math.round(this.getY()), Math.round(this.getZ()), targetPlayer.getName().getString()), false);
            }
        }
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }

    public BeePortBlockEntity getTargetBlockEntity() {
        return targetBlockEntity;
    }

    /**
     * Updates the display message for the specified player with the estimated time of arrival.
     *
     * @param player The player to update.
     */
    public void updateDisplay(Player player) {
        if (player == null) return;
        player.displayClientMessage(Component.translatable("create_mobile_packages.robo_entity.eta", calcETA(player.position(), this.position())), true);
    }

    /**
     * Calculates the estimated time of arrival (ETA) to the specified targetPosition.
     *
     * @param targetPosition The Vec3 to calculate the ETA for.
     * @return The ETA in seconds.
     */
    public static int calcETA(Vec3 targetPosition, Vec3 currentPosition) {
        if (targetPosition == null || currentPosition == null) return Integer.MAX_VALUE;
        double distance = targetPosition.distanceTo(currentPosition);
        return (int) (distance / CMPConfigs.server().beeSpeed.get()) + 1;
    }

    /**
     * Instantly rotates the RoboEntity to look at its target.
     */
    public void lookAtTarget() {
        if (level().isClientSide()) return;
        BlockPos targetPos = getTargetPosition();
        if (targetPos != null) {
            Vec3 direction = new Vec3(targetPos.getX(), targetPos.getY(), targetPos.getZ()).subtract(this.position()).normalize();
            this.entityData.set(ROT_YAW, (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90);
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
        float currentYaw = this.entityData.get(ROT_YAW);
        float deltaYaw = targetYaw - currentYaw;
        deltaYaw = (deltaYaw > 180) ? deltaYaw - 360 : (deltaYaw < -180) ? deltaYaw + 360 : deltaYaw;
        float rotationSpeed = CMPConfigs.server().beeRotationSpeed.get();
        if (Math.abs(deltaYaw) > rotationSpeed) {
            currentYaw += (deltaYaw > 0) ? rotationSpeed : -rotationSpeed;
        } else {
            currentYaw = targetYaw;
        }
        this.entityData.set(ROT_YAW, currentYaw);
        return (int) Math.ceil(Math.abs(deltaYaw) / rotationSpeed);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (!getItemStack().isEmpty()) {
            nbt.put("itemStack", getItemStack().save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("itemStack", Tag.TAG_COMPOUND)) {
            setItemStack(ItemStack.of(nbt.getCompound("itemStack")));
        } else {
            setItemStack(ItemStack.EMPTY);
        }
    }

    @Override
    public void load(CompoundTag pCompound) {
        super.load(pCompound);
        if (pCompound.contains("itemStack")) {
            setItemStack(ItemStack.of(pCompound.getCompound("itemStack")));
        }
        if (!getItemStack().isEmpty()) {
            setTargetFromItemStack(getItemStack());
        }
        if (!level().isClientSide() && !getItemStack().isEmpty()) {
            setPackageHeightScale(1.0f);
        }
    }

    public void setTargetAddress(String address) {
        this.targetAddress = address;
        updateTarget();
    }

    @Override
    public void kill() {
        this.level().broadcastEntityEvent(this, (byte) 60);
        if (this.level() instanceof ServerLevel serverLevel) {
            ItemStack drop = new ItemStack(CMPItems.ROBO_BEE.get());
            Containers.dropItemStack(serverLevel, this.getX(), this.getY(), this.getZ(), drop);
        }
        this.discard();
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 60) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        } else {
            super.handleEntityEvent(pId);
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) return false;

        if (!this.level().isClientSide && !this.isRemoved()) {
            this.markHurt();
            this.damageCounter += pAmount * 10;
            if (this.damageCounter > 40) {
                handleItemStackOnRemove();
                this.discard();
                this.kill();
            }
        }

        return true;
    }

    public boolean getPathing() {
        return pathing;
    }

    public void setPathing(boolean pathing) {
        this.pathing = pathing;
    }
}
