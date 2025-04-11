package de.theidler.create_mobile_packages.entities;

import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FlyToPlayerGoal extends Goal {
    private final Mob mob;
    private Player targetPlayer;
    private Vec3 origin;
    private boolean returning = false;

    private Vec3 lastPosition = Vec3.ZERO;
    private int stuckTicks = 0;
    private final int maxStuckTicks = 40; // 2 seconds
    private final double stuckDistanceThreshold = 0.05; // minimal movement

    public FlyToPlayerGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        Player target = ((DroneEntity) this.mob).getTargetPlayer();
        if (target != null) {
            this.targetPlayer = target;
            this.origin = this.mob.position();
            this.returning = false;
            this.lastPosition = this.mob.position();
            this.stuckTicks = 0;
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetPlayer != null && this.targetPlayer.isAlive();
    }

    @Override
    public void tick() {
        Vec3 currentPos = this.mob.position();
        Vec3 targetPos;

        if (!returning) {
            targetPos = this.targetPlayer.position().add(0, 1, 0);
            if (currentPos.distanceTo(targetPos) < 1.5) {
                returning = true;
            }
        } else {
            targetPos = origin;
            if (currentPos.distanceTo(origin) < 1.5) {
                this.mob.discard();
                return;
            }
        }

        // --- Stuck detection ---
        double movedDistance = currentPos.distanceTo(lastPosition);
        if (movedDistance < stuckDistanceThreshold) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
        }
        lastPosition = currentPos;

        // Get speed from config
        double blocksPerSecond = CMPConfigs.server().droneSpeed.get();
        double speedPerTick = blocksPerSecond / 20.0;

        // Movement vector
        Vec3 direction = targetPos.subtract(currentPos).normalize();

        // If stuck for too long, apply random offset
        if (stuckTicks >= maxStuckTicks) {
            double randomX = (mob.getRandom().nextDouble() - 0.5) * 2;
            double randomY = (mob.getRandom().nextDouble() - 0.5) * 2;
            double randomZ = (mob.getRandom().nextDouble() - 0.5) * 2;
            direction = direction.add(randomX, randomY, randomZ).normalize();
            stuckTicks = 0; // reset after attempt
        }

        Vec3 velocity = direction.scale(speedPerTick);
        Vec3 smoothed = this.mob.getDeltaMovement().lerp(velocity, 0.2);
        this.mob.setDeltaMovement(smoothed);
        this.mob.hasImpulse = true;

        this.mob.getLookControl().setLookAt(targetPos.x, targetPos.y, targetPos.z, 30.0F, 30.0F);
    }
}
