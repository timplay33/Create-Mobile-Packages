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

import java.util.UUID;

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
        UUID networkId = networkFromStack(pstItem);
        if (networkId == null) return; // This should never happen, but just in case

        RoboTrashStore trashStore = RoboManager.get(player.serverLevel()).getTrashStore(networkId, player.getUUID());
        String targetAddress = trashStore != null ? trashStore.getTargetAddress() : "";

        // Open the menu on server with targetAddress
        // Menu constructor will load inventory from RoboManager automatically
        player.closeContainer();
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new TrashMenu(id, inv, pst, targetAddress),
                net.minecraft.network.chat.Component.translatable("item.create_mobile_packages.portable_stock_ticker.trash_menu")
        ));

        // Sync address to client (redundant but ensures client is aware)
        CatnipServices.NETWORK.sendToClient(player, new SyncTrashAddressToClientPacket(targetAddress));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.OPEN_TRASH_MENU;
    }
}
