package de.theidler.create_mobile_packages.items.drone_controller;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

import static de.theidler.create_mobile_packages.items.drone_controller.StockCheckingItem.getAccurateSummary;

public class RequestStockUpdate implements ServerboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestStockUpdate> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, packet -> packet.networkId,
            RequestStockUpdate::new
    );
    private UUID networkId;

    public RequestStockUpdate(UUID networkId) {
        this.networkId = networkId;
    }

    public UUID getNetworkId() {
        return networkId;
    }
    public RequestStockUpdate() {
    }


    @Override
    public void handle(ServerPlayer player) {
        if (player != null) {
            BigItemStackListPacket responsePacket = new BigItemStackListPacket(getAccurateSummary(player.getMainHandItem()).getStacks());
            CatnipServices.NETWORK.sendToClient(player, responsePacket);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.REQUEST_STOCK_UPDATE;
    }
}
