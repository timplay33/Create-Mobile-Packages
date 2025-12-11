package de.theidler.create_mobile_packages.blocks.bee_port;

import net.minecraft.core.BlockPos;

import java.util.UUID;

public class RoboRequest { //TODO: integrate with RoboTarget logic to allow requests from players / ports and blocks
    private final UUID logisticsNetworkId;
    BlockPos targetPos;
    long createdAt;
    private Status status;
    private int eta = -1;

    public RoboRequest(BlockPos pos, UUID logisticsNetworkId) {
        this.targetPos = pos;
        status = Status.PENDING;
        createdAt = System.currentTimeMillis();
        this.logisticsNetworkId = logisticsNetworkId;
    }

    public int getEta() {
        if (status == Status.DONE || status == Status.CANCELLED) return -1;
        return eta;
    }

    public void setEta(int eta) {
        this.eta = eta;
    }

    public UUID getLogisticsNetworkId() {
        return logisticsNetworkId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public enum Status {
        PENDING, IN_PROGRESS, DONE, CANCELLED
    }
}
