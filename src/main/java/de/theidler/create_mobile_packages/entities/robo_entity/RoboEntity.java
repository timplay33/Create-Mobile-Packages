package de.theidler.create_mobile_packages.entities.robo_entity;

import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_port.ModCapabilities;
import de.theidler.create_mobile_packages.entities.robo_entity.states.AdjustRotationToTarget;
import de.theidler.create_mobile_packages.entities.robo_entity.states.LandingDescendFinishState;
import de.theidler.create_mobile_packages.entities.robo_entity.states.LaunchPrepareState;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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

    private RoboEntityState state;
    private Vec3 targetVelocity = Vec3.ZERO;
    private ItemStack itemStack;
    private Player targetPlayer;
    private BeePortBlockEntity targetBlockEntity;
    private BeePortBlockEntity startBeePortBlockEntity;
    private String targetAddress = "";

    private final List<ChunkPos> loadedChunks = new ArrayList<>();
    public PackageEntity packageEntity;
    public boolean doPackageEntity = false;

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
        if (targetPos != null) {
            this.targetBlockEntity = level.getBlockEntity(targetPos) instanceof BeePortBlockEntity dpbe ? dpbe : null;
            if (this.targetBlockEntity != null) {
                setState(new LaunchPrepareState());
            }
        }
        setItemStack(itemStack);
        createPackageEntity(itemStack);
        setTargetFromItemStack(itemStack);
        this.setPos(spawnPos.getCenter().subtract(0, 0.5, 0));
        if (targetBlockEntity != null) {targetBlockEntity.trySetEntityOnTravel(this);}
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

    /**
     * Creates a `PackageEntity` from the given `ItemStack` and initializes its properties.
     * The created entity is added to the level.
     *
     * @param itemStack The `ItemStack` used to create the `PackageEntity`.
     *                  If null or not a package, the method returns without action.
     */
    public void createPackageEntity(ItemStack itemStack) {
        if (itemStack == null || !PackageItem.isPackage(itemStack)) return;

        packageEntity = PackageEntity.fromItemStack(level(), this.position(), itemStack);
        packageEntity.noPhysics = true;
        packageEntity.setNoGravity(true);
        packageEntity.setPos(this.getX(), this.getY(), this.getZ());

        int randomAngle = new java.util.Random().nextInt(4) * 90;
        packageEntity.setYRot(randomAngle);
        packageEntity.setYHeadRot(randomAngle);
        packageEntity.setYBodyRot(randomAngle);

        level().addFreshEntity(packageEntity);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROT_YAW, getYRot());
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
        if (level().isClientSide) { return; }
        targetPlayer = getTargetPlayerFromAddress();
        if (targetPlayer != null) { return; }
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
        BlockPos targetPos = null;
        if (targetPlayer != null) {
            targetPos = targetPlayer.blockPosition();
        } else if (targetBlockEntity != null) {
            targetPos = targetBlockEntity.getBlockPos();
        }
        return targetPos != null ? targetPos.above().above() : null;
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
        updatePackageEntity();
    }

public void updatePackageEntity() {
    if (packageEntity == null) return;

    if (doPackageEntity) {
        packageEntity.setPos(this.getX(), this.getY() - 0.8, this.getZ());
    }

    if (packageEntity.isRemoved()) {
        packageDelivered();
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
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null) return;
        this.itemStack = itemStack;
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

        if (getTargetBlockEntity() != null) {
            getTargetBlockEntity().trySetEntityOnTravel(null);
        }

        if (pReason == RemovalReason.KILLED && packageEntity != null) {
            if (this.targetPlayer != null) {
                targetPlayer.displayClientMessage(Component.translatable("create_mobile_packages.robo_entity.death", Math.round(this.getX()), Math.round(this.getY()), Math.round(this.getZ()), targetPlayer.getName().getString()), false);
            }
        }
        // unload all chunks
        loadedChunks.forEach(chunkPos -> {
            if (level() instanceof ServerLevel serverLevel) {
                ForgeChunkManager.forceChunk(serverLevel, CreateMobilePackages.MODID, this.blockPosition(), chunkPos.x, chunkPos.z, false, false);
            }
        });
        super.remove(pReason);
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
        player.displayClientMessage(Component.translatable("create_mobile_packages.robo_entity.eta", calcETA(player)), true);
    }

    /**
     * Calculates the estimated time of arrival (ETA) to the specified player.
     *
     * @param player The player to calculate the ETA for.
     * @return The ETA in seconds.
     */
    public int calcETA(Player player) {
        if (player == null) return Integer.MAX_VALUE;
        double distance = player.position().distanceTo(this.position());
        return (int) (distance / CMPConfigs.server().droneSpeed.get()) + 1;
    }
    public int calcETA(BeePortBlockEntity beePortBlockEntity) {
        if (beePortBlockEntity == null) return Integer.MAX_VALUE;
        double distance = beePortBlockEntity.getBlockPos().above(2).getCenter().distanceTo(this.position());
        return (int) (distance / CMPConfigs.server().droneSpeed.get()) + 1;
    }

    /**
     * Instantly rotates the RoboEntity to look at its target.
     */
    public void lookAtTarget(){
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
    public int rotateLookAtTarget(){
        return rotateToAngle((float) getAngleToTarget()+90);
    }

    /**
     * Rotates the RoboEntity to the nearest snap angle.
     *
     * @return The number of ticks required to complete the rotation.
     */
    public int rotateToSnap(){
        return rotateToAngle((float) getSnapAngle(getAngleToTarget())+90);
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
        float rotationSpeed = CMPConfigs.server().droneRotationSpeed.get();
        if (Math.abs(deltaYaw) > rotationSpeed) {
            currentYaw += (deltaYaw > 0) ? rotationSpeed : -rotationSpeed;
        } else {
            currentYaw = targetYaw;
        }
        this.entityData.set(ROT_YAW, currentYaw);
        return (int) Math.ceil(Math.abs(deltaYaw) / rotationSpeed);
    }

    public boolean hasPackageEntity() {
        return packageEntity != null;
    }

    public void removePackageEntity() {
        if (packageEntity == null) return;
        packageEntity.remove(Entity.RemovalReason.DISCARDED);
    }

    public void packageDelivered() {
        this.packageEntity = null;
        this.itemStack = ItemStack.EMPTY;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (packageEntity != null) {
            packageEntity.discard();
            packageEntity = null;
        }
        if (!itemStack.isEmpty()) {
            nbt.put("itemStack", itemStack.save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("itemStack", Tag.TAG_COMPOUND)) {
            itemStack = ItemStack.of(nbt.getCompound("itemStack"));
        } else {
            itemStack = ItemStack.EMPTY;
        }
    }

    @Override
    public void load(CompoundTag pCompound) {
        super.load(pCompound);
        if (pCompound.contains("itemStack")) {
            itemStack = ItemStack.of(pCompound.getCompound("itemStack"));
        }
        if (!itemStack.isEmpty()) {
            setTargetFromItemStack(itemStack);
        }
        if (!level().isClientSide() && !itemStack.isEmpty() && packageEntity == null) {
            createPackageEntity(itemStack);
            doPackageEntity = true;
        }
    }

    public void setTargetAddress(String address) {
        this.targetAddress = address;
        updateTarget();
    }
}
