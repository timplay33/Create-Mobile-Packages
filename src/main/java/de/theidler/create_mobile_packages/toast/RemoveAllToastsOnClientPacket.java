package de.theidler.create_mobile_packages.toast;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RemoveAllToastsOnClientPacket implements ClientboundPacketPayload {

    public static final RemoveAllToastsOnClientPacket INSTANCE = new RemoveAllToastsOnClientPacket();
    public static final StreamCodec<? super RegistryFriendlyByteBuf, RemoveAllToastsOnClientPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public RemoveAllToastsOnClientPacket() {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        ToastOverlayRenderer.removeAllToasts();
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.REMOVE_ALL_TOAST_ON_CLIENT;
    }
}
