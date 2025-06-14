package de.theidler.create_mobile_packages.entities.robo_bee_entity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

import static de.theidler.create_mobile_packages.entities.robo_bee_entity.Helpers.flyToVec3;

public class FlyToPlayerGoal extends Goal {

    private final Mob mob;
    private Player targetPlayer;

    public FlyToPlayerGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
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
            flyToVec3(mob, targetPlayer.position());
        } else {
            mob.getNavigation().stop();
        }
    }
}
