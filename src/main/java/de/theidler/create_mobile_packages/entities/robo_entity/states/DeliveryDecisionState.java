package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import de.theidler.create_mobile_packages.robo.VirtualRobo;

public class DeliveryDecisionState implements RoboEntityState {
    @Override
    public void tick(VirtualRobo re) {
        if (re.getItemStack().isEmpty()) {
            re.setTargetAddress(null);
            re.setState(new AdjustRotationToTarget());
        }
        //TODO: implement option to deliver package from player
    }
}
