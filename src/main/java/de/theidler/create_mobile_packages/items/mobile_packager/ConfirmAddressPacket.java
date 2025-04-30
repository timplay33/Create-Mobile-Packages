package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ConfirmAddressPacket extends SimplePacketBase {
    private final String value;

    public ConfirmAddressPacket(String value) {
        this.value = value;
    }

    public ConfirmAddressPacket(FriendlyByteBuf buffer) {
        this.value = buffer.readUtf();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(value);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player.containerMenu instanceof MobilePackagerMenu menu) {
                menu.serverConfirm(value);
            }
        });
        return true;
    }
}
