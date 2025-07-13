package de.theidler.create_mobile_packages.entities.robo_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public class Target {

    private Vec3 targetPosition;
    private TargetType targetType;
    private int targetID;
    private String targetAddress;

    public Target(Vec3 targetPosition, TargetType targetType, @Nullable Integer targetID, @Nullable String targetAddress) {
        this.targetPosition = targetPosition;
        this.targetType = targetType;
        this.targetID = targetID != null ? targetID : -1;
        this.targetAddress = targetAddress != null ? targetAddress : "";
    }

    public static Target createPlayerTarget(Player player) {
        return new Target(player.position(), TargetType.PLAYER, player.getId(), null);
    }

    public static Target createBlockEntityTarget(BlockEntity blockEntity) {
        return new Target(blockEntity.getBlockPos().getCenter(), TargetType.BLOCKENTITY, null, null);
    }

    public static Target createAddressTarget(String address) {
        return new Target(Vec3.ZERO, TargetType.UNKNOWN, -1, address);
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

    public TargetType getTargetType() {
        return targetType;
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    public @Nullable Player getTargetPlayer(Level level) {
        if (targetType == TargetType.PLAYER && targetID != -1) {
            Entity entity = level.getEntity(targetID);
            if (entity instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    public @Nullable BlockEntity getTargetBlockEntity(Level level) {
        if (targetType == TargetType.BLOCKENTITY) {
            return level.getBlockEntity(new BlockPos((int) targetPosition.x, (int) targetPosition.y, (int) targetPosition.z));
        }
        return null;
    }

    public enum TargetType {
        UNKNOWN, PLAYER, BLOCKENTITY
    }
}
