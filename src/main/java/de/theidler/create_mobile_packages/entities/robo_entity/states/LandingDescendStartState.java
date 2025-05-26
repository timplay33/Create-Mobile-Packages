package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import net.minecraft.world.phys.Vec3;

public class LandingDescendStartState implements RoboEntityState {
    private int wait = 0;

    @Override
    public void tick(RoboEntity re) {
        if (re.getTargetBlockEntity() != null && !re.getTargetBlockEntity().canAcceptEntity(re, !re.getItemStack().isEmpty()))
            return;

        if (re.getTargetBlockEntity() == null) return;
        Vec3 target = (re.getTargetPortalEntity() != null ? re.getTargetPortalEntity() : re.getTargetBlockEntity()).getBlockPos().getCenter().subtract(0, 0.5, 0);
        Vec3 direction = target.subtract(re.position()).normalize();
        re.setTargetVelocity(direction.scale(1 / 20.0)); // fixed speed of 1 block per second

        double distanceToTarget = re.position().distanceToSqr(target);
        if (distanceToTarget < 0.2) {
            re.setPackageHeightScale(0.0f);
            if (wait++ < 10) {
                return;
            }
            re.setState(new LandingDescendFinishState());
        }

        if (distanceToTarget < 1.0) {
            re.setPackageHeightScale((float) distanceToTarget);
        }
    }
}
