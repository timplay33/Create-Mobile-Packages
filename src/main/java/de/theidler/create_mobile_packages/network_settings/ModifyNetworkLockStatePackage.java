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
import java.util.UUID;

public class ModifyNetworkLockStatePackage extends SimplePacketBase {

    private final boolean locked;
    private final UUID networkId;

    public ModifyNetworkLockStatePackage(boolean locked, UUID networkId) {
        this.locked = locked;
        this.networkId = networkId;
    }

    public ModifyNetworkLockStatePackage(FriendlyByteBuf buffer) {
        locked = buffer.readBoolean();
        networkId = buffer.readUUID();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(locked);
        buffer.writeUUID(networkId);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            LogisticsNetwork network = Create.LOGISTICS.logisticsNetworks.get(networkId);
            if (network == null) return;

            // Only the owner can lock/unlock the network
            if (!player.getUUID().equals(network.owner)) return;

            network.locked = locked;
            Create.LOGISTICS.markDirty();

            // Send updated network data back to client
            IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(network);
            if (extendedNetwork != null) {
                NetworkDataPacket responsePacket = new NetworkDataPacket(
                        networkId,
                        network.owner,
                        network.locked,
                        extendedNetwork.create_mobile_packages$getName(),
                        new ArrayList<>(extendedNetwork.create_mobile_packages$getPlayers())
                );
                CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), responsePacket);
            }
        });
        return true;
    }
}
