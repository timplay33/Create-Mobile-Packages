package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;

public class LaunchPrepareState implements RoboEntityState {
    @Override
    public void tick(RoboEntity re) {
        if (re.getStartBeePortalBlockEntity() != null && !re.getStartBeePortalBlockEntity().isLaunchingPeek(re)) return;
        BeePortBlockEntity.setOpen(re.getStartBeePortBlockEntity(), true);
        re.setState(new LaunchAscendState());
    }
}