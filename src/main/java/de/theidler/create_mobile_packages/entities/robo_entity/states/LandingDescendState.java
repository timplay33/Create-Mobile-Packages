package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class LandingDescendState implements RoboEntityState {
    private int wait = 0;

    @Override
    public void tick(RoboEntity re) {
        BeePortBlockEntity targetBlock = re.getTargetBlockEntity();
        BeePortalBlockEntity targetPortal = re.getTargetPortalEntity();
        Player targetPlayer = re.getTargetPlayer();
        re.updateTarget();
        if (targetBlock != re.getTargetBlockEntity() || targetPortal != re.getTargetPortalEntity() || targetPlayer != re.getTargetPlayer()) {
            re.setState(new FlyToTargetState());
            return;
        }

        if (targetBlock == null && targetPlayer == null) return;
        if (targetBlock != null && !targetBlock.canAcceptEntity(re, !re.getItemStack().isEmpty())) {
            re.setTargetVelocity(Vec3.ZERO);
            return;
        }

        Vec3 targetPos = targetPortal != null ? targetPortal.getBlockPos().getCenter() : targetBlock != null ? targetBlock.getBlockPos().getCenter() : targetPlayer.position();
        Vec3 direction = re.position().vectorTo(targetPos).normalize();
        re.setTargetVelocity(direction.scale(1 / 20.0)); // fixed speed of 1 block per second

        double distanceToTarget = re.position().distanceToSqr(targetPos);
        if (distanceToTarget < 0.2) {
            re.setPackageHeightScale(0.0f);
            if (wait++ < 10) return;
            re.setState(new LandingFinishState());
        }

        if (distanceToTarget < 1.0) {
            re.setPackageHeightScale((float) distanceToTarget);
        }
    }
}
