package de.theidler.create_mobile_packages.blocks;

import de.theidler.create_mobile_packages.Location;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BeePortalConnection {
    private final @Nullable Integer Id;
    private final BeePortalBlockEntity portalA;
    private final BeePortalBlockEntity portalB;

    public BeePortalConnection(BeePortalBlockEntity portalA, BeePortalBlockEntity portalB) {
        this.portalA = portalA;
        this.portalB = portalB;
        this.Id = BeePortStorage.newConnectionId();
    }

    public static double distanceToTarget(BeePortalConnection connection, Location location, Vec3 targetPos) {
        BeePortalBlockEntity targetPortal;
        BeePortalBlockEntity exitPortal;

        if (connection.portalA.getLevel() == location.level()) {
            targetPortal = connection.portalA;
            exitPortal = connection.portalB;
        } else {
            targetPortal = connection.portalB;
            exitPortal = connection.portalA;
        }

        if (targetPos == null)
            return location.position().getCenter().distanceTo(targetPortal.getBlockPos().getCenter());
        return location.position().getCenter().distanceTo(targetPortal.getBlockPos().getCenter()) + exitPortal.getBlockPos().getCenter().distanceTo(targetPos);
    }

    public boolean connectionExists(BeePortalBlockEntity portalA, BeePortalBlockEntity portalB) {
        if (this.portalA.getLevel() == portalA.getLevel() && this.portalB.getLevel() == portalB.getLevel())
            return true;
        return this.portalB.getLevel() == portalA.getLevel() && this.portalA.getLevel() == portalB.getLevel();
    }

    public void tryRemoveFromQueue(@NotNull RoboEntity re) {
        if (re.getTargetPortalEntity() == portalA) {
            portalA.tryRemoveFromLandingQueue(re);
            portalB.tryRemoveFromLaunchingQueue(re);
        } else {
            portalA.tryRemoveFromLaunchingQueue(re);
            portalB.tryRemoveFromLandingQueue(re);
        }
    }

    public BeePortalBlockEntity getCurrent(@NotNull ServerLevel serverLevel) {
        if (serverLevel == portalA.getLevel()) return portalA;
        return portalB;
    }

    public BeePortalBlockEntity getExit(@NotNull ServerLevel serverLevel) {
        if (serverLevel == portalA.getLevel()) return portalB;
        return portalA;
    }

    public BeePortalBlockEntity getPortalA() {
        return portalA;
    }

    public BeePortalBlockEntity getPortalB() {
        return portalB;
    }

    public @Nullable Integer getId() {
        return Id;
    }
}
