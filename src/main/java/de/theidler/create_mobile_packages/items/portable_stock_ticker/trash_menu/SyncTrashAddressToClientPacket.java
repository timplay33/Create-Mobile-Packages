package de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SyncTrashAddressToClientPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTrashAddressToClientPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, packet -> packet.address,
            SyncTrashAddressToClientPacket::new
    );

    private final String address;

    public SyncTrashAddressToClientPacket(String address) {
        this.address = address;
    }

    @Override
    public void handle(LocalPlayer player) {
        if (player == null) return;

        if (player.containerMenu instanceof TrashMenu trashMenu) {
            trashMenu.setTargetAddress(address);
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof TrashScreen trashScreen) {
            trashScreen.applyTargetAddress(address);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.SYNC_TRASH_ADDRESS_TO_CLIENT;
    }
}

