package de.theidler.create_mobile_packages.blocks.bee_port;

import java.util.List;
import java.util.UUID;

public interface IBeePortEntityTracker {
    void add(BeePortBlockEntity dpbe);

    void remove(BeePortBlockEntity dpbe);

    List<BeePortBlockEntity> getAll();

    List<BeePortBlockEntity> getAllByNetwork(UUID logisticsNetworkId);
}

