package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.blocks.BeePortalConnection;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LandingFinishState implements RoboEntityState {
    boolean init = true;

    @Override
    public void tick(RoboEntity re) {
        BeePortBlockEntity targetBlock = re.getTargetBlockEntity();
        Player targetPlayer = re.getTargetPlayer();
        BeePortalBlockEntity targetPortal = re.getTargetPortalEntity();
        re.setTargetVelocity(Vec3.ZERO);
        if (targetPortal != null) {
            Level targetLevel = targetBlock == null ? targetPlayer.level() : targetBlock.getLevel();
            if (targetLevel == null) return;
            BeePortalConnection portalConnection = re.getPortalConnection();
            if (portalConnection == null) return;
            BeePortalBlockEntity exitPortal = portalConnection.getExit(re);
            if (exitPortal != null && exitPortal.sendDrone(re)) {
                targetPortal.tryRemoveFromLandingQueue(re);
                re.remove(Entity.RemovalReason.CHANGED_DIMENSION);
            }
        } else {
            if (targetBlock != null && init) {
                targetBlock.tryRemoveFromLandingQueue(re);
                targetBlock.addBeeToRoboBeeInventory(1);
                init = false;
            }

            if (re.getItemStack().isEmpty()) re.setState(new ShutdownState());
            if (targetBlock != null && targetBlock.addItemStack(re.getItemStack())) re.setItemStack(ItemStack.EMPTY);
        }
    }
}
