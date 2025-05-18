package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;

import static de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity.sendPackageToPlayer;

public class InteractWithPlayerState implements RoboEntityState {
    @Override
    public void tick(RoboEntity re) {
        if (sendPackageToPlayer(re.getTargetPlayer(), re.getItemStack())){
            re.removePackageEntity();
        }
        re.packageDelivered();
        //TODO: check if player has toBeSendPackages
        re.setState(new DeliveryDecisionState());
    }
}
