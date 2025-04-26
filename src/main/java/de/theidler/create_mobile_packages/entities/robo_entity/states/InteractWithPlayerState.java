package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;

import static de.theidler.create_mobile_packages.blocks.drone_port.DronePortBlockEntity.sendPackageToPlayer;

public class InteractWithPlayerState implements RoboEntityState {
    @Override
    public void tick(RoboEntity re) {
        sendPackageToPlayer(re.getTargetPlayer(), re.getItemStack(), re.blockPosition());
        //TODO: check if player has toBeSendPackages
        re.setState(new DeliveryDecisionState());
    }
}
