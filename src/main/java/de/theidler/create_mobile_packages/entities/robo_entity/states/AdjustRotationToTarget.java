package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;

public class AdjustRotationToTarget implements RoboEntityState {
    @Override
    public void tick(RoboEntity re) {
        if (re.rotateLookAtTarget() == 0){
            re.setState(new FlyToTargetState());
        }
    }
}
