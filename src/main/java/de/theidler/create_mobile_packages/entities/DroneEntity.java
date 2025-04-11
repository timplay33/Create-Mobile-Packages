package de.theidler.create_mobile_packages.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class DroneEntity extends PathfinderMob {
    private UUID targetPlayerUUID;

    public DroneEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public void setTargetPlayerUUID(UUID uuid) {
        this.targetPlayerUUID = uuid;
    }
    public UUID getTargetPlayerUUID() {
        return this.targetPlayerUUID;
    }
    public Player getTargetPlayer() {
        if (this.targetPlayerUUID == null) return null;
        return this.level().getPlayerByUUID(this.targetPlayerUUID);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FlyToPlayerGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        return new FlyingPathNavigation(this, pLevel);
    }

    @Override
    public void travel(Vec3 pTravelVector) {
        if (this.isControlledByLocalInstance()) {
            this.moveRelative(this.getSpeed(), pTravelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91)); // damping
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0) // Maximale Gesundheit
                .add(Attributes.MOVEMENT_SPEED, 1); // Bewegungsgeschwindigkeit
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }
}
