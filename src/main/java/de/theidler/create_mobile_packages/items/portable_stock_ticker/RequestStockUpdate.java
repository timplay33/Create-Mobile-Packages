package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static de.theidler.create_mobile_packages.items.portable_stock_ticker.StockCheckingItem.getAccurateSummary;

public class RequestStockUpdate implements ServerboundPacketPayload {
    public static final RequestStockUpdate INSTANCE = new RequestStockUpdate();
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestStockUpdate> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public RequestStockUpdate() {
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player != null) {
            ItemStack stack = PortableStockTicker.find(player.getInventory());
            if (stack == null || stack.isEmpty()) return;

            GenericStackListPacket responsePacket = new GenericStackListPacket(getAccurateSummary(stack).get());
            CatnipServices.NETWORK.sendToClient(player, responsePacket);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.REQUEST_STOCK_UPDATE;
    }
}
