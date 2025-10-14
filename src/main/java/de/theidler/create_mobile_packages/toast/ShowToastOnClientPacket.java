package de.theidler.create_mobile_packages.toast;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class ShowToastOnClientPacket extends SimplePacketBase {

    private final CustomToast toast;

    public ShowToastOnClientPacket(CustomToast toast) {
        this.toast = toast;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(toast.uuid);
        buffer.writeUtf(Component.Serializer.toJson(toast.title));
        buffer.writeUtf(Component.Serializer.toJson(toast.subtitle));
        buffer.writeInt(toast.timeout);
        buffer.writeItem(toast.icon);
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
        UUID uuid = buffer.readUUID();
        Component title = Component.Serializer.fromJson(buffer.readUtf());
        Component subtitle = Component.Serializer.fromJson(buffer.readUtf());
        int timeout = buffer.readInt();
        return new ShowToastOnClientPacket(new CustomToast(uuid, title, subtitle, buffer.readItem(), timeout));
    }
}
