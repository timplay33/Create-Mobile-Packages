package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddPlayerToNetworkPackage implements ServerboundPacketPayload {

    public static final StreamCodec<FriendlyByteBuf, AddPlayerToNetworkPackage> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, packet -> packet.playerId,
            UUIDUtil.STREAM_CODEC, packet -> packet.networkId,
            AddPlayerToNetworkPackage::new
    );

    private final UUID playerId;
    private final UUID networkId;

    public AddPlayerToNetworkPackage(UUID playerId, UUID networkId) {
        this.playerId = playerId;
        this.networkId = networkId;
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player == null) return;

        LogisticsNetwork network = Create.LOGISTICS.logisticsNetworks.get(networkId);
        if (network == null) return;

        // Player can only add themselves to the network
        if (!playerId.equals(player.getUUID())) return;

        // Get the extended network
        IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(network);
        if (extendedNetwork == null) {
            CreateMobilePackages.LOGGER.debug("Network is not IExtendedLogisticsNetwork!");
            return;
        }

        extendedNetwork.create_mobile_packages$addPlayer(playerId);

        // Mark as dirty to persist
        Create.LOGISTICS.markDirty();

        // Send updated network data back to client
        List<UUID> updatedPlayers = new ArrayList<>(extendedNetwork.create_mobile_packages$getPlayers());
        Map<UUID, String> playerNames = NetworkHelper.buildNetworkPlayerNames(player.serverLevel(), updatedPlayers, network.owner);
        NetworkDataPacket responsePacket = new NetworkDataPacket(
                networkId,
                network.owner,
                network.locked,
                extendedNetwork.create_mobile_packages$getName(),
                updatedPlayers,
                extendedNetwork.create_mobile_packages$isOwnerMember(),
                playerNames
        );
        CatnipServices.NETWORK.sendToClient(player, responsePacket);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.ADD_PLAYER_TO_NETWORK;
    }
}
