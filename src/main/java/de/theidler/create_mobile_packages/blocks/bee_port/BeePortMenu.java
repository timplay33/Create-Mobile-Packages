package de.theidler.create_mobile_packages.blocks.bee_port;

import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class BeePortMenu extends PackagePortMenu {

    public BeePortMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public BeePortMenu(MenuType<?> type, int id, Inventory inv, BeePortBlockEntity beePortBlockEntity) {
        super(type, id, inv, beePortBlockEntity);
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
            addSlot(new SlotItemHandler(beePortBlockEntity.getRoboBeeInventory(), 0, 10, 58));
        }

    }
}
