package de.theidler.create_mobile_packages;

import java.util.Set;
import java.util.UUID;

public interface IExtendedLogisticsNetwork {
    Set<UUID> create_mobile_packages$getPlayers();
    void create_mobile_packages$addPlayer(UUID player);
    void create_mobile_packages$removePlayer(UUID player);

    String create_mobile_packages$getName();
    void create_mobile_packages$setName(String name);
}
