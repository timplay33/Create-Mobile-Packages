package de.theidler.create_mobile_packages.robo;

import de.theidler.create_mobile_packages.CMPHelper;
import de.theidler.create_mobile_packages.index.CMPBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class PortalPortTarget implements RoboTarget {
    private final BlockPos pos;
    private final ServerLevel level;
    private int eta;

    public PortalPortTarget(ServerLevel level, BlockPos pos) {
        this.level = level;
        this.pos = pos.immutable();
    }

    @Override
    public Vec3 getTargetPos() {
        return CMPHelper.getGlobalCenter(level, pos);
    }

    @Override
    public BlockPos asBlockPos() {
        return pos;
    }

    @Override
    public boolean isValid(VirtualRobo robo) {
        if (level == null || robo.getServerLevel() != level) {
            return false;
        }
        return level.getBlockState(pos).is(CMPBlocks.PORTAL_PORT.get());
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
