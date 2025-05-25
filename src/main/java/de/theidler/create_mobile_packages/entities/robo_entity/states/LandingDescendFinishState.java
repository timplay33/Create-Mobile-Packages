package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.Location;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LandingDescendFinishState implements RoboEntityState {
    boolean init = true;

    @Override
    public void tick(RoboEntity re) {
        BeePortBlockEntity targetBlock = re.getTargetBlockEntity();
        Player targetPlayer = re.getTargetPlayer();
        BeePortalBlockEntity targetPortal = re.getTargetPortalEntity();
        if (targetPortal != null) {
            Level targetLevel = targetBlock == null ? targetPlayer.level() : targetPortal.getLevel();
            if (targetLevel == null)
                return;

            Vec3 position = targetLevel.dimension() == Level.END
                    ? new Vec3(100, 49, 0)
                    : targetPortal.getBlockPos().getCenter().multiply(1 / 8d, 1, 1 / 8d);
            BeePortalBlockEntity exitPortal = RoboEntity.getClosestBeePortal(targetLevel, position);
            exitPortal.addBeeToRoboBeeInventory(1);
            exitPortal.sendDrone(re);
            re.remove(Entity.RemovalReason.CHANGED_DIMENSION);
        } else {
            if (targetBlock != null && init) {
                BeePortBlockEntity.setOpen(targetBlock, false);
                targetBlock.addBeeToRoboBeeInventory(1);
                init = false;
            }

            if (re.getItemStack().isEmpty()) re.setState(new ShutdownState());
            if (targetBlock != null) {
                if (targetBlock.addItemStack(re.getItemStack())) {
                    re.setItemStack(ItemStack.EMPTY);
                }
            }
        }
    }
}
