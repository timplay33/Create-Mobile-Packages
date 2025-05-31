package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;

public class LaunchPrepareState implements RoboEntityState {
    @Override
    public void tick(RoboEntity re) {
        BeePortBlockEntity startPort = re.getStartBeePortBlockEntity();
        BeePortalBlockEntity startPortal = re.getStartBeePortalBlockEntity();
        if (startPortal != null && !startPortal.isLaunchingPeek(re)
                || startPort != null && !startPort.isLaunchingPeek(re))
            return;

        if (startPort != null) startPort.tryAddToLaunchingQueue(re);
        else if (startPortal != null) startPortal.tryAddToLaunchingQueue(re);
        re.setState(new LaunchAscendState());
    }
}