package de.theidler.create_mobile_packages.blocks.bee_port;

public enum FilterMode {
    ALL(0),
    PACKAGES_ONLY(1),
    ROBO_ONLY(2);

    private final int id;

    FilterMode(int id) {
        this.id = id;
    }

    static FilterMode fromId(int id) {
        for (FilterMode mode : values()) {
            if (mode.getId() == id) {
                return mode;
            }
        }
        return ALL;
    }

    public int getId() {
        return id;
    }
}
