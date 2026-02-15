package de.theidler.create_mobile_packages.items.mobile_packager;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class ConfirmEditMenuPacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfirmEditMenuPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, packet -> packet.address, ConfirmEditMenuPacket::new
    );
    private final String address;

    public ConfirmEditMenuPacket(String address) {
        this.address = address;
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player == null || !player.isAlive()) return;

        if (player.containerMenu instanceof MobilePackagerEditMenu menu) {
            menu.serverConfirm(address);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.CONFIRM_EDIT_MENU;
    }
}
