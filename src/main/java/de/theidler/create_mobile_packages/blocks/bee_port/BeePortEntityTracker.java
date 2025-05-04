package de.theidler.create_mobile_packages.blocks.bee_port;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @Override
    public List<BeePortBlockEntity> getAll() {
        return Collections.unmodifiableList(list);
    }
}
