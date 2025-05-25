package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class OpenPortableStockTicker extends SimplePacketBase {

    public OpenPortableStockTicker() {
    }

    public OpenPortableStockTicker(FriendlyByteBuf buffer) {
        // No data to read from the buffer
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            NetworkHooks.openScreen(player, new SimpleMenuProvider(
                    (id, inv, ply) -> new PortableStockTickerMenu(id, inv, PortableStockTicker.find(player.getInventory())),
                    Component.translatable("item.create_mobile_packages.portable_stock_ticker")
            ), buf -> {});
        });
        return true;
    }
}
