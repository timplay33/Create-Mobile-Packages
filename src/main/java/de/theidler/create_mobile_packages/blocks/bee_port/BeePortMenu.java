package de.theidler.create_mobile_packages.blocks.bee_port;

import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BeePortMenu extends PackagePortMenu {

    private ContainerData data;

    public BeePortMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
        if (contentHolder instanceof BeePortBlockEntity beePortBlockEntity) {
            this.data = beePortBlockEntity.getData();
            this.addDataSlots(this.data);
        }
    }

    public BeePortMenu(MenuType<?> type, int id, Inventory inv, BeePortBlockEntity beePortBlockEntity) {
        super(type, id, inv, beePortBlockEntity);
        this.data = beePortBlockEntity.getData();
        this.addDataSlots(this.data);
    }

    @Override
    protected BeePortBlockEntity createOnClient(FriendlyByteBuf extraData) {
        BlockPos readBlockPos = extraData.readBlockPos();
        ClientLevel world = Minecraft.getInstance().level;
        BlockEntity blockEntity = world.getBlockEntity(readBlockPos);
        if (blockEntity instanceof BeePortBlockEntity beePortBlockEntity)
            return beePortBlockEntity;
        return null;
    }

    public static BeePortMenu create(int id, Inventory inv, BeePortBlockEntity beePortBlockEntity) {
        return new BeePortMenu(CMPMenuTypes.BEE_PORT_MENU.get(), id, inv, beePortBlockEntity);
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        if (contentHolder instanceof BeePortBlockEntity beePortBlockEntity) {
            addSlot(new BeePortBeeStackHandler(beePortBlockEntity.getRoboBeeInventory(), 0, 12, 60));
        }

    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return super.quickMoveStack(player, index);
        }

        ItemStack stack = slot.getItem();

        // Move from RoboBee-Slot to Player Inventory
        if (index == 54) {
            int originalCount = stack.getCount();
            if (moveItemStackTo(stack, 18, 54, false)) {
                int moved = originalCount - stack.getCount();
                if (stack.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
                ItemStack result = stack.copy();
                result.setCount(moved);
                return result;
            }
        }
        // Move from Player Inventory to RoboBee-Slot
        else if (stack.getItem() == CMPItems.ROBO_BEE.get()) {
            Slot roboBeeSlot = slots.get(54);
            ItemStack targetStack = roboBeeSlot.getItem();

            int maxStackSize = stack.getMaxStackSize();
            int space = maxStackSize - (targetStack.isEmpty() ? 0 : targetStack.getCount());

            if (space > 0) {
                int toMove = Math.min(space, stack.getCount());
                if (targetStack.isEmpty()) {
                    ItemStack moved = stack.copy();
                    moved.setCount(toMove);
                    roboBeeSlot.set(moved);
                } else {
                    targetStack.grow(toMove);
                    roboBeeSlot.setChanged();
                }
                stack.shrink(toMove);
                if (stack.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
                ItemStack result = stack.copy();
                result.setCount(toMove);
                return result;
            }
            return ItemStack.EMPTY;
        }

        return super.quickMoveStack(player, index);
    }

    public int getETA() {
        if (data != null) {
            return data.get(0);
        }
        return Integer.MAX_VALUE;
    }
    public boolean isBeeOnTravel() {
        if (data != null) {
            return data.get(1) == 1;
        }
        return false;
    }
}
