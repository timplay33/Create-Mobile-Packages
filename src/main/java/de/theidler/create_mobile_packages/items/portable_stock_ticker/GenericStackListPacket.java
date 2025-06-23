package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class GenericStackListPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, GenericStackListPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(GenericStack.STREAM_CODEC), packet -> packet.stacks,
            GenericStackListPacket::new
    );

    private final List<GenericStack> stacks;

    // Standard constructor
    public GenericStackListPacket(List<GenericStack> stacks) {
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
