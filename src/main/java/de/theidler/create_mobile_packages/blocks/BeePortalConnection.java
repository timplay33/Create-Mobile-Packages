package de.theidler.create_mobile_packages.blocks;

import de.theidler.create_mobile_packages.Location;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import net.minecraft.world.phys.Vec3;

public record BeePortalConnection(BeePortalBlockEntity portalA, BeePortalBlockEntity portalB) {
    public static double distanceToTarget(BeePortalConnection connection, Location location, Vec3 targetPos) {
        BeePortalBlockEntity targetPortal;
        BeePortalBlockEntity exitPortal;

        if (connection.portalA().getLevel() == location.level()) {
            targetPortal = connection.portalA();
            exitPortal = connection.portalB();
        } else {
            targetPortal = connection.portalB();
            exitPortal = connection.portalA();
        }

        if (targetPos == null)
            return location.position().getCenter().distanceTo(targetPortal.getBlockPos().getCenter());
        return location.position().getCenter().distanceTo(targetPortal.getBlockPos().getCenter()) + exitPortal.getBlockPos().getCenter().distanceTo(targetPos);
    }

    public boolean contains(BeePortalBlockEntity portalA, BeePortalBlockEntity portalB) {
        if (this.portalA == portalA || this.portalA == portalB)
            return true;
        return this.portalB == portalA || this.portalB == portalB;
    }

    public boolean connectionExists(BeePortalBlockEntity portalA, BeePortalBlockEntity portalB) {
        if (this.portalA().getLevel() == portalA.getLevel() && this.portalB.getLevel() == portalB.getLevel())
            return true;
        return this.portalB().getLevel() == portalA.getLevel() && this.portalA.getLevel() == portalB.getLevel();
    }
}
