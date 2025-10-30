package de.theidler.create_mobile_packages.toast;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ShowToastOnClientPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ShowToastOnClientPacket> STREAM_CODEC = StreamCodec.composite(
        Toast.STREAM_CODEC,
        packet -> packet.toast,
        ShowToastOnClientPacket::new
    );

    private final Toast toast;

    public ShowToastOnClientPacket(Toast toast) {
        this.toast = toast;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        ToastOverlayRenderer.showToast(toast);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.SHOW_TOAST_ON_CLIENT;
    }
}
