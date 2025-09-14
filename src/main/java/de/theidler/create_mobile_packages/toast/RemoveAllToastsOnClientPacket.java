package de.theidler.create_mobile_packages.toast;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class RemoveAllToastsOnClientPacket extends SimplePacketBase {
    @Override
    public void write(FriendlyByteBuf buffer) {
        
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(this::handleClient);
        context.setPacketHandled(true);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        ToastOverlayRenderer.removeAllToasts();
    }

    public static RemoveAllToastsOnClientPacket read(FriendlyByteBuf ignoredFriendlyByteBuf) {
        return new RemoveAllToastsOnClientPacket();
    }
}
