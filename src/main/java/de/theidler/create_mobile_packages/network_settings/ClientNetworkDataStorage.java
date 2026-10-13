package de.theidler.create_mobile_packages.network_settings;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ClientNetworkDataStorage {

    private static final Map<UUID, NetworkData> networks = new HashMap<>();
    private static final Map<UUID, String> errorMessages = new HashMap<>();
    private static int updateCount = 0;

    public static void updateNetworkData(UUID networkId, UUID owner, boolean locked, String name, List<UUID> players, boolean isOwnerMember) {
        networks.put(networkId, new NetworkData(owner, locked, name, players, isOwnerMember));
        errorMessages.remove(networkId); // Clear error if new data arrives
        updateCount++;
    }

    public static int getUpdateCount() {
        return updateCount;
    }

    public static NetworkData getNetworkData(UUID networkId) {
        return networks.get(networkId);
    }

    public static Map<UUID, NetworkData> getNetworks() {
        return networks;
    }

    public static void setErrorMessage(UUID networkId, String errorMessage) {
        errorMessages.put(networkId, errorMessage);
        networks.remove(networkId); // Remove any old data
        updateCount++;
    }

    public static String getErrorMessage(UUID networkId) {
        return errorMessages.get(networkId);
    }

    public static boolean hasError(UUID networkId) {
        return errorMessages.containsKey(networkId);
    }

    public static void removeNetwork(UUID networkId) {
        networks.remove(networkId);
        errorMessages.remove(networkId);
        updateCount++;
    }

    public static void clear() {
        networks.clear();
        errorMessages.clear();
        updateCount++;
    }

    public static class NetworkData {
        public final UUID owner;
        public final List<UUID> players;
        public boolean locked;
        public String name;
        public boolean isOwnerMember;

        public NetworkData(UUID owner, boolean locked, String name, List<UUID> players, boolean isOwnerMember) {
            this.owner = owner;
            this.locked = locked;
            this.name = name;
            this.players = new ArrayList<>(players);
            this.isOwnerMember = isOwnerMember;
        }

        public void addPlayer(UUID playerId) {
            if (!players.contains(playerId)) {
                players.add(playerId);
            }
        }

        public void removePlayer(UUID playerId) {
            players.remove(playerId);
        }
    }
}
