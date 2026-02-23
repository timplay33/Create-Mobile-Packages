package de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu;

import de.theidler.create_mobile_packages.index.CMPPackets;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.LogisticallyLinkedItem;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTicker;
import de.theidler.create_mobile_packages.robo.RoboManager;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class SyncTrashAddressPacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTrashAddressPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, packet -> packet.address,
            SyncTrashAddressPacket::new
    );

    private final String address;

    public SyncTrashAddressPacket(String address) {
        this.address = address;
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player == null || !(player.level() instanceof ServerLevel serverLevel)) return;

        ItemStack pstItem = PortableStockTicker.find(player.getInventory());
        if (pstItem == null) return;

        UUID networkId = LogisticallyLinkedItem.networkFromStack(pstItem);
        if (networkId == null) return;

        RoboManager roboManager = RoboManager.get(serverLevel);
        roboManager.setTrashTargetAddress(networkId, player.getUUID(), address);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.SYNC_TRASH_ADDRESS;
    }
}
