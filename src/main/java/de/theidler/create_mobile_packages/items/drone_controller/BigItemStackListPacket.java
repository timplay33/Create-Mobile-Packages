package de.theidler.create_mobile_packages.items.drone_controller;

import com.simibubi.create.content.logistics.BigItemStack;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class BigItemStackListPacket implements ClientboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, BigItemStackListPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(BigItemStack.STREAM_CODEC), packet -> packet.stacks,
    BigItemStackListPacket::new
    );

    private final List<BigItemStack> stacks;

    // Standard constructor
    public BigItemStackListPacket(List<BigItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        ClientScreenStorage.stacks = stacks;
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.BIG_ITEM_STACK_LIST;
    }
}
