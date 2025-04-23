package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;

public class AdjustRotationToTarget implements RoboEntityState {
    int wait = 0;
    @Override
    public void tick(RoboEntity re) {
        if (true) { //TODO: fix rotation
            if (wait++ < 20){
                return;
            }
            re.setState(new FlyToTargetState());
            return;
        }

        if (Math.round(re.getYRot() - re.getAngleToTarget()) == 0) {
            re.setState(new FlyToTargetState());
            return;
        }

        double currentAngle = re.getYRot();
        System.out.println("Current Angle: " + currentAngle);
        double targetAngle = re.getAngleToTarget();
        re.setYRot((float) targetAngle);
        //re.setState(new FlyToTargetState());
    }
}
