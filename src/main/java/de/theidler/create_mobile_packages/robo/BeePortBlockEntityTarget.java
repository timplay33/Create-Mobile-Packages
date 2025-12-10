package de.theidler.create_mobile_packages.robo;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import net.minecraft.world.phys.Vec3;

public class BeePortBlockEntityTarget implements RoboTarget {
    private final BeePortBlockEntity be;
    private int eta;

    public BeePortBlockEntityTarget(BeePortBlockEntity be) {
        this.be = be;
    }

    @Override
    public Vec3 getTargetPos() {
        return Vec3.atCenterOf(be.getBlockPos());
    }

    @Override
    public BeePortBlockEntity asBeePortBlockEntity() {
        return be;
    }

    @Override
    public boolean isValid() {
        return !be.isRemoved() && !be.isFull();
    }

    @Override
    public int getETA() {
        return eta;
    }

    @Override
    public void setETA(int eta) {
        this.eta = eta;
    }
}
