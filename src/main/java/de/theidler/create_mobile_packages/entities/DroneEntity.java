package de.theidler.create_mobile_packages.entities;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class DroneEntity extends Mob {

    private UUID targetPlayerUUID;
    private Vec3 targetVelocity = Vec3.ZERO;
    private Vec3 origin;

    private enum DroneState { MOVING_TO_PLAYER, WAITING, RETURNING }
    private DroneState state = DroneState.MOVING_TO_PLAYER;
    private int waitTicks = 60;

    public DroneEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setNoAi(true);
        origin = this.position();
    }

    public void setTargetPlayerUUID(UUID uuid) {
        this.targetPlayerUUID = uuid;
    }

    public Player getTargetPlayer() {
        if (this.targetPlayerUUID == null) return null;
        return this.level().getPlayerByUUID(this.targetPlayerUUID);
    }

    public void setOrigin(Vec3 origin) {
        this.origin = origin;
    }
    /**
     * Each tick, the drone behaves according to its state:
     *
     * - MOVING_TO_PLAYER: Moves in a straight line toward the target player.
     *   When close (within 0.5 blocks), it transitions to WAITING.
     *
     * - WAITING: Remains at approximately 0.5 blocks away for a set time.
     *   When waitTicks reaches 0, state changes to RETURNING.
     *
     * - RETURNING: Moves in a straight line back to its origin.
     *   Once close to the origin (within 0.5 blocks), the drone is discarded.
     */
    @Override
    public void tick() {
        super.tick();

        Player target = getTargetPlayer();
        Vec3 currentPos = this.position();

        switch (state) {
            case MOVING_TO_PLAYER:
                if (target != null && target.isAlive()) {
                    Vec3 desiredTarget = target.position();
                    if (currentPos.distanceTo(desiredTarget) <= 1.5) {
                        state = DroneState.WAITING;
                        targetVelocity = Vec3.ZERO;
                    } else {
                        Vec3 direction = desiredTarget.subtract(currentPos).normalize();
                        double speed = 0.5;
                        targetVelocity = direction.scale(speed);
                    }
                } else {
                    targetVelocity = Vec3.ZERO;
                }
                break;

            case WAITING:
                targetVelocity = Vec3.ZERO;
                waitTicks--;
                if (waitTicks <= 0) {
                    state = DroneState.RETURNING;
                }
                break;

            case RETURNING:
                if (origin == null) {
                    targetVelocity = Vec3.ZERO;
                    break;
                }
                if (currentPos.distanceTo(origin) <= 0.5) {
                    this.discard();
                    return;
                } else {
                    Vec3 direction = origin.subtract(currentPos).normalize();
                    double speed = 0.5;
                    targetVelocity = direction.scale(speed);
                }
                break;
        }

        this.setDeltaMovement(targetVelocity);
        this.move(MoverType.SELF, targetVelocity);
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
}
