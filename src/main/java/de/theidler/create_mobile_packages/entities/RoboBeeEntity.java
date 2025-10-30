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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

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
    public boolean canCollideWith(@NotNull Entity entity) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public void push(@NotNull Entity entity) {
    }

    @Override
    protected void doPush(@NotNull Entity entity) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    public static RoboBeeEntity createEmpty(EntityType<? extends Mob> type, Level level) {
        UUID linkedId = UUID.randomUUID();
        return new RoboBeeEntity(type, level, linkedId);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (level() instanceof ServerLevel serverLevel) {
            BlockPos spawnPos = this.blockPosition();
            RoboManager.get(serverLevel).newRobo(serverLevel, this.getItemStack(), spawnPos, this.linkedId, 0);
        }
    }
}
