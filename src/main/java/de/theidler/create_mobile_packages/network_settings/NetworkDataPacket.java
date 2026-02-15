package de.theidler.create_mobile_packages.network_settings;

import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NetworkDataPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkDataPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, packet -> packet.networkId,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), packet -> Optional.ofNullable(packet.owner),
            ByteBufCodecs.BOOL, packet -> packet.locked,
            ByteBufCodecs.STRING_UTF8, packet -> packet.name,
            CatnipStreamCodecBuilders.list(UUIDUtil.STREAM_CODEC), packet -> packet.players,
            (networkId, owner, locked, name, players) -> new NetworkDataPacket(networkId, owner.orElse(null), locked, name, players)
    );

    private final UUID networkId;
    private final UUID owner; // Can be null for error packets
    private final boolean locked;
    private final String name;
    private final List<UUID> players;
    private final boolean isError;

    public NetworkDataPacket(UUID networkId, UUID owner, boolean locked, String name, List<UUID> players) {
        this.networkId = networkId;
        this.owner = owner;
        this.locked = locked;
        this.name = name;
        this.players = players;
        this.isError = name != null && name.startsWith("ERROR:");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        if (isError) {
            ClientNetworkDataStorage.setErrorMessage(networkId, name);
        } else {
            // Check if the network is locked or owner or part
            if (!locked || (Minecraft.getInstance().player != null &&
                    (Minecraft.getInstance().player.getUUID().equals(owner) || players.contains(Minecraft.getInstance().player.getUUID())))) {
                // Player is in the network, store the data
                ClientNetworkDataStorage.updateNetworkData(networkId, owner, locked, name, players);
            } else {
                // Player is not in the network, remove it from the cache
                ClientNetworkDataStorage.removeNetwork(networkId);
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.NETWORK_DATA;
    }
}
