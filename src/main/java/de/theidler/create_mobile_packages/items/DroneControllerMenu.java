package de.theidler.create_mobile_packages.items;

import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class DroneControllerMenu extends AbstractContainerMenu {
    public DroneControllerMenu(int containerId, Inventory playerInventory) {
        super(CMPMenuTypes.DRONE_CONTROLLER_MENU.get(), containerId);
    }

    public DroneControllerMenu(MenuType<? extends DroneControllerMenu> droneControllerMenuMenuType, int containerId, Inventory playerInventory) {
        super(droneControllerMenuMenuType, containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
