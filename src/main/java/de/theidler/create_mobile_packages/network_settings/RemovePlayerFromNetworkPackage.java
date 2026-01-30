package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import net.minecraft.network.FriendlyByteBuf;
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
            IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(networkId);
            if (extendedNetwork == null) return;
            extendedNetwork.create_mobile_packages$removePlayer(playerId);
        });
        return true;
    }
}
