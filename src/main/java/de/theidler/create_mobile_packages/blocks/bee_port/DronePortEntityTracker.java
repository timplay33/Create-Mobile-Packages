package de.theidler.create_mobile_packages.blocks.bee_port;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DronePortEntityTracker implements IDronePortEntityTracker {
    private final List<DronePortBlockEntity> list = new ArrayList<>();

    @Override
    public void add(DronePortBlockEntity dpbe) {
        list.add(dpbe);
    }

    @Override
    public void remove(DronePortBlockEntity dpbe) {
        list.remove(dpbe);
    }

    @Override
    public List<DronePortBlockEntity> getAll() {
        return Collections.unmodifiableList(list);
    }
}
