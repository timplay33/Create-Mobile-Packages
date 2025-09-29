package de.theidler.create_mobile_packages.toast;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class RemoveToastOnClientPacket extends SimplePacketBase {

    private final UUID toastid;

    public RemoveToastOnClientPacket(UUID toastid) {
        this.toastid = toastid;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(toastid);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(this::handleClient);
        context.setPacketHandled(true);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        ToastOverlayRenderer.removeToast(toastid);
    }

    public static RemoveToastOnClientPacket read(FriendlyByteBuf buffer) {
        UUID uuid = buffer.readUUID();
        return new RemoveToastOnClientPacket(uuid);
    }
}
