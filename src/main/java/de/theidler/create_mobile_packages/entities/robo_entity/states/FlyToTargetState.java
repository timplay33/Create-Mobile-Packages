package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.Location;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.world.phys.Vec3;

public class FlyToTargetState implements RoboEntityState {

    @Override
    public void tick(RoboEntity re) {
        Location targetLocation = re.getTargetLocation();
        if (targetLocation == null || targetLocation.position() == null) {
            return;
        }

        if (re.position().distanceTo(targetLocation.position().getCenter()) <= CMPConfigs.server().beeSpeed.get() / 12f) {
            if (re.getTargetPlayer() != null && re.getTargetPortalEntity() != null)
                re.updateDisplay(re.getTargetPlayer());

            if (re.multidimensional()) {
                if (re.getTargetPortalEntity() != null)
                    re.setState(new LandingPrepareState());
            } else {
                if (re.getTargetBlockEntity() != null) re.setState(new LandingPrepareState());
                else if (re.getTargetPlayer() != null) re.setState(new InteractWithPlayerState());
                re.setTargetVelocity(Vec3.ZERO);
            }
        } else {
            if (re.getTargetPlayer() != null)
                re.updateDisplay(re.getTargetPlayer());

            Vec3 direction = targetLocation.position().getCenter().subtract(re.position()).normalize();
            double speed = CMPConfigs.server().beeSpeed.get() / 20f;
            re.setTargetVelocity(direction.scale(speed));
            if (re.position().distanceTo(targetLocation.position().getCenter()) > 2.5) { // entity rotation starts drifting
                re.lookAtTarget();
            }
        }
    }
}
