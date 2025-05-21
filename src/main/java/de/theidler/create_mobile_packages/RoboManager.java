package de.theidler.create_mobile_packages;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.*;

public class RoboManager {

    public Map<UUID, SimpleRobo> robos;

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
        for (SimpleRobo robo : robos.values()) {
            robo.tick();
        }

        for (Iterator<SimpleRobo> iterator = robos.values().iterator(); iterator.hasNext();) {
            SimpleRobo robo = iterator.next();
            if (robo.invalid) {
                iterator.remove();
                robos.remove(robo.id);
            }
        }
    }

    public void addRobo(SimpleRobo robo) {
        robos.put(robo.id, robo);
    }

    public void removeRobo(UUID id) {
        SimpleRobo robo = robos.get(id);
        if (robo != null) robo.remove();
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

    public enum RoboType {
        SIMPLE_ROBO,
        ROBO_BEE
    }

}
