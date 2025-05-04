package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.blocks.bee_port.DronePortBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;

public class LandingPrepareState implements RoboEntityState {
    boolean init = true;
    @Override
    public void tick(RoboEntity re) {
        if (re.getTargetBlockEntity() != null) {
            if (init) {
                DronePortBlockEntity.setOpen(re.getTargetBlockEntity(), true);
                re.setPos(re.getTargetBlockEntity().getBlockPos().above().getCenter());
                init = false;
            }
            if (re.rotateToSnap() == 0) {
                re.setState(new LandingDecendStartState());
            }
        }
    }
}
