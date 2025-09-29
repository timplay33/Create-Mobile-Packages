package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import de.theidler.create_mobile_packages.robo.VirtualRobo;

public class ShutdownState implements RoboEntityState {
    @Override
    public void tick(VirtualRobo re) {
        re.setRemoved(re.getServerLevel());
    }
}
