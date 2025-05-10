package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.gui.menu.MenuBase;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class MobilePackagerMenu extends MenuBase<MobilePackager> {

    public ItemStackHandler packageSlotInventory;
    public ItemStackHandler packageContentsInventory;
    public boolean hasEditMenu = false;
    public boolean firstConfirmClicked = false;

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
        packageSlotInventory = new ItemStackHandler(1);
        packageContentsInventory = getContents();
    }

    @Override
    public void addSlots() {
        int slotX = 27;
        int slotY = 28;
        slots.removeIf(slot -> true);
        if (hasEditMenu) {
            for (int i = 0; i < 9; i++)
                addSlot(new SlotItemHandler(packageContentsInventory, i, slotX + 20 * i, slotY));
            addPlayerSlots(33, 142);
        } else {
            addSlot(new MobilePackagerStackHandler(packageSlotInventory, 0, 74, 28));
            addPlayerSlots(13, 112);
        }
    }

    @Override
    protected void saveData(MobilePackager contentHolder) {
        CreateMobilePackages.LOGGER.info("Saving Data");
        writeContents();
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
        if (Minecraft.getInstance().screen instanceof MobilePackagerEditScreen || !firstConfirmClicked) {
            boolean success = false;
            ItemStack stack = packageSlotInventory.getStackInSlot(0);
            if (!stack.isEmpty()) {
                success= !moveItemStackTo(stack, 0, playerInventory.getContainerSize()-1, false);
            }
            if (!success) {
                ItemEntity itemEntity = new ItemEntity(playerIn.level(), playerIn.getX(), playerIn.getY(), playerIn.getZ(), stack);
                itemEntity.setPickUpDelay(10);
                itemEntity.setDeltaMovement(0, 0, 0);
                playerIn.level().addFreshEntity(itemEntity);
            }
            super.removed(playerIn);
        }
    }

    public String getAddress() {
        ItemStack stack = packageSlotInventory.getStackInSlot(0);
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
        ItemStack stack = packageSlotInventory.getStackInSlot(0);
        if (PackageItem.isPackage(stack)) {
            PackageItem.addAddress(stack, value);
        } else {
            ItemStack newStack = PackageItem.containing(packageContentsInventory);
            PackageItem.addAddress(newStack, value);
            packageSlotInventory.setStackInSlot(0, newStack);
            for (int i = 0; i < packageContentsInventory.getSlots(); i++) {
                packageContentsInventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

    }

    public ItemStackHandler getContents() {
        ItemStack stack = packageSlotInventory.getStackInSlot(0);
        if (PackageItem.isPackage(stack)){
            return PackageItem.getContents(stack);
        }
        return new ItemStackHandler(9);
    }

    public void writeContents() {
        if (player.level().isClientSide) {
            CreateMobilePackages.LOGGER.info("Clientside WriteContentsPacket");
            return;
            //CMPPackets.getChannel().sendToServer(new WriteContentsPacket());
        }
        CreateMobilePackages.LOGGER.info("Serverside WriteContentsPacket");
        ItemStack stack = packageSlotInventory.getStackInSlot(0);
        if (PackageItem.isPackage(stack)){
            CompoundTag nbt = new CompoundTag();
            nbt.put("Items", packageContentsInventory.serializeNBT());
            stack.setTag(nbt);
        }
    }

    static class MobilePackagerStackHandler extends SlotItemHandler {

        public MobilePackagerStackHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            if (PackageItem.isPackage(stack)) {
                return super.mayPlace(stack);
            }
            return false;
        }
    }
}
