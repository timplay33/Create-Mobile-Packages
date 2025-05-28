package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import net.minecraft.world.phys.Vec3;

public class LaunchFinishState implements RoboEntityState {
    @Override
    public void tick(RoboEntity re) {
        if (re.getStartBeePortBlockEntity() != null)
            BeePortBlockEntity.setOpen(re.getStartBeePortBlockEntity(), false);
        else
            BeePortalBlockEntity.setOpen(re.getStartBeePortalBlockEntity(), false);
        re.setTargetVelocity(Vec3.ZERO);
        re.setState(new AdjustRotationToTarget());
    }
}
