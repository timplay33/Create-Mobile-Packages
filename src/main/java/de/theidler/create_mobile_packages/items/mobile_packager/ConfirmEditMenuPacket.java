package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ConfirmEditMenuPacket extends SimplePacketBase {

    private final String address;

    public ConfirmEditMenuPacket(String address) {
        this.address = address;
    }

    public ConfirmEditMenuPacket(FriendlyByteBuf buffer) {
        this.address = buffer.readUtf(25); // max length of the Address box
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(address, 25); // max length of the Address box
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.isAlive()) return;

            if (player.containerMenu instanceof MobilePackagerEditMenu menu) {
                menu.serverConfirm(address);
            }
        });
        return true;
    }
}
