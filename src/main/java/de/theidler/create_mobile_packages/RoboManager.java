package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoboManager {

    public Map<UUID, RoboBeeEntity> robos;
    public Map<UUID, RoboBeeEntity> clientRobos;
    public List<RoboBeeEntity> robosToAdd;

    private RoboManagerSavedData savedData;
    private Level level;

    public RoboManager() {
        cleanUp();
    }

    public void markDirty() {
        if (savedData != null)
            savedData.setDirty();
    }

    public void tick(Level level) {
        if (level.dimension() != Level.OVERWORLD)
            return;

        tickRobos(level);
    }

    private void tickRobos(Level level) {
        addPendingRobos();
        tickExistingRobos(level);
        removeMarkedRobos();
    }

    private void addPendingRobos() {
        if (robosToAdd.isEmpty()) return;
        
        List<RoboBeeEntity> newRobos = new ArrayList<>(robosToAdd);
        for (RoboBeeEntity robo : newRobos) {
            if (robo.level().isClientSide()) {
                clientRobos.put(robo.getUUID(), robo);
            } else {
                robos.put(robo.getUUID(), robo);
            }
        }
        robosToAdd.removeAll(newRobos);
    }

    private void tickExistingRobos(Level level) {
        if (robos.isEmpty() && clientRobos.isEmpty()) return;
        
        robos.values().stream()
            .filter(Objects::nonNull)
            .forEach(robo -> {
                level.guardEntityTick(entity -> {}, robo);
                robo.roboMangerTick();
            });
        clientRobos.values().stream()
            .filter(Objects::nonNull)
            .forEach(robo -> {
                level.guardEntityTick(entity -> {}, robo);
                robo.roboMangerTick();
            });
    }

    private void removeMarkedRobos() {
        if (robos.isEmpty() && clientRobos.isEmpty()) return;
        robos.entrySet().removeIf(entry -> entry.getValue().isRemoved());
        clientRobos.entrySet().removeIf(entry -> entry.getValue().isRemoved());
    }

    public void addRobo(RoboBeeEntity robo) {
        robosToAdd.add(robo);
    }

    public Level getLevel() {
        return level;
    }
    public void setLevel(Level level) {
        this.level = level;
    }

    public void levelLoaded(LevelAccessor level) {
        this.level = (Level) level;
        MinecraftServer server = level.getServer();
        if (server == null || server.overworld() != level)
            return;
        cleanUp();
        savedData = null;
        loadRoboData(server);
    }

    private void loadRoboData(MinecraftServer server) {
        if (savedData != null)
            return;
        savedData = RoboManagerSavedData.load(server);
        robos = savedData.getRobos();
    }

    private void cleanUp() {
        this.robos = new ConcurrentHashMap<>();
        this.robosToAdd = new ArrayList<>();
        this.clientRobos = new ConcurrentHashMap<>();
    }
}