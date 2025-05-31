package de.theidler.create_mobile_packages.blocks;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BeePortStorage extends SavedData {
    private final List<BeePortBlockEntity> ports = new ArrayList<>();
    private final List<BeePortalBlockEntity> portals = new ArrayList<>();

    public static BeePortStorage create() {
        return new BeePortStorage();
    }

    public static BeePortStorage load(CompoundTag compoundTag) {
        return create();
    }

    public static BeePortStorage get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BeePortStorage::load, BeePortStorage::create, "bee_port_storage");
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
        return compoundTag;
    }

    public List<@NotNull BeePortBlockEntity> getPorts() {
        return new ArrayList<>(ports.stream().filter(Objects::nonNull).toList());
    }

    public List<@NotNull BeePortBlockEntity> getAllPorts(Level currentLevel) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null)
            return new ArrayList<>();
        Iterable<ServerLevel> levels = server.getAllLevels();
        List<BeePortBlockEntity> result = new ArrayList<>();
        for (ServerLevel level : levels) {
            BeePortStorage storage = get(level);
            List<BeePortBlockEntity> ports = storage.getPorts();
            result.addAll(ports);
        }

        result.sort(Comparator.comparingInt(be -> {
            if (be.getLevel() == null) return 4;
            ResourceKey<Level> level = be.getLevel().dimension();
            if (be.getLevel().dimensionType() == currentLevel.dimensionType())
                return -1;
            else if (level.equals(Level.OVERWORLD))
                return 0;
            else if (level.equals(Level.NETHER))
                return 1;
            else if (level.equals(Level.END))
                return 2;
            return 3;
        }));

        return result;
    }

    public List<BeePortalBlockEntity> getPortals() {
        return new ArrayList<>(portals.stream().filter(Objects::nonNull).toList());
    }

    public void add(@NotNull BeePortBlockEntity beePort) {
        Level level = beePort.getLevel();
        if (level instanceof ServerLevel && !ports.contains(beePort))
            ports.add(beePort);
    }

    public void add(@NotNull BeePortalBlockEntity beePortal) {
        Level level = beePortal.getLevel();
        if (level instanceof ServerLevel && !portals.contains(beePortal))
            portals.add(beePortal);
    }

    public void remove(@NotNull BeePortBlockEntity beePort) {
        Level level = beePort.getLevel();
        if (level instanceof ServerLevel)
            ports.remove(beePort);
    }

    public void remove(@NotNull BeePortalBlockEntity beePortal) {
        Level level = beePortal.getLevel();
        if (level instanceof ServerLevel)
            portals.remove(beePortal);
    }
}
