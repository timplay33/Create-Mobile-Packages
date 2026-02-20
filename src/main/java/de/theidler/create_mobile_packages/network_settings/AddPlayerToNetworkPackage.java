package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.UUID;

public class AddPlayerToNetworkPackage extends SimplePacketBase {

    private final UUID playerId;
    private final UUID networkId;

    public AddPlayerToNetworkPackage(UUID playerId, UUID networkId) {
        this.playerId = playerId;
        this.networkId = networkId;
    }

    public AddPlayerToNetworkPackage(FriendlyByteBuf buffer) {
        this.playerId = buffer.readUUID();
        this.networkId = buffer.readUUID();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerId);
        buffer.writeUUID(networkId);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
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
            CreateMobilePackages.LOGGER.debug("Added player {} to network", playerId);

            // Mark as dirty to persist
            Create.LOGISTICS.markDirty();

            // Send updated network data back to client
            NetworkDataPacket responsePacket = new NetworkDataPacket(
                    networkId,
                    network.owner,
                    network.locked,
                    extendedNetwork.create_mobile_packages$getName(),
                    new ArrayList<>(extendedNetwork.create_mobile_packages$getPlayers())
            );
            CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), responsePacket);
        });
        return true;
    }
}


