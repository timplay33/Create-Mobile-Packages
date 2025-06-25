package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import static de.theidler.create_mobile_packages.items.portable_stock_ticker.LogisticallyLinkedItem.isTuned;

public class PortableStockTickerMenu extends AbstractContainerMenu {
    public PortableStockTicker portableStockTicker;
    public Object screenReference;
    public Player player;

    public PortableStockTickerMenu(int id, Inventory playerInventory) {
        super(CMPMenuTypes.PORTABLE_STOCK_TICKER_MENU.get(), id);
        ItemStack stack = PortableStockTicker.find(playerInventory);
        if (stack != null && stack.getItem() instanceof PortableStockTicker pst) {
            if (!isTuned(stack)) {
                playerInventory.player.displayClientMessage(Component.translatable("item.create_mobile_packages.portable_stock_ticker.not_linked"), true);
            }
            this.portableStockTicker = pst;
        }
        this.player = playerInventory.player;
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
