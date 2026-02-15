package de.theidler.create_mobile_packages.network_settings;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class ClearNetworksPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClearNetworksPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(UUIDUtil.STREAM_CODEC), packet -> packet.validNetworkIds,
            ClearNetworksPacket::new
    );

    private final List<UUID> validNetworkIds;

    public ClearNetworksPacket(List<UUID> validNetworkIds) {
        this.validNetworkIds = new ArrayList<>(validNetworkIds);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        // Remove networks that are not in the valid list
        var validSet = new HashSet<>(validNetworkIds);
        var networksToRemove = new ArrayList<UUID>();

        for (UUID networkId : ClientNetworkDataStorage.getNetworks().keySet()) {
            if (!validSet.contains(networkId)) {
                networksToRemove.add(networkId);
            }
        }

        for (UUID networkId : networksToRemove) {
            ClientNetworkDataStorage.removeNetwork(networkId);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.CLEAR_NETWORKS;
    }
}
