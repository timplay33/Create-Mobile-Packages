package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

import static de.theidler.create_mobile_packages.items.portable_stock_ticker.StockCheckingItem.getAccurateSummary;

public class RequestStockUpdate implements ServerboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestStockUpdate> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, packet -> packet.networkId,
            RequestStockUpdate::new
    );
    private final UUID networkId;

    public RequestStockUpdate(UUID networkId) {
        if (networkId == null) {
            this.networkId = UUID.randomUUID();
            return;
        }
        this.networkId = networkId;
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player != null) {
            ItemStack stack = PortableStockTicker.find(player.getInventory());
            if (stack == null || stack.isEmpty()) return;
            
            BigItemStackListPacket responsePacket = new BigItemStackListPacket(getAccurateSummary(stack).getStacks());
            CatnipServices.NETWORK.sendToClient(player, responsePacket);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.REQUEST_STOCK_UPDATE;
    }
}
