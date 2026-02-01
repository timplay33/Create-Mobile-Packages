package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

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
        });
        return true;
    }
}
