package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RemovePlayerFromNetworkPackage implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, RemovePlayerFromNetworkPackage> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, packet -> packet.playerId,
            UUIDUtil.STREAM_CODEC, packet -> packet.networkId,
            RemovePlayerFromNetworkPackage::new
    );

    private final UUID playerId;
    private final UUID networkId;

    public RemovePlayerFromNetworkPackage(UUID playerId, UUID networkId) {
        this.playerId = playerId;
        this.networkId = networkId;
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player == null) return;

        LogisticsNetwork logisticsNetwork = Create.LOGISTICS.logisticsNetworks.get(networkId);
        if (logisticsNetwork == null) return;

        // The Owner can remove players from the network
        // Players can only remove themselves from the network
        if ((logisticsNetwork.owner == null || !logisticsNetwork.owner.equals(player.getUUID()))
                && !playerId.equals(player.getUUID())) return;

        // Get the extended network
        IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(logisticsNetwork);
        if (extendedNetwork == null) return;

        extendedNetwork.create_mobile_packages$removePlayer(playerId);

        // Mark as dirty to persist
        Create.LOGISTICS.markDirty();

        // Send updated network data back to client
        List<UUID> updatedPlayers = new ArrayList<>(extendedNetwork.create_mobile_packages$getPlayers());
        Map<UUID, String> playerNames = NetworkHelper.buildNetworkPlayerNames(player.serverLevel(), updatedPlayers, logisticsNetwork.owner);
        NetworkDataPacket responsePacket = new NetworkDataPacket(
                networkId,
                logisticsNetwork.owner,
                logisticsNetwork.locked,
                extendedNetwork.create_mobile_packages$getName(),
                updatedPlayers,
                extendedNetwork.create_mobile_packages$isOwnerMember(),
                playerNames
        );
        CatnipServices.NETWORK.sendToClient(player, responsePacket);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.REMOVE_PLAYER_FROM_NETWORK;
    }
}
