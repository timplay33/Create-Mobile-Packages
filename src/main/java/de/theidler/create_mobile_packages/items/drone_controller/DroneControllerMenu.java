package de.theidler.create_mobile_packages.items.drone_controller;

import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class DroneControllerMenu extends AbstractContainerMenu {
    public DroneController droneController;
    public Object screenReference;

    public DroneControllerMenu(int id, Inventory playerInventory, DroneController droneController) {
        super(CMPMenuTypes.DRONE_CONTROLLER_MENU.get(), id);
        this.droneController = droneController;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
