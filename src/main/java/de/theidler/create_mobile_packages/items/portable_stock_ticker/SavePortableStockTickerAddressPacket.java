package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class SavePortableStockTickerAddressPacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, SavePortableStockTickerAddressPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, packet -> packet.address,
            SavePortableStockTickerAddressPacket::new
    );

    private final String address;

    public SavePortableStockTickerAddressPacket(String address) {
        this.address = address;
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player == null) return;

        ItemStack pstStack = PortableStockTicker.find(player.getInventory());
        if (pstStack == null) return;

        PortableStockTicker pst = (PortableStockTicker) pstStack.getItem();
        pst.saveAddressToStack(pstStack, address);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.SAVE_PORTABLE_STOCK_TICKER_ADDRESS;
    }
}

