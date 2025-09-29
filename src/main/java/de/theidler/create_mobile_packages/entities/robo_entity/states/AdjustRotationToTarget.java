package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import de.theidler.create_mobile_packages.robo.VirtualRobo;

public class AdjustRotationToTarget implements RoboEntityState {
    @Override
    public void tick(VirtualRobo re) {
        if (re.rotateLookAtTarget() == 0){
            re.setState(new FlyToTargetState());
        }
    }
}
