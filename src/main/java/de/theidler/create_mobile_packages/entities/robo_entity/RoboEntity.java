package de.theidler.create_mobile_packages.entities.robo_entity;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_port.ModCapabilities;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import de.theidler.create_mobile_packages.robo.RoboManager;
import de.theidler.create_mobile_packages.robo.VirtualRobo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.*;

public class RoboEntity extends Mob {

    private static final EntityDataAccessor<Float> ROT_YAW = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> PACKAGE_HEIGHT_SCALE = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.FLOAT);

    public UUID linkedId;

    /**
     * Constructor for RoboEntity. Used for spawning the entity.
     *
     * @param type      The entity type.
     * @param level     The level in which the entity exists.
     */
    public RoboEntity(EntityType<? extends Mob> type, Level level, UUID linkedId) {
        super(type, level);
        this.linkedId = linkedId;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROT_YAW, getYRot());
        this.entityData.define(DATA_ITEM_STACK, ItemStack.EMPTY);
        this.entityData.define(PACKAGE_HEIGHT_SCALE, 0.0f);
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
    public static BeePortBlockEntity getClosestBeePort(Level level, String address, BlockPos origin, VirtualRobo entity) {
        final BeePortBlockEntity[] closest = {null};
        level.getCapability(ModCapabilities.BEE_PORT_ENTITY_TRACKER_CAP).ifPresent(tracker -> {
            List<BeePortBlockEntity> allBEs = new ArrayList<>(tracker.getAll());
            allBEs.removeIf(BlockEntity::isRemoved);
            allBEs.removeIf(dpbe -> !isWithinRange(dpbe.getBlockPos(), origin));
            if (address != null) {
                allBEs.removeIf(dpbe -> !PackageItem.matchAddress(address, dpbe.addressFilter));
            }
            allBEs.removeIf(dpbe -> !dpbe.canAcceptEntity(entity, (entity != null && !entity.getItemStack().isEmpty())));
            closest[0] = allBEs.stream()
                    .min(Comparator.comparingDouble(a -> a.getBlockPos().distSqr(origin)))
                    .orElse(null);
        });
        return closest[0];
    }

    @Override
    public void tick() {
        super.tick();
        setYRot(this.entityData.get(ROT_YAW));
        if (level() instanceof ServerLevel serverLevel) {
            if (RoboManager.get(serverLevel).get(linkedId) == null) {
                this.discard();
            }
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean save(@Nonnull CompoundTag compound) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag compound) {
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag compound) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    private void updateNametag(VirtualRobo virtualRobo) {
        if (level().isClientSide) return;
        if (!CMPConfigs.server().displayNametag.get()) {
            setCustomName(null);
            setCustomNameVisible(false);
        } else if (virtualRobo.getTargetAddress() != null && !virtualRobo.getTargetAddress().isBlank()) {
            setCustomName(Component.literal("-> " + virtualRobo.getTargetAddress()));
            setCustomNameVisible(true);
        } else if (virtualRobo.getTargetBlockEntity() != null) {
            BlockPos pos = virtualRobo.getTargetBlockEntity().getBlockPos();
            setCustomName(Component.literal("-> [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"));
            setCustomNameVisible(true);
        } else {
            setCustomName(null);
            setCustomNameVisible(false);
        }
    }

    public ItemStack getItemStack() {
        return this.entityData.get(DATA_ITEM_STACK);
    }

    public float getPackageHeightScale() {
        return this.entityData.get(PACKAGE_HEIGHT_SCALE);
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

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return false; // RoboEntity cannot be damaged.
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    public void syncFromVirtual(VirtualRobo virtualRobo) {
        updateNametag(virtualRobo);
        this.setPos(virtualRobo.getCurrentPos());
        this.setXRot(virtualRobo.getPitch());
        this.entityData.set(ROT_YAW, virtualRobo.getYaw());
        this.entityData.set(DATA_ITEM_STACK, virtualRobo.getItemStack());
        this.entityData.set(PACKAGE_HEIGHT_SCALE, virtualRobo.getPackageHeightScale());
    }
}
