package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import net.minecraft.world.item.ItemStack;

public class LandingDescendFinishState implements RoboEntityState {
    boolean init = true;

    @Override
    public void tick(RoboEntity re) {
        if (re.getTargetBlockEntity() != null && init) {
            BeePortBlockEntity.setOpen(re.getTargetBlockEntity(), false);
            re.getTargetBlockEntity().addBeeToRoboBeeInventory(1);
            init = false;
        }

        if (re.getItemStack().isEmpty()) re.setState(new ShutdownState());

        if (re.getTargetBlockEntity() != null) {
            if (re.getTargetBlockEntity().addItemStack(re.getItemStack())){
                re.setItemStack(ItemStack.EMPTY);
            }
        }
    }
}
