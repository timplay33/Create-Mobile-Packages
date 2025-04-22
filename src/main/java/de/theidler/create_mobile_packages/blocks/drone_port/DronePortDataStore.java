package de.theidler.create_mobile_packages.blocks.drone_port;

import net.minecraft.core.BlockPos;

public class DronePortDataStore {
    private final BlockPos pos;
    private final String address;

    public DronePortDataStore(BlockPos pos, String address) {
        this.pos = pos;
        this.address = address;
    }

    public DronePortDataStore(DronePortBlockEntity blockEntity) {
        this.pos = blockEntity.getBlockPos();
        this.address = blockEntity.addressFilter;
    }

    public BlockPos getPos() {
        return pos;
    }

    public String getAddress() {
        return address;
    }
}
