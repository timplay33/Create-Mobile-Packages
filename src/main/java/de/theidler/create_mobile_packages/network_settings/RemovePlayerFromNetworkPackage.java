package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class RemovePlayerFromNetworkPackage extends SimplePacketBase {

    private final UUID playerId;
    private final UUID networkId;

    public RemovePlayerFromNetworkPackage(UUID playerId, UUID networkId) {
        this.playerId = playerId;
        this.networkId = networkId;
    }

    public RemovePlayerFromNetworkPackage(FriendlyByteBuf buffer) {
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

            LogisticsNetwork logisticsNetwork = Create.LOGISTICS.logisticsNetworks.get(networkId);
            IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(logisticsNetwork);
            if (logisticsNetwork == null || extendedNetwork == null) return;

            // The Owner can remove players from the network
            // Players can only remove themselves from the network
            if (!logisticsNetwork.owner.equals(player.getUUID())
                    && !playerId.equals(player.getUUID())) return;

            extendedNetwork.create_mobile_packages$removePlayer(playerId);
        });
        return true;
    }
}
