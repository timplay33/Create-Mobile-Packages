package de.theidler.create_mobile_packages.entities.robo_bee_entity;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.index.CMPEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static de.theidler.create_mobile_packages.entities.robo_bee_entity.Helpers.getPlayerByName;
import static de.theidler.create_mobile_packages.entities.robo_bee_entity.Helpers.getPlayerNameByAddress;

public class RoboBeeEntity extends FlyingMob {

    private static final EntityDataAccessor<Float> PACKAGE_HEIGHT_SCALE = SynchedEntityData.defineId(RoboBeeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<ItemStack> ITEM_STACK = SynchedEntityData.defineId(RoboBeeEntity.class, EntityDataSerializers.ITEM_STACK);


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
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        flyingpathnavigation.setCanOpenDoors(false);
        return flyingpathnavigation;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FlyToPlayerGoal(this));
        this.goalSelector.addGoal(2, new FlyToBeePortGoal(this));
    }

    public void roboMangerTick() {
        super.tick();
        CreateMobilePackages.ROBO_MANAGER.markDirty();
        setTarget(getPlayerByName(level(), getPlayerNameByAddress(PackageItem.getAddress(getItemStack()))));
    }

    @Override
    public void tick() {

    }

    public ItemStack getItemStack() {
        return this.entityData.get(ITEM_STACK);
    }
    public void setItemStack(ItemStack stack) {
        this.entityData.set(ITEM_STACK, stack);
        if (!stack.isEmpty()) {
            this.setTarget(getPlayerByName(level(), getPlayerNameByAddress(PackageItem.getAddress(stack))));
        }
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
        this.entityData.define(ITEM_STACK, ItemStack.EMPTY);
    }

    @Override
    public void remove(RemovalReason pReason) {
        super.remove(pReason);
        if (!getItemStack().isEmpty() && pReason == RemovalReason.KILLED){
            level().addFreshEntity(new ItemEntity(
                    level(),
                    getX(),
                    getY(),
                    getZ(),
                    getItemStack()
            ));
            setItemStack(ItemStack.EMPTY);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("ItemStack")) {
            setItemStack(ItemStack.of(pCompound.getCompound("ItemStack")));
            setPackageHeightScale(1.0F);
        } else {
            setItemStack(ItemStack.EMPTY);
        }
        this.setPos(this.getX(), this.getY(), this.getZ()); // Force Position Update (Fixes Client Side de-sync with Carry On)
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (!getItemStack().isEmpty()) {
            pCompound.put("ItemStack", getItemStack().save(new CompoundTag()));
        }

    }
}
