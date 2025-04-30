package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.gui.menu.MenuBase;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MobilePackagerMenu extends MenuBase<MobilePackager> {

    public ItemStackHandler proxyInventory;

    public MobilePackagerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(CMPMenuTypes.MOBILE_PACKAGER_MENU.get(), id, inv, extraData);
    }

    public MobilePackagerMenu(int id, Inventory inv, MobilePackager contentHolder) {
        super(CMPMenuTypes.MOBILE_PACKAGER_MENU.get(), id, inv, contentHolder);
    }

    @Override
    public MobilePackager createOnClient(FriendlyByteBuf extraData) {
        return null;
    }

    @Override
    public void initAndReadInventory(MobilePackager contentHolder) {
        proxyInventory = new ItemStackHandler(1);
    }

    @Override
    public void addSlots() {
        addSlot(new SlotItemHandler(proxyInventory, 0, 16, 24));
        addPlayerSlots(18, 106);
    }

    @Override
    public void saveData(MobilePackager contentHolder) {

    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int index) {
        Slot clickedSlot = getSlot(index);
        if (!clickedSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack stack = clickedSlot.getItem();
        int size = 1;
        boolean success = false;
        if (index < size) {
            success = !moveItemStackTo(stack, size, slots.size(), false);
        } else
            success = !moveItemStackTo(stack, 0, size, false);

        return success ? ItemStack.EMPTY : stack;
    }

    @Override
    public void removed(Player playerIn) {
        ItemStack stack = proxyInventory.getStackInSlot(0);
        if (stack.isEmpty()) {
            super.removed(playerIn);
            return;
        }

        if (!playerIn.getInventory().add(stack))
            playerIn.level().addFreshEntity(new ItemEntity(playerIn.level(), playerIn.getX(), playerIn.getY(), playerIn.getZ(), stack));
        super.removed(playerIn);
    }

    public String getAddress() {
        ItemStack stack = proxyInventory.getStackInSlot(0);
        if (PackageItem.isPackage(stack)){
            return PackageItem.getAddress(stack);
        }
        return "";
    }

    public void confirm(String value) {
        if (player.level().isClientSide) {
            CMPPackets.getChannel().sendToServer(new ConfirmAddressPacket(value));
        }
    }

    public void serverConfirm(String value) {
        ItemStack stack = proxyInventory.getStackInSlot(0);
        if (!stack.isEmpty() && PackageItem.isPackage(stack)) {
            PackageItem.addAddress(stack, value);
        }
    }
}
