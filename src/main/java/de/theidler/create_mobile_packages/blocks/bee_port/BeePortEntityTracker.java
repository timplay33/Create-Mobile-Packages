package de.theidler.create_mobile_packages.blocks.bee_port;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BeePortEntityTracker implements IBeePortEntityTracker {
    private final List<BeePortBlockEntity> list = new ArrayList<>();

    @Override
    public void add(BeePortBlockEntity dpbe) {
        list.add(dpbe);
    }

    @Override
    public void remove(BeePortBlockEntity dpbe) {
        list.remove(dpbe);
    }

    /**
     * Get all BeePortBlockEntities grouped by logistics network.
     *
     * @return A map of UUIDs to lists of BeePortBlockEntities.
     */
    @Override
    public List<BeePortBlockEntity> getAll() {
        return List.copyOf(list);
    }

    /**
     * Get all BeePortBlockEntities for a given logistics network.
     *
     * @param logisticsNetworkId The UUID of the logistics network.
     * @return A list of BeePortBlockEntities.
     */
    @Override
    public List<BeePortBlockEntity> getAllByNetwork(UUID logisticsNetworkId) {
        return list.stream().filter(dpbe -> dpbe.getLogisticsNetworkId().equals(logisticsNetworkId)).toList();
    }
}
