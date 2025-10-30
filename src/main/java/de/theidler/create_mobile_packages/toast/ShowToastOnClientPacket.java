package de.theidler.create_mobile_packages.toast;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ShowToastOnClientPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ShowToastOnClientPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> CustomToast.STREAM_CODEC.encode(buf, packet.toast),
        buf -> new ShowToastOnClientPacket(CustomToast.STREAM_CODEC.decode(buf))
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
