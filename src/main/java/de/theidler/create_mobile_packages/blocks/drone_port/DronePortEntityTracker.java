package de.theidler.create_mobile_packages.blocks.drone_port;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DronePortEntityTracker implements IDronePortEntityTracker {
    private final List<DronePortDataStore> list = new ArrayList<>();

    @Override
    public void add(DronePortDataStore ds) {
        list.add(ds);
    }

    @Override
    public void remove(DronePortDataStore ds) {
        list.remove(ds);
    }

    @Override
    public List<DronePortDataStore> getAll() {
        return Collections.unmodifiableList(list);
    }
}
