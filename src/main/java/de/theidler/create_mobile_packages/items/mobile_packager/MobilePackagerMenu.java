package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.gui.menu.MenuBase;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class MobilePackagerMenu extends MenuBase<MobilePackager> {

    private ItemStackHandler packageSlotInventory;
    public boolean confirmed = false;

    public MobilePackagerMenu(int id, Inventory inv, MobilePackager contentHolder) {
        super(CMPMenuTypes.MOBILE_PACKAGER_MENU.get(), id, inv, contentHolder);
    }

    @Override
    public MobilePackager createOnClient(FriendlyByteBuf extraData) {
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
    public ItemStack quickMoveStack(Player pPlayer, int index) {
        Slot clickedSlot = getSlot(index);
        if (!clickedSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack stack = clickedSlot.getItem();

        boolean success = !moveItemStackTo(stack, 0, slots.size(), false);

        return success ? ItemStack.EMPTY : stack;
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
                CMPPackets.getChannel().sendToServer(new OpenEditMenuPacket(packageSlotInventory.getStackInSlot(0)));
            }
        }
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
