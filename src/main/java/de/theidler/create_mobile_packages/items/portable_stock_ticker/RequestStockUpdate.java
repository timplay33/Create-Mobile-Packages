package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.List;

import static de.theidler.create_mobile_packages.items.portable_stock_ticker.StockCheckingItem.getAccurateSummary;

public class RequestStockUpdate extends SimplePacketBase {

    public static final int MAX_ITEMS_PER_PACKET = 500;

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

                List<GenericStack> allStacks = getAccurateSummary(stack).get();

                for (int i = 0; i < allStacks.size(); i += MAX_ITEMS_PER_PACKET) {
                    int end = Math.min(i + MAX_ITEMS_PER_PACKET, allStacks.size());
                    boolean isLast = end == allStacks.size();
                    List<GenericStack> chunk = allStacks.subList(i, end);
                    GenericStackListPacket responsePacket = new GenericStackListPacket(chunk, isLast);
                    CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), responsePacket);
                }
            }
        });
        return true;
    }
}
