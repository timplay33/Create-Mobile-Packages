package de.theidler.create_mobile_packages.robo;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerNameCache extends SavedData {

    private final Map<UUID, String> playerNamesByUUID = new HashMap<>();
    private final Map<String, UUID> playerUUIDsByNames = new HashMap<>();

    private ServerLevel serverLevel;


    public static PlayerNameCache get(ServerLevel level) {
        PlayerNameCache cache = level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        PlayerNameCache::new,
                        PlayerNameCache::load
                ),
                "create_mobile_packages_player_name_cache"
        );
        cache.serverLevel = level;
        return cache;
    }


    /**
     * Loads the playerNameByUUID map from the given tag.
     * Creates the playerUUIDsByNames map from the playerNameByUUID map.
     */
    public static PlayerNameCache load(CompoundTag tag, HolderLookup.Provider registries) {
        PlayerNameCache data = new PlayerNameCache();

        ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);

        for (Tag t : list) {
            CompoundTag entry = (CompoundTag) t;
            UUID uuid = entry.getUUID("uuid");
            String value = entry.getString("value");
            data.playerNamesByUUID.put(uuid, value);
            data.playerUUIDsByNames.put(value, uuid);
        }

        return data;
    }

    public void addPlayerName(UUID playerUUID, String playerName) {
        playerNamesByUUID.put(playerUUID, playerName);
        playerUUIDsByNames.put(playerName, playerUUID);
        setDirty();
    }

    public String getPlayerName(UUID playerUUID) {
        return playerNamesByUUID.get(playerUUID);
    }

    public UUID getPlayerUUID(String playerName) {
        return playerUUIDsByNames.get(playerName);
    }

    /**
     * Gets the player with the given UUID from the servers {@link net.minecraft.server.players.PlayerList}
     *
     * @param playerUUID UUID of the player to get
     * @return the player with the given UUID or empty if the player is not online
     */
    public Optional<ServerPlayer> getPlayer(UUID playerUUID) {
        return Optional.ofNullable(serverLevel.getServer().getPlayerList().getPlayer(playerUUID));
    }

    /**
     * Checks if the player with the given UUID is online by checking if the player is present in the servers {@link net.minecraft.server.players.PlayerList}
     *
     * @param playerUUID UUID of the player to check
     * @return true if the player is online, false otherwise
     */
    public boolean isPlayerOnline(UUID playerUUID) {
        return getPlayer(playerUUID).isPresent();
    }

    /**
     * Tries to match the given address to a player name.
     *
     * @param address address to match to a player name
     * @return the player name that was found or empty if no match was found
     */
    public Optional<String> matchPlayerNameToAddress(String address) {
        return playerUUIDsByNames.keySet().stream()
                .filter(playerName -> doesAddressMatchPlayerName(address, playerName))
                .findFirst();
    }

    /**
     * Checks if the given address matches the given player name.
     * Removes the '@' from the address if present.
     *
     * @param address    address to check against the player name
     * @param playerName the player name to check against the address
     * @return true if the address matches the player name, false otherwise
     */
    private boolean doesAddressMatchPlayerName(String address, String playerName) {
        if (address == null) return false;
        if (playerName == null) return false;
        int atIndex = address.lastIndexOf('@');
        if (atIndex == -1) {
            return address.equals(playerName);
        }
        return address.substring(atIndex + 1).equals(playerName);
    }

    /**
     * Builds a UUID → name map for the given player UUIDs.
     */
    public Map<UUID, String> buildNamesMap(List<UUID> playerUUIDs) {
        Map<UUID, String> result = new HashMap<>();
        for (UUID uuid : playerUUIDs) {
            String name = playerNamesByUUID.get(uuid);
            if (name != null) {
                result.put(uuid, name);
            }
        }
        return result;
    }

    /**
     * Saves the playerNameByUUID map to the given tag.
     * The playerUUIDsByNames map is not saved, as it includes the same information.
     * The playerUUIDsByNames is rebuilt on load.
     */
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        ListTag list = new ListTag();

        for (Map.Entry<UUID, String> entry : playerNamesByUUID.entrySet()) {
            CompoundTag nbt = new CompoundTag();
            nbt.putUUID("uuid", entry.getKey());
            nbt.putString("value", entry.getValue());
            list.add(nbt);
        }

        tag.put("entries", list);
        return tag;
    }
}
