package de.theidler.create_mobile_packages.entities;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.index.CMPEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RoboBeeEntity extends FlyingMob {

    private static final EntityDataAccessor<Float> PACKAGE_HEIGHT_SCALE = SynchedEntityData.defineId(RoboBeeEntity.class, EntityDataSerializers.FLOAT);

    public RoboBeeEntity(EntityType<? extends FlyingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setPersistenceRequired();
        CreateMobilePackages.ROBO_MANAGER.addRobo(this);
    }

    public static RoboBeeEntity createBeeEntity(Level level, ItemStack itemStack, BlockPos spawnPos) {
        RoboBeeEntity entity = new RoboBeeEntity(CMPEntities.ROBO_BEE_ENTITY.get(), level);
        entity.setItemStack(itemStack);
        entity.setPos(spawnPos.getCenter());
        return entity;
    }

    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FlyToPlayerGoal(this));
    }

    public void roboMangerTick() {
        super.tick();
        CreateMobilePackages.ROBO_MANAGER.markDirty();
        if (this.getTarget() == null) {
            this.setTarget(level().getNearestPlayer(this, 10.0D));
        }
    }

    @Override
    public void tick() {

    }

    public ItemStack getItemStack() {
        return this.getSlot(0).get();
    }
    public void setItemStack(ItemStack stack) {
        this.getSlot(0).set(stack);
    }

    public float getPackageHeightScale() {
        return this.entityData.get(PACKAGE_HEIGHT_SCALE);
    }
    public void setPackageHeightScale(float scale) {
        if (scale < 0.0f || scale > 1.0f) return;
        this.entityData.set(PACKAGE_HEIGHT_SCALE, scale);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PACKAGE_HEIGHT_SCALE, 0.0F);
    }


}
