package de.theidler.create_mobile_packages.blocks;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.Location;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
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
    private final ServerLevel level;
    private final List<BeePortBlockEntity> ports = new ArrayList<>();
    private final List<BeePortalConnection> portalConnections = new ArrayList<>();
    private BeePortalBlockEntity portalToConnect = null;

    public BeePortStorage(ServerLevel level) {
        this.level = level;
    }

    public static BeePortStorage create(ServerLevel level) {
        BeePortStorage storage = new BeePortStorage(level);
        CreateMobilePackages.PORT_STORAGE.put(level, storage);
        return storage;
    }

    public static BeePortStorage load(CompoundTag compoundTag, ServerLevel level) {
        return create(level);
    }

    public static BeePortStorage get(@NotNull ServerLevel level) {
        if (CreateMobilePackages.PORT_STORAGE.get(level) != null)
            return CreateMobilePackages.PORT_STORAGE.get(level);
        return level.getDataStorage().computeIfAbsent(c -> BeePortStorage.load(c, level), () -> BeePortStorage.create(level), "bee_port_storage");
    }

    public static int newPortId(ServerLevel level) {
        BeePortStorage storage = BeePortStorage.get(level);
        int lastId = storage.ports.stream().map(BeePortBlockEntity::getId).filter(Objects::nonNull).mapToInt(v -> v).max().orElse(-1);
        return lastId + 1;
    }

    public static int newConnectionId() {
        int lastId = CreateMobilePackages.PORT_STORAGE.values().stream().map(s -> s.portalConnections.stream()
                .map(BeePortalConnection::getId).filter(Objects::nonNull).mapToInt(v -> v).max()
                .orElse(-1)).mapToInt(v -> v).max().orElse(-1);
        return lastId + 1;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        BeePortStorage storage = CreateMobilePackages.PORT_STORAGE.get(level);
        CreateMobilePackages.LOGGER.info("Saving RoboManager...");
        nbt.put("Ports", NBTHelper.writeCompoundList(storage.ports, port -> {
            CompoundTag roboTag = new CompoundTag();
            if (port.getId() != null)
                roboTag.putInt("Id", port.getId());
            return roboTag;
        }));
        nbt.put("Connections", NBTHelper.writeCompoundList(storage.portalConnections, portalConnection -> {
            CompoundTag roboTag = new CompoundTag();
            if (portalConnection.getId() != null)
                roboTag.putInt("Id", portalConnection.getId());
            return roboTag;
        }));
        return nbt;
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

    public List<BeePortalConnection> getPortalConnections() {
        return new ArrayList<>(portalConnections.stream().filter(Objects::nonNull).toList());
    }

    public BeePortalConnection getPortalConnection(BlockPos originPos, Location targetLocation) {
        // TODO: Create connection paths through multiple dimensions if needed
        return getPortalConnections().stream()
                .min(Comparator.comparingDouble(c -> BeePortalConnection.distanceToTarget(c, new Location(originPos, targetLocation.level()), targetLocation.position().getCenter())))
                .orElse(null);
    }

    public void add(@NotNull BeePortBlockEntity beePort) {
        Level level = beePort.getLevel();
        if (level instanceof ServerLevel && !ports.contains(beePort))
            ports.add(beePort);
    }

    public void add(@NotNull BeePortalConnection portalConnection) {
        portalConnections.add(portalConnection);
    }

    public boolean trySetPortalToConnect(@NotNull BeePortalBlockEntity portal) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            Iterable<ServerLevel> levels = server.getAllLevels();
            for (ServerLevel serverLevel : levels) {
                BeePortStorage storage = BeePortStorage.get(serverLevel);
                if (storage.portalToConnect == portal) return true;
                if (storage.portalToConnect != null) return false;
            }
        }

        if (portalToConnect == null) {
            portalToConnect = portal;
            return true;
        }

        return false;
    }


    public BeePortalBlockEntity getCurrentPortalToConnect() {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) return null;
        Iterable<ServerLevel> levels = server.getAllLevels();
        for (ServerLevel serverLevel : levels) {
            BeePortStorage storage = get(serverLevel);
            if (storage.portalToConnect != null) return storage.portalToConnect;
        }

        return null;
    }

    public void createConnection(@NotNull BeePortalBlockEntity portal) {
        final BeePortalBlockEntity portalToConnect = getCurrentPortalToConnect();
        if (portalToConnect == null || portalToConnect.getLevel() == portal.getLevel()) return;
        if (portalConnections.stream().noneMatch(c -> c.connectionExists(portalToConnect, portal)))
            add(new BeePortalConnection(portalToConnect, portal));
    }

    public void remove(@NotNull BeePortBlockEntity beePort) {
        Level level = beePort.getLevel();
        if (level instanceof ServerLevel)
            ports.remove(beePort);
    }

    public void remove(@NotNull BeePortalBlockEntity beePortal) {
        Level level = beePortal.getLevel();
        if (level instanceof ServerLevel) {
            portalConnections.removeIf(c -> c.getPortalA() == beePortal || c.getPortalB() == beePortal);
        }

        if (portalToConnect == beePortal)
            portalToConnect = null;
    }
}
