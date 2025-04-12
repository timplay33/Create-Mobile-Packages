package de.theidler.create_mobile_packages.blocks;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import de.theidler.create_mobile_packages.index.CMPBlocks;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class DronePortMenu extends MenuBase<DronePortBlockEntity> {

    public ItemStackHandler proxyInventory;

    public DronePortMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public static AbstractContainerMenu create(int pContainerId, Inventory pPlayerInventory,
                                               DronePortBlockEntity dronePortBlockEntity) {
        return new DronePortMenu(CMPMenuTypes.DRONE_PORT_MENU.get(), pContainerId, pPlayerInventory,
                dronePortBlockEntity);
    }

    public DronePortMenu(MenuType<?> type, int id, Inventory inv, DronePortBlockEntity contentHolder) {
        super(type, id, inv, contentHolder);
    }

    @Override
    protected DronePortBlockEntity createOnClient(FriendlyByteBuf extraData) {
        BlockPos blockPos = extraData.readBlockPos();
        return CMPBlocks.DRONE_PORT.get().getBlockEntity(Minecraft.getInstance().level, blockPos);
    }

    @Override
    protected void initAndReadInventory(DronePortBlockEntity contentHolder) {
        proxyInventory = contentHolder.getInventory();
    }

    @Override
    protected void addSlots() {
        int x = 16;
        int y = 24;
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new SlotItemHandler(proxyInventory, col + row * 9, x + col * 18, y + row * 18));
        addPlayerSlots(18, 106);
    }

    @Override
    protected void saveData(DronePortBlockEntity contentHolder) {

    }

    @Override
    public boolean stillValid(Player player) {
        return !contentHolder.isRemoved() && player.position()
                .closerThan(Vec3.atCenterOf(contentHolder.getBlockPos()), player.getBlockReach() + 4);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }
}
