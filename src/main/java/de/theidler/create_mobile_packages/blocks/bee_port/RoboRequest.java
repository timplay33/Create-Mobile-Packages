package de.theidler.create_mobile_packages.blocks.bee_port;

import de.theidler.create_mobile_packages.robo.RoboTarget;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class RoboRequest {
    private final UUID logisticsNetworkId;
    private final Mission mission;
    long createdAt;
    private Status status;
    private int eta = -1;
    RoboTarget target;

    public RoboRequest(RoboTarget target, UUID logisticsNetworkId, Mission mission) {
        this.target = target;
        status = Status.PENDING;
        createdAt = System.currentTimeMillis();
        this.logisticsNetworkId = logisticsNetworkId;
        this.mission = mission;
    }

    public Mission getMission() {
        return mission;
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

    public Vec3 getTargetPos() {
        return target.getTargetPos();
    }

    public RoboTarget getTarget() {
        return target;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public enum Status {
        PENDING, IN_PROGRESS, DONE, CANCELLED
    }

    public enum Mission {
        RESTOCK, // TODO: remove and replace with PICKUP for "fly by" PICKUP
        DELIVER,
        PICKUP
    }
}
