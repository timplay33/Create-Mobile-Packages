package de.theidler.create_mobile_packages.blocks;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BeePortStorage extends SavedData {
    private final List<BeePortBlockEntity> ports = new ArrayList<>();
    private final List<BeePortalBlockEntity> portals = new ArrayList<>();

    public static BeePortStorage create() {
        return new BeePortStorage();
    }

    public static BeePortStorage load(CompoundTag compoundTag) {
        return create();
    }

    public static BeePortStorage get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BeePortStorage::load, BeePortStorage::create, "bee_port_storage");
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
        return compoundTag;
    }

    public List<BeePortBlockEntity> getPorts() {
        return new ArrayList<>(ports);
    }

    public List<BeePortBlockEntity> getAllPorts() {
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

        return result;
    }

    public List<BeePortalBlockEntity> getPortals() {
        return new ArrayList<>(portals);
    }

    public void add(BeePortBlockEntity beePort) {
        Level level = beePort.getLevel();
        if (level instanceof ServerLevel && !ports.contains(beePort))
            ports.add(beePort);
    }

    public void add(BeePortalBlockEntity beePortal) {
        Level level = beePortal.getLevel();
        if (level instanceof ServerLevel && !portals.contains(beePortal))
            portals.add(beePortal);
    }

    public void remove(BeePortBlockEntity beePort) {
        Level level = beePort.getLevel();
        if (level instanceof ServerLevel)
            ports.remove(beePort);
    }

    public void remove(BeePortalBlockEntity beePortal) {
        Level level = beePortal.getLevel();
        if (level instanceof ServerLevel)
            portals.remove(beePortal);
    }
}
