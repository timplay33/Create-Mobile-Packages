package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HiddenCategoriesPacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, HiddenCategoriesPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(ByteBufCodecs.INT), packet -> packet.indices,
            HiddenCategoriesPacket::new
    );
    private final List<Integer> indices;

    public HiddenCategoriesPacket(List<Integer> indices) {
        this.indices = indices;
    }

    @Override
    public void handle(ServerPlayer player) {
        int slotIndex = PortableStockTicker.getIndexOfPortableStockTicker(player.getInventory());
        if (slotIndex != -1 && player.getInventory().getItem(slotIndex).getItem() instanceof PortableStockTicker pst) {
            Map<UUID, List<Integer>> hiddenCategories = new HashMap<>();
            hiddenCategories.put(player.getUUID(), indices);
            pst.hiddenCategoriesByPlayer = hiddenCategories;
            pst.saveHiddenCategoriesByPlayerToStack(player.getInventory().getItem(slotIndex), hiddenCategories);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return null;
    }
}
