package de.theidler.create_mobile_packages.entities;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.index.CMPEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RoboBeeEntity extends RoboEntity {

    public RoboBeeEntity(Level level, ItemStack itemStack, BlockPos targetPos, BlockPos spawnPos) {
        super(CMPEntities.ROBO_BEE_ENTITY.get(), level, itemStack, targetPos, spawnPos);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setNoAi(true);
        this.setPersistenceRequired();
    }

    public static RoboBeeEntity createEmpty(EntityType<? extends Mob> type, Level level) {
        return new RoboBeeEntity(level, ItemStack.EMPTY, null, new BlockPos(0, 0, 0));
    }

    // No AI goals; movement is entirely controlled via tick().
    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    protected void doPush(Entity entity) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public void checkDespawn() {
    }

}
