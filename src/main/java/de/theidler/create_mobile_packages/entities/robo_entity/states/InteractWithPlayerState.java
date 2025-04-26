package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import net.minecraft.world.item.ItemStack;

import static de.theidler.create_mobile_packages.blocks.drone_port.DronePortBlockEntity.sendPackageToPlayer;

public class InteractWithPlayerState implements RoboEntityState {
    @Override
    public void tick(RoboEntity re) {
        if (sendPackageToPlayer(re.getTargetPlayer(), re.getItemStack())){
            re.removePackageEntity();
        }
        re.doPackageEntity = false;
        re.packageDelivered();
        //TODO: check if player has toBeSendPackages
        re.setState(new DeliveryDecisionState());
    }
}
