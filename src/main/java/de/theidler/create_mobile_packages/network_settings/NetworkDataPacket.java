package de.theidler.create_mobile_packages.network_settings;

import de.theidler.create_mobile_packages.index.CMPPackets;
import de.theidler.create_mobile_packages.robo.ClientPlayerNameCache;
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

import java.util.*;

public class NetworkDataPacket implements ClientboundPacketPayload {

    private static final StreamCodec<RegistryFriendlyByteBuf, Map<UUID, String>> PLAYER_NAMES_CODEC = StreamCodec.of(
            (buf, map) -> {
                buf.writeVarInt(map.size());
                for (Map.Entry<UUID, String> entry : map.entrySet()) {
                    UUIDUtil.STREAM_CODEC.encode(buf, entry.getKey());
                    ByteBufCodecs.STRING_UTF8.encode(buf, entry.getValue());
                }
            },
            buf -> {
                int size = buf.readVarInt();
                Map<UUID, String> map = new HashMap<>(size);
                for (int i = 0; i < size; i++) {
                    map.put(UUIDUtil.STREAM_CODEC.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf));
                }
                return map;
            }
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkDataPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NetworkDataPacket decode(RegistryFriendlyByteBuf buf) {
            UUID networkId = UUIDUtil.STREAM_CODEC.decode(buf);
            Optional<UUID> owner = ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).decode(buf);
            boolean locked = ByteBufCodecs.BOOL.decode(buf);
            String name = ByteBufCodecs.STRING_UTF8.decode(buf);
            List<UUID> players = CatnipStreamCodecBuilders.list(UUIDUtil.STREAM_CODEC).decode(buf);
            boolean isOwnerMember = ByteBufCodecs.BOOL.decode(buf);
            Map<UUID, String> playerNames = PLAYER_NAMES_CODEC.decode(buf);
            return new NetworkDataPacket(networkId, owner.orElse(null), locked, name, players, isOwnerMember, playerNames);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, NetworkDataPacket packet) {
            UUIDUtil.STREAM_CODEC.encode(buf, packet.networkId);
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).encode(buf, Optional.ofNullable(packet.owner));
            ByteBufCodecs.BOOL.encode(buf, packet.locked);
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.name);
            CatnipStreamCodecBuilders.list(UUIDUtil.STREAM_CODEC).encode(buf, packet.players);
            ByteBufCodecs.BOOL.encode(buf, packet.isOwnerMember);
            PLAYER_NAMES_CODEC.encode(buf, packet.playerNames);
        }
    };

    private final UUID networkId;
    private final UUID owner; // Can be null for error packets
    private final boolean locked;
    private final String name;
    private final List<UUID> players;
    private final boolean isOwnerMember;
    private final Map<UUID, String> playerNames;
    private final boolean isError;

    public NetworkDataPacket(UUID networkId, UUID owner, boolean locked, String name, List<UUID> players, boolean isOwnerMember, Map<UUID, String> playerNames) {
        this.networkId = networkId;
        this.owner = owner;
        this.locked = locked;
        this.name = name;
        this.players = players;
        this.isOwnerMember = isOwnerMember;
        this.playerNames = playerNames;
        this.isError = name != null && name.startsWith("ERROR:");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        if (isError) {
            ClientNetworkDataStorage.setErrorMessage(networkId, name);
        } else {
            ClientPlayerNameCache.updatePlayerNames(playerNames);

            // Check if the network is locked or owner or part
            boolean isMember = players.contains(Minecraft.getInstance().player.getUUID()) || isOwnerMember;
            if (!locked || (Minecraft.getInstance().player != null &&
                    (Minecraft.getInstance().player.getUUID().equals(owner) || isMember))) {
                // Player is in the network, store the data
                ClientNetworkDataStorage.updateNetworkData(networkId, owner, locked, name, players, isOwnerMember);
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
