package de.theidler.create_mobile_packages.items;

import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DroneControllerMenu extends AbstractContainerMenu {
    public DroneController droneController;
    private final Inventory playerInventory;

    public DroneControllerMenu(int containerId, Inventory playerInventory) {
        super(CMPMenuTypes.DRONE_CONTROLLER_MENU.get(), containerId);
        this.playerInventory = playerInventory;

        ItemStack itemStack = playerInventory.getItem(playerInventory.selected);
        if (itemStack.getItem() instanceof DroneController){
            droneController = (DroneController) itemStack.getItem();
        } else {
            droneController = new DroneController(new Item.Properties());
        }
    }

    public DroneControllerMenu(MenuType<? extends DroneControllerMenu> droneControllerMenuMenuType, int containerId, Inventory playerInventory) {
        super(droneControllerMenuMenuType, containerId);
        this.playerInventory = playerInventory;
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
