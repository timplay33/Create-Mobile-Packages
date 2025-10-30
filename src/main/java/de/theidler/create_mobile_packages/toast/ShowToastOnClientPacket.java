package de.theidler.create_mobile_packages.toast;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class ShowToastOnClientPacket extends SimplePacketBase {

    private final Toast toast;

    public ShowToastOnClientPacket(Toast toast) {
        this.toast = toast;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        toast.write(buffer);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(this::handleClient);
        context.setPacketHandled(true);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        ToastOverlayRenderer.showToast(toast);
    }

    public static ShowToastOnClientPacket read(FriendlyByteBuf buffer) {
        return new ShowToastOnClientPacket(Toast.read(buffer));
    }
}
