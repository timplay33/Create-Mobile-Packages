package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NetworkHelper {
    public static @NotNull Set<UUID> getPlayerUUIDs(@Nullable UUID logisticsNetworkId) {
        IExtendedLogisticsNetwork extendedLogisticsNetwork = getExtendedLogisticsNetwork(logisticsNetworkId);
        if (extendedLogisticsNetwork == null) return new HashSet<>();

        Set<UUID> playerUUIDs = new HashSet<>(extendedLogisticsNetwork.create_mobile_packages$getPlayers());

        if (extendedLogisticsNetwork.create_mobile_packages$isOwnerMember()) {
            LogisticsNetwork logisticsNetwork = Create.LOGISTICS.logisticsNetworks.get(logisticsNetworkId);
            playerUUIDs.add(logisticsNetwork.owner);
        }

        return playerUUIDs;
    }

    public static @Nullable IExtendedLogisticsNetwork getExtendedLogisticsNetwork(@Nullable UUID logisticsNetworkId) {
        if (logisticsNetworkId == null) return null;

        LogisticsNetwork logisticsNetwork = Create.LOGISTICS.logisticsNetworks.get(logisticsNetworkId);
        return getExtendedLogisticsNetwork(logisticsNetwork);
    }

    public static @Nullable IExtendedLogisticsNetwork getExtendedLogisticsNetwork(@Nullable LogisticsNetwork logisticsNetwork) {
        if (logisticsNetwork == null) return null;

        if (logisticsNetwork instanceof IExtendedLogisticsNetwork extendedLogisticsNetwork) {
            return extendedLogisticsNetwork;
        }

        CreateMobilePackages.LOGGER.debug("FAILED: Network does not implement IExtendedLogisticsNetwork!");
        return null;
    }
}
