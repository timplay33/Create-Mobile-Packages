package de.theidler.create_mobile_packages.blocks.bee_port;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

public class DronePortTracker extends SavedData {
    private final List<BeePortBlockEntity> dronePorts = new ArrayList<>();

    public static DronePortTracker get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(DronePortTracker::create, DronePortTracker::load), "drone_port_tracker");
    }

    public static DronePortTracker create() {
        return new DronePortTracker();
    }

    public static DronePortTracker load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        return DronePortTracker.create();
    }

    public void add(BeePortBlockEntity dronePort) {
        if (!dronePorts.contains(dronePort)) {
            dronePorts.add(dronePort);
            setDirty();
        }
    }

    public void remove(BeePortBlockEntity dronePort) {
        if (dronePorts.remove(dronePort)) {
            setDirty();
        }
    }

    public List<BeePortBlockEntity> getAll() {
        return dronePorts;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return compoundTag;
    }
}