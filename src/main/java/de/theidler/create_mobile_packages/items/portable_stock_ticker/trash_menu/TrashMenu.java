package de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.LogisticallyLinkedItem;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTicker;
import de.theidler.create_mobile_packages.robo.RoboManager;
import de.theidler.create_mobile_packages.robo.RoboTrashStore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrashMenu extends MenuBase<PortableStockTicker> {

    private ItemStackHandler trashInventory;
    private String targetAddress;

    public TrashMenu(int id, Inventory playerInventory, PortableStockTicker contentHolder) {
        super(CMPMenuTypes.TRASH_MENU.get(), id, playerInventory, contentHolder);
        this.targetAddress = "";
    }

    public TrashMenu(int id, Inventory playerInventory, PortableStockTicker contentHolder, String targetAddress) {
        super(CMPMenuTypes.TRASH_MENU.get(), id, playerInventory, contentHolder);
        this.targetAddress = targetAddress != null ? targetAddress : "";
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);

        if (slot.hasItem()) {
            ItemStack itemStackInSlot = slot.getItem();
            itemStack = itemStackInSlot.copy();

            if (i < trashInventory.getSlots()) {
                // Trash Inventory -> Player Inventory
                if (!this.moveItemStackTo(itemStackInSlot, trashInventory.getSlots(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player Inventory -> Trash Inventory
                if (!this.moveItemStackTo(itemStackInSlot, 0, trashInventory.getSlots(), false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemStackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    protected PortableStockTicker createOnClient(RegistryFriendlyByteBuf extraData) {
        return null;
    }

    @Override
    protected void initAndReadInventory(PortableStockTicker contentHolder) {
        trashInventory = new ItemStackHandler(9);

        // Ensure targetAddress is initialized (may be called before constructor completes)
        if (targetAddress == null) {
            targetAddress = "";
        }

        // Server-side only: Load from RoboManager
        Level level = player.level();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            UUID networkId = getNetworkId();
            if (networkId != null) {
                RoboTrashStore trashStore = RoboManager.get(serverLevel).getTrashStore(networkId, player.getUUID());
                if (trashStore != null) {
                    List<ItemStack> trashSlots = trashStore.getItemStacks();
                    for (int i = 0; i < trashSlots.size() && i < trashInventory.getSlots(); i++) {
                        // Use copies to avoid modifying the original store's items
                        trashInventory.setStackInSlot(i, trashSlots.get(i).copy());
                    }
                    // Only update targetAddress if it wasn't already set (e.g., from constructor parameter)
                    if (targetAddress.isEmpty()) {
                        targetAddress = trashStore.getTargetAddress();
                    }
                }
            }
        }
    }

    private @Nullable UUID getNetworkId() {
        ItemStack stack = PortableStockTicker.find(player.getInventory());
        if (stack != null && stack.getItem() instanceof PortableStockTicker) {
            return LogisticallyLinkedItem.networkFromStack(stack);
        }
        return null;
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(String address) {
        this.targetAddress = address;
    }

    public void updateTrashInventory(List<ItemStack> items) {
        // Update the trash inventory with items from server
        for (int i = 0; i < trashInventory.getSlots(); i++) {
            if (i < items.size() && !items.get(i).isEmpty()) {
                trashInventory.setStackInSlot(i, items.get(i).copy());
            } else {
                trashInventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    private List<ItemStack> toTrashStacks() {
        List<ItemStack> trashStacks = new ArrayList<>();
        for (int i = 0; i < trashInventory.getSlots(); i++) {
            ItemStack stack = trashInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                trashStacks.add(stack);
            }
        }
        return trashStacks;
    }

    @Override
    protected void addSlots() {
        for (int i = 0; i < trashInventory.getSlots(); i++) {
            TrashStackHandler slot = new TrashStackHandler(trashInventory, i, 40 + i * 20, 4);
            slot.setMenu(this);
            addSlot(slot);
        }
        addPlayerSlots(48, 84);
    }

    @Override
    protected void saveData(PortableStockTicker contentHolder) {

    }


    private void saveDataImmediately(ServerLevel serverLevel) {
        UUID networkId = getNetworkId();
        if (networkId != null) {
            RoboManager roboManager = RoboManager.get(serverLevel);
            roboManager.setTrashSlots(serverLevel, networkId, player.getUUID(), toTrashStacks());
        }
    }

    static class TrashStackHandler extends SlotItemHandler {
        private TrashMenu menu;

        public TrashStackHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        public void setMenu(TrashMenu menu) {
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !(stack.getItem() instanceof PortableStockTicker);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            // Only save immediately on server
            if (menu != null) {
                var level = menu.player.level();
                if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                    menu.saveDataImmediately(serverLevel);
                }
            }
        }
    }
}
