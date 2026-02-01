package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NetworkDataPacket extends SimplePacketBase {

    private final UUID networkId;
    private final UUID owner; // Can be null for error packets
    private final boolean locked;
    private final String name;
    private final List<UUID> players;
    private final boolean isError;

    public NetworkDataPacket(UUID networkId, UUID owner, boolean locked, String name, List<UUID> players) {
        this.networkId = networkId;
        this.owner = owner;
        this.locked = locked;
        this.name = name;
        this.players = players;
        this.isError = name != null && name.startsWith("ERROR:");
    }

    public static NetworkDataPacket read(FriendlyByteBuf buffer) {
        UUID networkId = buffer.readUUID();
        boolean hasOwner = buffer.readBoolean();
        UUID owner = hasOwner ? buffer.readUUID() : null;
        boolean locked = buffer.readBoolean();
        String name = buffer.readUtf();
        int playerCount = buffer.readInt();
        List<UUID> players = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            players.add(buffer.readUUID());
        }
        return new NetworkDataPacket(networkId, owner, locked, name, players);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(networkId);
        buffer.writeBoolean(owner != null);
        if (owner != null) {
            buffer.writeUUID(owner);
        }
        buffer.writeBoolean(locked);
        buffer.writeUtf(name);
        buffer.writeInt(players.size());
        for (UUID player : players) {
            buffer.writeUUID(player);
        }
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(this::handleClient);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient() {
        if (isError) {
            ClientNetworkDataStorage.setErrorMessage(networkId, name);
        } else {
            // Check if the network is locked
            if (locked) {
                // Player is in the network, store the data
                ClientNetworkDataStorage.updateNetworkData(networkId, owner, locked, name, players);
            } else {
                // Player is not in the network, remove it from the cache
                ClientNetworkDataStorage.removeNetwork(networkId);
            }
        }
    }
}
