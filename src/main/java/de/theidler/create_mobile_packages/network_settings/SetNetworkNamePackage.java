package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import net.minecraft.network.FriendlyByteBuf;
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
            IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(networkId);
            if (extendedNetwork == null) return;
            extendedNetwork.create_mobile_packages$setName(name);
       });
        return true;
    }
}
