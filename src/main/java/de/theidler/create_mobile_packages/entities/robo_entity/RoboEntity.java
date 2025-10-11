package de.theidler.create_mobile_packages.entities.robo_entity;

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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.UUID;

public class RoboEntity extends Mob {

    private static final EntityDataAccessor<Float> ROT_YAW = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> PACKAGE_HEIGHT_SCALE = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.FLOAT);

    public UUID linkedId;

    /**
     * Constructor for RoboEntity. Used for spawning the entity.
     *
     * @param type  The entity type.
     * @param level The level in which the entity exists.
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
        } else if (virtualRobo.getTarget() != null && virtualRobo.getTarget().asBeePortBlockEntity() != null) {
            BlockPos pos = virtualRobo.getTarget().asBeePortBlockEntity().getBlockPos();
            setCustomName(Component.literal("-> [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"));
            setCustomNameVisible(true);
        }
    }

    public ItemStack getItemStack() {
        return this.entityData.get(DATA_ITEM_STACK);
    }

    public float getPackageHeightScale() {
        return this.entityData.get(PACKAGE_HEIGHT_SCALE);
    }

    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
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
