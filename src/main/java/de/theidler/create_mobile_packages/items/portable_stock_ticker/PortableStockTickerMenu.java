package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class PortableStockTickerMenu extends AbstractContainerMenu {
    public PortableStockTicker portableStockTicker;
    public Object screenReference;

    public PortableStockTickerMenu(int id, Inventory playerInventory, PortableStockTicker portableStockTicker) {
        super(CMPMenuTypes.DRONE_CONTROLLER_MENU.get(), id);
        this.portableStockTicker = portableStockTicker;
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
