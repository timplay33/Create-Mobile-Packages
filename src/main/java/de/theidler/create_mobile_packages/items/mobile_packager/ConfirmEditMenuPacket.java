package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;

public class ConfirmEditMenuPacket extends SimplePacketBase {

    private ItemStackHandler contents;
    private String address;

    public ConfirmEditMenuPacket(ItemStackHandler contents, String address) {
        this.contents = contents;
        this.address = address;
    }

    public ConfirmEditMenuPacket(FriendlyByteBuf buffer) {
        this.contents = new ItemStackHandler(9);
        for (int i = 0; i < 9; i++) {
            this.contents.setStackInSlot(i, buffer.readItem());
        }
        this.address = buffer.readUtf(32767); // max length of a string in Minecraft
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        for (int i = 0; i < 9; i++) {
            buffer.writeItem(contents.getStackInSlot(i));
        }
        buffer.writeUtf(address, 32767); // max length of a string in Minecraft
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.isAlive()) return;

            if (player.containerMenu instanceof MobilePackagerEditMenu menu) {
                menu.serverConfirm(contents, address);
            }
        });
        return true;
    }
}
