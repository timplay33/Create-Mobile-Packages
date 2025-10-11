package de.theidler.create_mobile_packages.entities;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.robo.RoboManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class RoboBeeEntity extends RoboEntity {

    public RoboBeeEntity(EntityType<? extends Mob> entityEntityType, Level level, UUID linkedId) {
        super(entityEntityType, level, linkedId);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setNoAi(true);
        this.setPersistenceRequired();
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

    public static RoboBeeEntity createEmpty(EntityType<? extends Mob> type, Level level) {
        UUID linkedId = null;
        if (level instanceof ServerLevel serverLevel) {
            linkedId = RoboManager.get(serverLevel).newRobo(serverLevel, ItemStack.EMPTY, BlockPos.ZERO, UUID.randomUUID(), true); // TODO:Let the bee be liked to a network
        }
        RoboBeeEntity entity = new RoboBeeEntity(type, level, linkedId);
        return entity;
    }
}
