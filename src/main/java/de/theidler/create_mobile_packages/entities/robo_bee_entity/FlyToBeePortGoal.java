package de.theidler.create_mobile_packages.entities.robo_bee_entity;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_port.ModCapabilities;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static de.theidler.create_mobile_packages.entities.robo_bee_entity.Helpers.flyToVec3;
import static de.theidler.create_mobile_packages.entities.robo_bee_entity.Helpers.getClosestBeePort;

public class FlyToBeePortGoal extends Goal {

    private final Mob mob;

    public FlyToBeePortGoal(Mob mob) {
        this.mob = mob;
        if (mob instanceof RoboBeeEntity roboBee) {
        }
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(mob instanceof RoboBeeEntity roboBee)) {
            return false;
        }
        List<BeePortBlockEntity> allBEs = new ArrayList<>();
        mob.level().getCapability(ModCapabilities.BEE_PORT_ENTITY_TRACKER_CAP).ifPresent(tracker -> allBEs.addAll(tracker.getAll()));
        return allBEs.stream().anyMatch(beePort -> beePort.canAcceptEntity(roboBee, !roboBee.getItemStack().isEmpty()));
    }

    @Override
    public void tick() {
        if (!(mob instanceof RoboBeeEntity roboBee)) {
            return;
        }

        BeePortBlockEntity closestPort = getClosestBeePort(roboBee.level(), roboBee);
        if (closestPort != null) {
            flyToVec3(mob, closestPort.getBlockPos().getCenter());
        } else {
            roboBee.getNavigation().stop();
        }
    }
}
