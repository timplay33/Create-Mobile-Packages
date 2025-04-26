package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class FlyToTargetState implements RoboEntityState {
    @Override
    public void tick(RoboEntity re) {
        BlockPos targetPos = re.getTargetPosition();
        if (targetPos == null) { return; }
        if (re.position().distanceTo(targetPos.getCenter()) <= CMPConfigs.server().droneSpeed.get()/12.0) {
            if (re.getTargetBlockEntity() != null) {
                re.setState(new LandingPrepareState());
            } else if (re.getTargetPlayer() != null) {
                re.setState(new InteractWithPlayerState());
            }
            re.setTargetVelocity(Vec3.ZERO);
        } else {
            if (re.getTargetPlayer() != null) {
                re.updateDisplay(re.getTargetPlayer());
            }
            Vec3 direction = targetPos.getCenter().subtract(re.position()).normalize();
            double speed = CMPConfigs.server().droneSpeed.get() / 20.0;
            re.setTargetVelocity(direction.scale(speed));
            if (re.position().distanceTo(targetPos.getCenter()) > 2.5) { // entity rotation starts drifting
                re.lookAtTarget();
            }
        }


    }
}
