package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.blocks.drone_port.DronePortBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import net.minecraft.world.item.ItemStack;

public class LandingDecendFinishState implements RoboEntityState {

    @Override
    public void tick(RoboEntity re) {
        if (re.getTargetBlockEntity() != null) {
            DronePortBlockEntity.setOpen(re.getTargetBlockEntity(), false);
        }

        if (re.getItemStack().isEmpty()) re.setState(new ShutdownState());

        if (re.getTargetBlockEntity() != null) {
            re.getTargetBlockEntity().addBeeToRoboBeeInventory(1);
            if (re.getTargetBlockEntity().addItemStack(re.getItemStack())){
                re.setItemStack(ItemStack.EMPTY);
            }
        }
    }
}
