package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PortableStockTickerMenu extends AbstractContainerMenu {
    public PortableStockTicker portableStockTicker;
    public Object screenReference;
    public Player player;

    public PortableStockTickerMenu(int id, Inventory playerInventory, PortableStockTicker portableStockTicker) {
        super(CMPMenuTypes.PORTABLE_STOCK_TICKER_MENU.get(), id);
        this.portableStockTicker = portableStockTicker;
        this.player = playerInventory.player;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }
}
