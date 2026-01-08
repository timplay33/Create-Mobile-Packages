package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class ModifyNetworkLockStatePackage extends SimplePacketBase {

    boolean locked;
    UUID networkId;

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
            LogisticsNetwork network = Create.LOGISTICS.logisticsNetworks.get(networkId);
            if (network == null) return;
            network.locked = locked;
        });
        return true;
    }
}
