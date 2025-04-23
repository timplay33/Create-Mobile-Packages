package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.blocks.drone_port.DronePortBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;

public class LandingPrepareState implements RoboEntityState {
    int wait = 0;
    @Override
    public void tick(RoboEntity re) {
        if (re.getTargetBlockEntity() != null) {
            if (wait == 0) {
                DronePortBlockEntity.setOpen(re.getTargetBlockEntity(), true);
                re.setPos(re.getTargetBlockEntity().getBlockPos().above().getCenter());
            }
            //TODO: slowly rotate to snap angle
            if (wait++ < 20) {
                return;
            }
            re.setState(new LandingDecendStartState());
        }
    }
}
