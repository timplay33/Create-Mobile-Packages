package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

public class HiddenCategoriesPacket extends SimplePacketBase {

    private List<Integer> indices;

    public HiddenCategoriesPacket(List<Integer> indices) {
        this.indices = indices;
    }

    public HiddenCategoriesPacket(FriendlyByteBuf buffer) {
        this.indices = IntStream.of(buffer.readVarIntArray()).boxed().toList();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarIntArray(indices.stream()
                .mapToInt(Integer::intValue)
                .toArray());
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player != null) {
                int slotIndex = PortableStockTicker.getIndexOfPortableStockTicker(player.getInventory());
                if (slotIndex != -1 && player.getInventory().getItem(slotIndex).getItem() instanceof PortableStockTicker pst) {
                    Map<UUID, List<Integer>> hiddenCategories = new HashMap<>();
                    hiddenCategories.put(player.getUUID(), indices);
                    pst.hiddenCategoriesByPlayer = hiddenCategories;
                    pst.saveHiddenCategoriesByPlayerToStack(player.getInventory().getItem(slotIndex), hiddenCategories);
                }
            }
        });
        return true;
    }
}
