package de.theidler.create_mobile_packages.entities;

import net.minecraft.commands.arguments.EntityAnchorArgument;
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
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() instanceof Player player) {
            this.targetPlayer = player;
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if (targetPlayer != null && targetPlayer.isAlive()) {
            mob.lookAt(EntityAnchorArgument.Anchor.EYES, targetPlayer.position().add(0, 1.5f, 0));

            double distance = mob.distanceTo(targetPlayer);
            if (distance > 2.0) {
                Vec3 direction = new Vec3(
                        targetPlayer.getX() - mob.getX(),
                        targetPlayer.getY()+1.5f - mob.getY(),
                        targetPlayer.getZ() - mob.getZ()
                ).normalize().scale(0.5);
                mob.setDeltaMovement(direction);
            } else {
                mob.setDeltaMovement(Vec3.ZERO);
            }
        } else {
            mob.setDeltaMovement(Vec3.ZERO);
        }
    }
}
