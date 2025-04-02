package de.theidler.create_mobile_packages.items.drone_controller;

import com.simibubi.create.content.logistics.BigItemStack;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DroneControllerMenu extends AbstractContainerMenu {
    private final List<BigItemStack> bigItemStacks;
    public DroneController droneController;

    public DroneControllerMenu(int id, Inventory playerInventory, List<BigItemStack> inventorySummary, DroneController droneController) {
        super(CMPMenuTypes.DRONE_CONTROLLER_MENU.get(), id);
        this.bigItemStacks = inventorySummary;
        this.droneController = droneController;
    }

    public List<BigItemStack> getBigItemStacks() {
        return bigItemStacks;
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
