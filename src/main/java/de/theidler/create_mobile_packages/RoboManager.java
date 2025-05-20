package de.theidler.create_mobile_packages;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class RoboManager {

    private final List<SimpleRobo> roboStore = new ArrayList<>();
    private boolean isInitialized = false;

    public void tick(Level level) {
        if (!isInitialized) {
            isInitialized = true;
            RoboManagerSavedData.load((ServerLevel) level);
        }

        List<SimpleRobo> toBeRemoved = new ArrayList<>();

        for (SimpleRobo simpleRobo : roboStore) {
            if (simpleRobo != null && !simpleRobo.isRemoved()) {
                simpleRobo.tick();
            } else {
                toBeRemoved.add(simpleRobo);
            }
        }

        roboStore.removeAll(toBeRemoved);
    }

    public void add(SimpleRobo simpleRobo) {
        if (simpleRobo != null && !roboStore.contains(simpleRobo)) {
            roboStore.add(simpleRobo);
        }
    }

    public List<SimpleRobo> getRoboStore() {
        return roboStore;
    }
}