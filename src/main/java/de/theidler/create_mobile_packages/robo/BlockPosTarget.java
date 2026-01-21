package de.theidler.create_mobile_packages.robo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class BlockPosTarget implements RoboTarget {
    private final BlockPos pos;
    private int eta;

    public BlockPosTarget(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public Vec3 getTargetPos() {
        return Vec3.atCenterOf(pos);
    }

    @Override
    public BlockPos asBlockPos() {
        return pos;
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
