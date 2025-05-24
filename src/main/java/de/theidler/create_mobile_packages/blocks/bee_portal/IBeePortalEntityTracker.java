package de.theidler.create_mobile_packages.blocks.bee_portal;

import java.util.List;

public interface IBeePortalEntityTracker {
    void add(BeePortalBlockEntity dpbe);

    void remove(BeePortalBlockEntity dpbe);

    List<BeePortalBlockEntity> getAll();
}

