package de.theidler.create_mobile_packages.blocks.portal_port;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PortalPortBlockEntity extends BlockEntity {

    public PortalPortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            PortalPortTracker.get(serverLevel).add(worldPosition);
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (level instanceof ServerLevel serverLevel) {
            PortalPortTracker.get(serverLevel).remove(worldPosition);
        }
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            PortalPortTracker.get(serverLevel).remove(worldPosition);
        }
        super.setRemoved();
    }
}
