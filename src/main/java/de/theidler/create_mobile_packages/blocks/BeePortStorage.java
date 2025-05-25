package de.theidler.create_mobile_packages.blocks;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BeePortStorage extends SavedData {
    private static final Map<ServerLevel, List<BeePortBlockEntity>> ports = new HashMap<>();
    private static final Map<ServerLevel, List<BeePortalBlockEntity>> portals = new HashMap<>();

    public static List<BeePortBlockEntity> getPorts(ServerLevel level) {
        return ports.get(level);
    }

    public static List<BeePortalBlockEntity> getPortals(ServerLevel serverLevel) {
        return portals.get(serverLevel);
    }

    public static Map<ServerLevel, List<BeePortBlockEntity>> getAllPorts(ServerLevel exclude) {
        return ports.entrySet().stream()
                .filter(entry -> entry.getKey() != exclude)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<ServerLevel, List<BeePortBlockEntity>> getAllPorts() {
        return ports;
    }

    public static void add(BeePortBlockEntity beePort) {
        Level level = beePort.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            if (!ports.containsKey(serverLevel)) {
                ports.put(serverLevel, new ArrayList<>());
                portals.put(serverLevel, new ArrayList<>());
            }

            if (ports.get(serverLevel).stream().noneMatch(be -> be == beePort))
                ports.get(serverLevel).add(beePort);
        }
    }

    public static void add(BeePortalBlockEntity beePortal) {
        Level level = beePortal.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            if (!portals.containsKey(serverLevel)) {
                ports.put(serverLevel, new ArrayList<>());
                portals.put(serverLevel, new ArrayList<>());
            }

            if (portals.get(serverLevel).stream().noneMatch(be -> be == beePortal))
                portals.get(serverLevel).add(beePortal);
        }
    }

    public static void remove(BeePortBlockEntity beePort) {
        Level level = beePort.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            if (ports.containsKey(serverLevel))
                getPorts(serverLevel).remove(beePort);
        }
    }

    public static void remove(BeePortalBlockEntity beePortal) {
        Level level = beePortal.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            if (portals.containsKey(serverLevel))
                getPortals(serverLevel).remove(beePortal);
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        return pCompoundTag;
    }
}
