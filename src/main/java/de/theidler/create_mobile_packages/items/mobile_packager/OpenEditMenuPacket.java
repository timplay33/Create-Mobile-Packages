package de.theidler.create_mobile_packages.items.mobile_packager;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

public class OpenEditMenuPacket implements ServerboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenEditMenuPacket> STREAM_CODEC = StreamCodec.composite(ItemStack.STREAM_CODEC, packet -> packet.originalPackage, OpenEditMenuPacket::new);

    private final ItemStack originalPackage;

    public OpenEditMenuPacket(ItemStack originalPackage) {
        this.originalPackage = originalPackage;
    }


    @Override
    public void handle(ServerPlayer player) {
        if (player == null || !player.isAlive()) return;

        if (player.containerMenu instanceof MobilePackagerMenu menu) {
            menu.confirmed = true;
        }
        player.closeContainer();
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new MobilePackagerEditMenu(id, inv, new MobilePackagerEdit(), originalPackage),
                Component.translatable("item.create_mobile_packages.mobile_packager")
        ), buf -> ItemStack.STREAM_CODEC.encode(buf, originalPackage));

    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.OPEN_EDIT_MENU;
    }
}
