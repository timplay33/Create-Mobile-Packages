package de.theidler.create_mobile_packages.blocks.drone_port;

import java.util.List;

public interface IDronePortEntityTracker {
    void add(DronePortDataStore ds);
    void remove(DronePortDataStore ds);
    List<DronePortDataStore> getAll();
}

