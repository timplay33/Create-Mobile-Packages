package de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SyncTrashItemsToClientPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTrashItemsToClientPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC), packet -> packet.items,
            SyncTrashItemsToClientPacket::new
    );

    private final List<ItemStack> items;

    public SyncTrashItemsToClientPacket(List<ItemStack> items) {
        this.items = items;
    }

    @Override
    public void handle(LocalPlayer player) {
        if (player == null) return;

        if (player.containerMenu instanceof TrashMenu trashMenu) {
            // Update the trash inventory with the new items
            trashMenu.updateTrashInventory(items);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.SYNC_TRASH_ITEMS_TO_CLIENT;
    }
}

