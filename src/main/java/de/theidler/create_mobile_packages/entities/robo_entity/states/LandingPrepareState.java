package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.Location;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import net.minecraft.world.phys.Vec3;

public class LandingPrepareState implements RoboEntityState {
    boolean init = true;

    @Override
    public void tick(RoboEntity re) {
        Location targetLocation = re.getTargetLocation();
        if (targetLocation != null) {
            if (init) {
                if (re.getTargetPortalEntity() == null)
                    BeePortBlockEntity.setOpen(re.getTargetBlockEntity(), true);
                else
                    BeePortalBlockEntity.setOpen(re.getTargetPortalEntity(), true);

                Vec3 newPos = targetLocation.position().getCenter().subtract(0, 0.5, 0);
                re.setPos(newPos);
                init = false;
            }

            if (re.rotateToSnap() == 0) {
                re.setState(new LandingDescendState());
            }
        }
    }
}
