package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RequestPlayerNetworksPacket implements ServerboundPacketPayload {

    public static final RequestPlayerNetworksPacket INSTANCE = new RequestPlayerNetworksPacket();
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestPlayerNetworksPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public RequestPlayerNetworksPacket() {
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player == null) return;

        UUID playerUuid = player.getUUID();
        List<UUID> networkIds = new ArrayList<>();

        for (LogisticsNetwork network : Create.LOGISTICS.logisticsNetworks.values()) {
            IExtendedLogisticsNetwork extended = NetworkHelper.getExtendedLogisticsNetwork(network);
            if (extended != null && (extended.create_mobile_packages$getPlayers().contains(playerUuid) || (network.owner != null && network.owner.equals(playerUuid)))) {

                String name = extended.create_mobile_packages$getName();
                List<UUID> players = new ArrayList<>(extended.create_mobile_packages$getPlayers());
                boolean isOwnerMember = extended.create_mobile_packages$isOwnerMember();

                networkIds.add(network.id);

                // Send data for this network
                NetworkDataPacket packet = new NetworkDataPacket(
                        network.id,
                        network.owner,
                        network.locked,
                        name,
                        players,
                        isOwnerMember
                );
                CatnipServices.NETWORK.sendToClient(player, packet);
            }
        }

        // Send a packet to clear old networks not in this list
        CatnipServices.NETWORK.sendToClient(player, new ClearNetworksPacket(networkIds));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.REQUEST_PLAYER_NETWORKS;
    }
}
