package de.theidler.create_mobile_packages.entities.robo_entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Target {

    private Vec3 targetPosition;
    private TargetType targetType;
    private int targetID;

    public Target(Vec3 targetPosition, TargetType targetType, int targetID) {
        this.targetPosition = targetPosition;
        this.targetType = targetType;
        this.targetID = targetID;
    }

    public Vec3 getTargetPosition() {
        return targetPosition;
    }

    public void tick(Level level) {
        if (targetType == TargetType.PLAYER) {
            Entity entity = level.getEntity(targetID);
            if (entity != null) {
                targetPosition = entity.position();
            }
        }
    }

    public enum TargetType {
        PLAYER,
        BLOCKENTITY
    }
}
