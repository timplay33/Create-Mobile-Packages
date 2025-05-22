package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.*;

public class RoboManager {

    public HashMap<UUID, RoboEntity> robos;

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

        StringBuilder output = new StringBuilder();
        for (RoboEntity robo : robos.values()) {
            output.append(robo.getUUID()).append(" ").append(robo).append("; ");
        }
        System.out.println("RoboManager ticking... with " + robos.size() + " robos: " + output);

        tickRobos(level);
    }

    private void tickRobos(Level level) {
        for (RoboEntity robo : robos.values()) {
            level.guardEntityTick(entity -> {}, robo);
            robo.roboMangerTick();
        }

        robos.values().removeIf(robo -> {return robo == null || robo.isRemoved();});
    }

    public void addRobo(RoboEntity robo) {
        robos.put(robo.getUUID(), robo);
    }

    public void removeRobo(UUID id) {
        RoboEntity robo = robos.get(id);
        if (robo != null) robo.remove(Entity.RemovalReason.DISCARDED);
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
        this.robos = new HashMap<>();
    }
}
