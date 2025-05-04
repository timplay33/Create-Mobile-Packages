package de.theidler.create_mobile_packages.blocks.bee_port;

import java.util.List;

public interface IDronePortEntityTracker {
    void add(BeePortBlockEntity dpbe);
    void remove(BeePortBlockEntity dpbe);
    List<BeePortBlockEntity> getAll();
}

