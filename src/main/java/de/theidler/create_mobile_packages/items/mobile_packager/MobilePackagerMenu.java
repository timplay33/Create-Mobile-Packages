package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.gui.menu.MenuBase;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class MobilePackagerMenu extends MenuBase<MobilePackager> {

    private ItemStackHandler packageSlotInventory;
    public boolean confirmed = false;

    public MobilePackagerMenu(int id, Inventory inv, MobilePackager contentHolder) {
        super(CMPMenuTypes.MOBILE_PACKAGER_MENU.get(), id, inv, contentHolder);
    }

    @Override
    protected MobilePackager createOnClient(RegistryFriendlyByteBuf extraData) {
        return null;
    }

    @Override
    public void initAndReadInventory(MobilePackager contentHolder) {
        packageSlotInventory = new ItemStackHandler(1);
    }

    @Override
    public void addSlots() {
        addSlot(new MobilePackagerStackHandler(packageSlotInventory, 0, 74, 28));
        addPlayerSlots(13, 112);
    }

    @Override
    protected void saveData(MobilePackager contentHolder) {
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index == 0) {
                // Custom Slot -> Player Inventory
                if (!this.moveItemStackTo(itemstack1, 1, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player Inventory -> Custom Slot (Index 0)
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(Player playerIn) {
        if (!playerIn.level().isClientSide && !confirmed) {
            playerIn.getInventory().placeItemBackInInventory(packageSlotInventory.getStackInSlot(0));
        }
        super.removed(playerIn);
    }

    public void confirm() {
        if (player.level().isClientSide) {
            ItemStack stack = packageSlotInventory.getStackInSlot(0);
            if (!stack.isEmpty()) {
                CatnipServices.NETWORK.sendToServer(new OpenEditMenuPacket(packageSlotInventory.getStackInSlot(0)));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.getMainHandItem().getItem() instanceof MobilePackager)
            return super.stillValid(player);
        return false;
    }

    static class MobilePackagerStackHandler extends SlotItemHandler {

        public MobilePackagerStackHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return stack.getItem() instanceof PackageItem;
        }
    }
}
