package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import static de.theidler.create_mobile_packages.items.portable_stock_ticker.StockCheckingItem.getAccurateSummary;

public class RequestStockUpdate extends SimplePacketBase {

    public RequestStockUpdate() {
    }

    public RequestStockUpdate(FriendlyByteBuf buffer) {
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack stack = PortableStockTicker.find(player.getInventory());
                if (stack == null || stack.isEmpty()) return;
                BigItemStackListPacket responsePacket = new BigItemStackListPacket(getAccurateSummary(stack).getStacks());
                CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), responsePacket);
            }
        });
        return true;
    }
}
