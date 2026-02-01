package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class ClearNetworksPacket extends SimplePacketBase {

    private final List<UUID> validNetworkIds;

    public ClearNetworksPacket(List<UUID> validNetworkIds) {
        this.validNetworkIds = new ArrayList<>(validNetworkIds);
    }

    public static ClearNetworksPacket read(FriendlyByteBuf buffer) {
        int count = buffer.readInt();
        List<UUID> networkIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            networkIds.add(buffer.readUUID());
        }
        return new ClearNetworksPacket(networkIds);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(validNetworkIds.size());
        for (UUID networkId : validNetworkIds) {
            buffer.writeUUID(networkId);
        }
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(this::handleClient);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient() {
        // Remove networks that are not in the valid list
        var validSet = new HashSet<>(validNetworkIds);
        var networksToRemove = new ArrayList<UUID>();

        for (UUID networkId : ClientNetworkDataStorage.getNetworks().keySet()) {
            if (!validSet.contains(networkId)) {
                networksToRemove.add(networkId);
            }
        }

        for (UUID networkId : networksToRemove) {
            ClientNetworkDataStorage.removeNetwork(networkId);
        }
    }
}
