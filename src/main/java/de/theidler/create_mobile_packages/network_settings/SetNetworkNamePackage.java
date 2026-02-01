package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class SetNetworkNamePackage extends SimplePacketBase {

    private final String name;
    private final UUID networkId;

    public SetNetworkNamePackage(FriendlyByteBuf buffer) {
        this.name = buffer.readUtf(32767);
        this.networkId = buffer.readUUID();
    }

    public SetNetworkNamePackage(String name, UUID networkId) {
        this.name = name;
        this.networkId = networkId;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(name, 32767);
        buffer.writeUUID(networkId);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            LogisticsNetwork logisticsNetwork = Create.LOGISTICS.logisticsNetworks.get(networkId);
            IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(logisticsNetwork);
            if (logisticsNetwork == null || extendedNetwork == null) return;

            // validate ownership. Only the owner can change the network name
            if (player == null || !logisticsNetwork.owner.equals(player.getUUID())) return;

            extendedNetwork.create_mobile_packages$setName(name);
       });
        return true;
    }
}
