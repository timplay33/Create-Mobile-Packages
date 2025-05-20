package de.theidler.create_mobile_packages;

import net.minecraft.nbt.CompoundTag;

public class SimpleRobo {

    private boolean isRemoved = false;

    public SimpleRobo(){}

    public static SimpleRobo createFromTag(CompoundTag roboTag) {
        SimpleRobo simpleRobo = new SimpleRobo();
        simpleRobo.isRemoved = roboTag.getBoolean("isRemoved");
        return simpleRobo;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void tick() {
        CreateMobilePackages.LOGGER.info("{} SimpleRobo ticked", this);
    }

    public void save(CompoundTag roboTag) {
        roboTag.putBoolean("isRemoved", isRemoved);
    }
}
