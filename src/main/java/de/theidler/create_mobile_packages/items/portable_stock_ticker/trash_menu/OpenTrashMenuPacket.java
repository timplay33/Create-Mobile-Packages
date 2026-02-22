package de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu;

import de.theidler.create_mobile_packages.index.CMPPackets;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTicker;
import de.theidler.create_mobile_packages.robo.RoboManager;
import de.theidler.create_mobile_packages.robo.RoboTrashStore;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

import static de.theidler.create_mobile_packages.items.portable_stock_ticker.LogisticallyLinkedItem.networkFromStack;

public class OpenTrashMenuPacket implements ServerboundPacketPayload {
    public static final OpenTrashMenuPacket INSTANCE = new OpenTrashMenuPacket();
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTrashMenuPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void handle(ServerPlayer player) {
        if (player == null || !player.isAlive()) return;

        ItemStack pstItem = PortableStockTicker.find(player.getInventory());
        if (pstItem == null) return;
        PortableStockTicker pst = (PortableStockTicker) pstItem.getItem();
        RoboTrashStore trashStore = RoboManager.get(player.serverLevel()).getTrashStore(networkFromStack(pstItem), player.getUUID());
        String targetAddress = trashStore != null ? trashStore.getTargetAddress() : "";

        player.closeContainer();
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new TrashMenu(id, inv, pst, targetAddress),
                net.minecraft.network.chat.Component.translatable("item.create_mobile_packages.portable_stock_ticker.trash_menu")
        ));

        CatnipServices.NETWORK.sendToClient(player, new SyncTrashAddressToClientPacket(targetAddress));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.OPEN_TRASH_MENU;
    }
}
