package de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu;

import de.theidler.create_mobile_packages.index.CMPPackets;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTicker;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public class OpenTrashMenuPacket implements ServerboundPacketPayload {
    public static final OpenTrashMenuPacket INSTANCE = new OpenTrashMenuPacket();
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTrashMenuPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void handle(ServerPlayer player) {
        if (player == null || !player.isAlive()) return;

        player.closeContainer();
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new TrashMenu(id, inv, (PortableStockTicker) PortableStockTicker.find(inv).getItem()),
                net.minecraft.network.chat.Component.translatable("item.create_mobile_packages.portable_stock_ticker.trash_menu")
        ), buf -> {
            // No additional data to write
        });
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.OPEN_TRASH_MENU;
    }
}
