package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoboManager {

    public Map<UUID, RoboEntity> robos;

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

    private synchronized void tickRobos(Level level) {
        Iterator<Map.Entry<UUID, RoboEntity>> it = robos.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, RoboEntity> entry = it.next();
            RoboEntity robo = entry.getValue();

            level.guardEntityTick(entity -> {}, robo);
            robo.roboMangerTick();

            if (robo.isRemoved()) {
                it.remove();
            }
        }
    }

    public void addRobo(RoboEntity robo) {
        robos.put(robo.getUUID(), robo);
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
    }
}
