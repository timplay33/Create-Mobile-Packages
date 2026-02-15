package de.theidler.create_mobile_packages.toast;


import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.UUID;

public class RemoveToastOnClientPacket implements ClientboundPacketPayload {

    public static final StreamCodec<? super RegistryFriendlyByteBuf, RemoveToastOnClientPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, packet -> packet.toastid,
            RemoveToastOnClientPacket::new
    );
    private final UUID toastid;

    public RemoveToastOnClientPacket(UUID toastid) {
        this.toastid = toastid;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        ToastOverlayRenderer.removeToast(toastid);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.REMOVE_TOAST_ON_CLIENT;
    }
}
