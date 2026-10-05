package de.theidler.create_mobile_packages.robo;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerNameCache {
    private static final Map<UUID, String> playerNamesByUUID = new HashMap<>();

    public static void updatePlayerNames(Map<UUID, String> incomingNames) {
        playerNamesByUUID.putAll(incomingNames);
    }

    public static String getPlayerName(UUID playerUUID) {
        return playerNamesByUUID.get(playerUUID);
    }

    public static void clear() {
        playerNamesByUUID.clear();
    }
}
