package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RequestPlayerNetworksPacket extends SimplePacketBase {

    public RequestPlayerNetworksPacket() {
    }

    public RequestPlayerNetworksPacket(FriendlyByteBuf buffer) {
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            UUID playerUuid = player.getUUID();
            List<UUID> networkIds = new ArrayList<>();

            for (LogisticsNetwork network : Create.LOGISTICS.logisticsNetworks.values()) {
                IExtendedLogisticsNetwork extended = NetworkHelper.getExtendedLogisticsNetwork(network);
                if (extended != null && extended.create_mobile_packages$getPlayers().contains(playerUuid)) {

                    String name = extended.create_mobile_packages$getName();
                    List<UUID> players = new ArrayList<>(extended.create_mobile_packages$getPlayers());

                    networkIds.add(network.id);

                    // Send data for this network
                    NetworkDataPacket packet = new NetworkDataPacket(
                            network.id,
                            network.owner,
                            network.locked,
                            name,
                            players
                    );
                    CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), packet);
                }
            }

            // Send a packet to clear old networks not in this list
            CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), new ClearNetworksPacket(networkIds));
        });
        return true;
    }
}
