package de.theidler.create_mobile_packages.robo;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class BeePortBlockEntityTarget implements RoboTarget {
    private final BlockPos pos;
    private final ServerLevel level;
    private int eta;

    public BeePortBlockEntityTarget(BeePortBlockEntity be) {
        this.pos = be.getBlockPos();
        this.level = be.getLevel() instanceof ServerLevel ? (ServerLevel) be.getLevel() : null;
    }

    @Override
    public Vec3 getTargetPos() {
        return Vec3.atCenterOf(pos);
    }

    @Override
    public BeePortBlockEntity asBeePortBlockEntity() {
        // Lazy lookup to avoid holding onto a removed/invalid instance
        if (level == null) return null;
        if (level.getBlockEntity(pos) instanceof BeePortBlockEntity be) return be;
        return null;
    }

    @Override
    public boolean isValid(VirtualRobo robo) {
        BeePortBlockEntity be = asBeePortBlockEntity();
        boolean doesBeePortExits = be != null && !be.isRemoved();
        boolean hasItemStack = !robo.getItemStack().isEmpty();
        return doesBeePortExits && (hasItemStack ? be.hasSpaceForPackageAndRobo() : be.hasSpaceForRobo());
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
