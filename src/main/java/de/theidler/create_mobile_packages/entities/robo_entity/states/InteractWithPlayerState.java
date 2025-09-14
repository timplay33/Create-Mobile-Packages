package de.theidler.create_mobile_packages.entities.robo_entity.states;

import com.simibubi.create.content.logistics.box.PackageEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import de.theidler.create_mobile_packages.robo.VirtualRobo;
import net.minecraft.world.item.ItemStack;

import static de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity.sendPackageToPlayer;

public class InteractWithPlayerState implements RoboEntityState {
    @Override
    public void tick(VirtualRobo re) {
        if (!re.getItemStack().isEmpty()) {
            if (sendPackageToPlayer(re.getTargetPlayer(), re.getItemStack())){
                re.setItemStack(ItemStack.EMPTY);
            } else {
                re.getServerLevel().addFreshEntity(PackageEntity.fromItemStack(re.getServerLevel(), re.getCurrentPos(), re.getItemStack()));
                re.setItemStack(ItemStack.EMPTY);
            }
        }
        //TODO: check if player has toBeSendPackages
        re.setState(new DeliveryDecisionState());
    }
}
