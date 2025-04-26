package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.blocks.drone_port.DronePortBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import net.minecraft.world.phys.Vec3;

public class LaunchFinishState implements RoboEntityState {
    @Override
    public void tick(RoboEntity re) {
        DronePortBlockEntity.setOpen(re.getStartDronePortBlockEntity(), false);
        re.setTargetVelocity(Vec3.ZERO);
        re.setState(new AdjustRotationToTarget());
    }
}
