package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static de.theidler.create_mobile_packages.items.portable_stock_ticker.LogisticallyLinkedItem.isTuned;

import net.minecraft.world.inventory.Slot;

public class PortableStockTickerMenu extends AbstractContainerMenu {
    public PortableStockTicker portableStockTicker;
    public Object screenReference;
    public Player player;
    public Inventory playerInventory;

    public PortableStockTickerMenu(int id, Inventory playerInventory) {
        super(CMPMenuTypes.PORTABLE_STOCK_TICKER_MENU.get(), id);
        this.playerInventory = playerInventory;
        ItemStack stack = PortableStockTicker.find(playerInventory);
        if (stack != null && stack.getItem() instanceof PortableStockTicker pst) {
            if (!isTuned(stack)) {
                playerInventory.player.displayClientMessage(
                        Component.translatable("item.create_mobile_packages.portable_stock_ticker.not_linked"), true);
            }
            this.portableStockTicker = pst;
        }
        this.player = playerInventory.player;
        addPlayerSlots(-1000, 0);
    }

    protected void addPlayerSlots(int x, int y) {
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
            this.addSlot(new Slot(playerInventory, hotbarSlot, x + hotbarSlot * 18, y + 58));
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
