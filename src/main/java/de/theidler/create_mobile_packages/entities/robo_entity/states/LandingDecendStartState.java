package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import net.minecraft.world.phys.Vec3;

public class LandingDecendStartState implements RoboEntityState {
    int wait = 0;
    @Override
    public void tick(RoboEntity re) {
        if (re.getTargetBlockEntity() != null) {
            Vec3 target = re.getTargetBlockEntity().getBlockPos().getCenter().subtract(0, 0.5, 0);
            Vec3 direction = target.subtract(re.position()).normalize();
            re.setTargetVelocity(direction.scale(1 / 20.0)); // fixed speed of 1 block per second

            if (re.position().distanceToSqr(target) < 0.2) {
                if (wait++ < 20) {
                    return;
                }
                re.setState(new LandingDecendFinishState());
            }
        }
    }
}
