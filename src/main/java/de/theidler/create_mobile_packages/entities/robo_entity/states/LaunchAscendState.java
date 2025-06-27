package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import net.minecraft.world.phys.Vec3;

public class LaunchAscendState implements RoboEntityState {
    private float initialDistanceToTarget = 0;
    @Override
    public void tick(RoboEntity re) {
        BeePortBlockEntity bpbe = re.getStartBeePortBlockEntity();
        if (bpbe == null) {
            re.setState(new LaunchFinishState());
            return;
        }

        Vec3 target = bpbe.getBlockPos().getCenter().add(0, 2, 0);
        Vec3 direction = target.subtract(re.position()).normalize();
        re.setTargetVelocity(direction.scale(1 / 20.0)); // fixed speed of 1 block per second

        double distanceToTarget = re.position().distanceToSqr(target);
        if (initialDistanceToTarget == 0) {
            initialDistanceToTarget = (float) distanceToTarget;
        }
        if (distanceToTarget < 0.2) {
            re.setPackageHeightScale(1.0f);
            re.setState(new LaunchFinishState());
        } else {
            float scale =1.0f - (float) (distanceToTarget / initialDistanceToTarget);
            re.setPackageHeightScale(scale);
        }
    }
}
