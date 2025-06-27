package de.theidler.create_mobile_packages.blocks;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.Location;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import de.theidler.create_mobile_packages.index.CMPBlockEntities;
import de.theidler.create_mobile_packages.index.CMPBlocks;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlock.IS_OPEN_TEXTURE;

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

    public static BeePortStorage load(CompoundTag nbt, ServerLevel level) {
        BeePortStorage storage = CreateMobilePackages.PORT_STORAGE.computeIfAbsent(level, BeePortStorage::create);
        NBTHelper.iterateCompoundList(nbt.getList("Ports", CompoundTag.TAG_COMPOUND), c -> {
            Tag dimTag = nbt.get("Dim");
            Tag posTag = nbt.get("Pos");
            MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null && dimTag != null && posTag != null) {
                if (level.dimension().location().getPath().equals(dimTag.getAsString())) {
                    int[] xyz = Arrays.stream(posTag.getAsString().split(",")).mapToInt(Integer::parseInt).toArray();
                    BlockPos blockPos = new BlockPos(xyz[0], xyz[1], xyz[2]);
                    storage.ports.add(new BeePortBlockEntity(CMPBlockEntities.BEE_PORT.get(), blockPos, CMPBlocks.BEE_PORT.getDefaultState()));
                }
            }
        });
        NBTHelper.iterateCompoundList(nbt.getList("Connections", CompoundTag.TAG_COMPOUND), c -> {
            Tag dimATag = nbt.get("DimA");
            Tag dimBTag = nbt.get("DimB");
            Tag posATag = nbt.get("PosA");
            Tag posBTag = nbt.get("PosB");
            MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null && dimATag != null && dimBTag != null && posATag != null && posBTag != null) {
                if (level.dimension().location().getPath().equals(dimATag.getAsString()) || level.dimension().location().getPath().equals(dimBTag.getAsString())) {
                    int[] xyzA = Arrays.stream(posATag.getAsString().split(",")).mapToInt(Integer::parseInt).toArray();
                    int[] xyzB = Arrays.stream(posATag.getAsString().split(",")).mapToInt(Integer::parseInt).toArray();
                    BlockPos blockPosA = new BlockPos(xyzA[0], xyzA[1], xyzA[2]);
                    BlockPos blockPosB = new BlockPos(xyzB[0], xyzB[1], xyzB[2]);
                    BeePortalBlockEntity portalA = new BeePortalBlockEntity(CMPBlockEntities.BEE_PORTAL.get(), blockPosA, CMPBlocks.BEE_PORTAL.getDefaultState());
                    BeePortalBlockEntity portalB = new BeePortalBlockEntity(CMPBlockEntities.BEE_PORTAL.get(), blockPosB, CMPBlocks.BEE_PORTAL.getDefaultState());
                    storage.portalConnections.add(new BeePortalConnection(portalA, portalB));
                }
            }
        });

        return storage;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        BeePortStorage storage = CreateMobilePackages.PORT_STORAGE.get(level);
        CreateMobilePackages.LOGGER.info("Saving RoboManager...");
        nbt.put("Ports", NBTHelper.writeCompoundList(storage.ports, port -> {
            CompoundTag portTag = new CompoundTag();
            if (port.getLevel() != null) {
                BlockPos pos = port.getBlockPos();
                portTag.putString("Dim", port.getLevel().dimension().location().getPath());
                portTag.putString("Pos", pos.getX() + "," + pos.getY() + "," + pos.getZ());
            }

            return portTag;
        }));
        nbt.put("Connections", NBTHelper.writeCompoundList(storage.portalConnections, portalConnection -> {
            CompoundTag connectionTag = new CompoundTag();
            BlockPos posA = portalConnection.getPortalA().getBlockPos();
            BlockPos posB = portalConnection.getPortalB().getBlockPos();
            if (portalConnection.getPortalA().getLevel() != null && portalConnection.getPortalB().getLevel() != null) {
                connectionTag.putString("DimA", portalConnection.getPortalA().getLevel().dimension().location().getPath());
                connectionTag.putString("DimB", portalConnection.getPortalB().getLevel().dimension().location().getPath());
                connectionTag.putString("PosA", posA.getX() + "," + posA.getY() + "," + posA.getZ());
                connectionTag.putString("PosB", posB.getX() + "," + posB.getY() + "," + posB.getZ());
            }

            return connectionTag;
        }));

        return nbt;
    }

    public static BeePortStorage get(@NotNull ServerLevel level) {
        if (CreateMobilePackages.PORT_STORAGE.get(level) != null)
            return CreateMobilePackages.PORT_STORAGE.get(level);
        return level.getDataStorage().computeIfAbsent(c -> BeePortStorage.load(c, level), () -> BeePortStorage.create(level), "bee_port_storage");
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

    public List<BeePortalConnection> getPortalConnections(BeePortalBlockEntity portal) {
        if (portal.getLevel() instanceof ServerLevel serverLevel)
            return getPortalConnections().stream().filter(c -> c.getCurrent(serverLevel) == portal).toList();
        return new ArrayList<>();
    }

    public List<BeePortalConnection> getPortalConnections() {
        return new ArrayList<>(portalConnections.stream().toList());
    }

    public BeePortalConnection getPortalConnection(BlockPos originPos, Location targetLocation) {
        // TODO: Create connection paths through multiple dimensions if needed
        return getPortalConnections().stream()
                .min(Comparator.comparingDouble(c -> BeePortalConnection.distanceToTarget(c, new Location(originPos, targetLocation.level()), targetLocation.position().getCenter())))
                .orElse(null);
    }

    public void add(@NotNull BeePortBlockEntity beePort) {
        if (beePort.getLevel() instanceof ServerLevel && !ports.contains(beePort))
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

    public void createConnection(@NotNull BeePortalBlockEntity portal, @NotNull BeePortalBlockEntity portalToConnect) {
        if (portalToConnect.getLevel() == portal.getLevel()) return;
        if (!(portalToConnect.getLevel() instanceof ServerLevel serverLevelB)
                || !(portal.getLevel() instanceof ServerLevel serverLevelA))
            return;
        if (portalConnections.stream().noneMatch(c -> c.connectionExists(portalToConnect, portal))) {
            BeePortStorage storage = get(serverLevelB);
            if (getPortalConnections(portal).isEmpty()) {
                serverLevelA.setBlockAndUpdate(portal.getBlockPos(), portal.getBlockState().setValue(IS_OPEN_TEXTURE, true));
                serverLevelA.playSound(null, portal.getBlockPos(), SoundEvents.PORTAL_TRIGGER,
                        SoundSource.BLOCKS);
            }

            if (storage.getPortalConnections(portalToConnect).isEmpty()) {
                serverLevelB.setBlockAndUpdate(portalToConnect.getBlockPos(), portalToConnect.getBlockState().setValue(IS_OPEN_TEXTURE, true));
                serverLevelB.playSound(null, portalToConnect.getBlockPos(), SoundEvents.PORTAL_TRIGGER,
                        SoundSource.BLOCKS);
            }

            BeePortalConnection connection = new BeePortalConnection(portalToConnect, portal);
            add(connection);
            storage.add(connection);
        }
    }

    public void remove(@NotNull BeePortBlockEntity beePort) {
        Level level = beePort.getLevel();
        if (level instanceof ServerLevel)
            ports.remove(beePort);
    }

    public void forceRemove(@NotNull BeePortalConnection connection) {
        getPortalConnections().remove(connection);
    }

    public void remove(@NotNull BeePortalBlockEntity portal) {
        Level level = portal.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            getPortalConnections().removeIf(c -> {
                if (c.getCurrent(serverLevel) == portal) {
                    BeePortalBlockEntity portalB = c.getExit(serverLevel);
                    if (portalB.getLevel() instanceof ServerLevel serverLevelB) {
                        BeePortStorage storage = BeePortStorage.get(serverLevelB);
                        storage.forceRemove(c);
                        if (storage.getPortalConnections(portalB).isEmpty()) {
                            serverLevelB.setBlockAndUpdate(portalB.getBlockPos(), portalB.getBlockState().setValue(IS_OPEN_TEXTURE, false));
                            serverLevelB.playSound(null, portalB.getBlockPos(), SoundEvents.PORTAL_TRIGGER,
                                    SoundSource.BLOCKS);
                        }
                    }

                    return true;
                }

                return false;
            });
        }

        if (portalToConnect == portal)
            portalToConnect = null;
    }
}
