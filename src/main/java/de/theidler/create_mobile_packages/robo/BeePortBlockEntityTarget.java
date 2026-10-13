package de.theidler.create_mobile_packages.robo;

import de.theidler.create_mobile_packages.CMPHelper;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

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
        return CMPHelper.getGlobalCenter(level, pos);
    }

    @Override
    public @Nullable BeePortBlockEntity asBeePortBlockEntity() {
        if (level == null) return null;
        if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return null;
        if (level.getBlockEntity(pos) instanceof BeePortBlockEntity be) return be;
        return null;
    }

    @Override
    public boolean isValid(VirtualRobo robo) {
        BeePortBlockEntity be = asBeePortBlockEntity();
        boolean doesBeePortExists = be != null && !be.isRemoved();
        boolean hasItemStack = !robo.getItemStack().isEmpty();
        return doesBeePortExists && (hasItemStack ? be.hasSpaceForPackageAndRobo() : be.hasSpaceForRobo());
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
