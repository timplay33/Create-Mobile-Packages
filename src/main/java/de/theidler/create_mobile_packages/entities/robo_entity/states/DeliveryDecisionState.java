package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;

public class DeliveryDecisionState implements RoboEntityState {
    @Override
    public void tick(RoboEntity re) {
        if (re.getItemStack().isEmpty()) {
            re.setTargetAddress(null);
            re.setState(new AdjustRotationToTarget());
            return;
        }
        //TODO: implement option to deliver package from player
    }
}
