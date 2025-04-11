package de.theidler.create_mobile_packages.entities;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FlyToPlayerGoal extends Goal {
    private final Mob mob;
    private Player targetPlayer;

    public FlyToPlayerGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        Player target = ((DroneEntity) this.mob).getTargetPlayer();
        if (target != null) {
            this.targetPlayer = target;
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if (this.targetPlayer != null && this.targetPlayer.isAlive()) {
            // Where to go (hover slightly above player)
            Vec3 targetPos = this.targetPlayer.position().add(0, 1, 0);
            Vec3 currentPos = this.mob.position();
            Vec3 direction = targetPos.subtract(currentPos);

            double distance = direction.length();
            if (distance < 1) {
                this.mob.setDeltaMovement(Vec3.ZERO); // close enough, stop
                return;
            }

            // Normalize and apply speed
            Vec3 velocity = direction.normalize().scale(0.25); // speed = 0.25
            this.mob.setDeltaMovement(velocity);
            this.mob.hasImpulse = true;

            // Look at the player
            this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
        }
    }
}
