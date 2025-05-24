package de.theidler.create_mobile_packages.blocks.bee_portal;

import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBeeStackHandler;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BeePortalMenu extends PackagePortMenu {

    private ContainerData data;

    public BeePortalMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
        if (contentHolder instanceof BeePortBlockEntity beePortBlockEntity) {
            this.data = beePortBlockEntity.getData();
            this.addDataSlots(this.data);
        }
    }

    public BeePortalMenu(MenuType<?> type, int id, Inventory inv, BeePortBlockEntity beePortBlockEntity) {
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

    public static BeePortalMenu create(int id, Inventory inv, BeePortBlockEntity beePortBlockEntity) {
        System.out.println(2);
        return new BeePortalMenu(CMPMenuTypes.BEE_PORT_PORTAL_MENU.get(), id, inv, beePortBlockEntity);
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        if (contentHolder instanceof BeePortBlockEntity beePortBlockEntity) {
            addSlot(new BeePortBeeStackHandler(beePortBlockEntity.getRoboBeeInventory(), 0, 12, 60));
        }
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
