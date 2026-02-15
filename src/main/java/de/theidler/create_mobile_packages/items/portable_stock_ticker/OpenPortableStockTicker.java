package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

import static de.theidler.create_mobile_packages.items.portable_stock_ticker.LogisticallyLinkedItem.isTuned;

public class OpenPortableStockTicker implements ServerboundPacketPayload {
    public static final OpenPortableStockTicker INSTANCE = new OpenPortableStockTicker();
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPortableStockTicker> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public OpenPortableStockTicker() {
    }

    @Override
    public void handle(ServerPlayer player) {

        ItemStack stack = PortableStockTicker.find(player.getInventory());
        if (stack == null || !(stack.getItem() instanceof PortableStockTicker)) return;

        if (!isTuned(stack)) {
            player.displayClientMessage(Component.translatable("item.create_mobile_packages.portable_stock_ticker.not_linked"), true);
            return;
        }

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new PortableStockTickerMenu(id, inv),
                Component.translatable("item.create_mobile_packages.portable_stock_ticker")
        ));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.OPEN_PORTABLE_STOCK_TICKER;
    }
}
