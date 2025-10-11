package de.theidler.create_mobile_packages.entities.robo_entity;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.robo.VirtualRobo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class RoboBeeBehaviorController {
    private RoboBeeState state = RoboBeeState.IDLE;
    private boolean init = true;

    public void tick(VirtualRobo robo) {
        switch (state) {
            case IDLE:
                handleIdle(robo);
                break;
            case TAKEOFF:
                handleTakeoff(robo);
                break;
            case NAVIGATE_TO_TARGET:
                handleNavigateToTarget(robo);
                break;
            case ALIGN_FOR_DELIVERY:
                handleAlignForDelivery(robo);
                break;
            case LAND:
                handleLand(robo);
                break;
            case DELIVER_PACKAGE:
                handleDeliverPackage(robo);
                break;
            case SHUTDOWN:
                handleShutdown(robo);
                break;
        }
    }

    private void handleIdle(VirtualRobo robo) {
        robo.setTargetVelocity(Vec3.ZERO);
        if (robo.getTarget() != null && robo.getTarget().isValid()) {
            setState(RoboBeeState.TAKEOFF);
        }
    }

    private void handleTakeoff(VirtualRobo robo) {
        if (init) {
            openPort(robo.getStartBeePortBlockEntity(), true);
            init = false;
        }
        if (robo.getStartBeePortBlockEntity() == null) {
            setState(RoboBeeState.NAVIGATE_TO_TARGET);
            return;
        }
        Vec3 mid = getAbove(robo.getStartBeePortBlockEntity(), 1.6);
        Vec3 end = getAbove(robo.getStartBeePortBlockEntity(), 2);

        double y = robo.getCurrentPos().y;
        if (y < mid.y - 0.05) {
            moveAndScale(robo, mid, 0.1, 0, 1); // 1st part with scaling package
        } else if (y < end.y - 0.05) {
            moveTo(robo, end, 0.1); // 2nd part without scaling package
            robo.setPackageHeightScale(1.0f);
        } else {
            robo.setPos(end);
            robo.setTargetVelocity(Vec3.ZERO);
            openPort(robo.getStartBeePortBlockEntity(), false);
            setState(RoboBeeState.NAVIGATE_TO_TARGET);
        }
    }

    private void handleNavigateToTarget(VirtualRobo robo) {
        if (robo.getTargetPosition() == null) {
            setState(RoboBeeState.IDLE);
            return;
        }
        Vec3 target = getAbove(robo.getTargetPosition(), 2);
        moveTo(robo, target, 0.2);
        if (isAtTarget(robo, target)) {
            setState(RoboBeeState.ALIGN_FOR_DELIVERY);
            robo.setTargetVelocity(Vec3.ZERO);
        }
    }

    private void handleAlignForDelivery(VirtualRobo robo) {
        if (init) {
            openPort(robo.getTarget() != null ? robo.getTarget().asBeePortBlockEntity() : null, true);
            init = false;
        }
        if (robo.rotateToSnap() == 0) {
            setState(RoboBeeState.LAND);
        }
    }

    private void handleLand(VirtualRobo robo) {
        if (robo.getTarget() != null && robo.getTarget().asBeePortBlockEntity() == null) {
            setState(RoboBeeState.DELIVER_PACKAGE);
            return;
        }
        Vec3 end = getBelow(robo.getTarget() != null ? robo.getTarget().asBeePortBlockEntity() : null, 0.5);
        Vec3 mid = getAbove(robo.getTarget().asBeePortBlockEntity(), 1);
        Vec3 start = getAbove(robo.getTarget().asBeePortBlockEntity(), 2);
        if (init) {
            robo.setPos(start);
            robo.setPackageHeightScale(1.0f);
            init = false;
        }
        double y = robo.getCurrentPos().y;
        if (y > mid.y + 0.05) {
            moveTo(robo, mid, 0.1); // 1st part without scaling package
            robo.setPackageHeightScale(1.0f);
        } else if (y > end.y + 0.05) {
            moveAndScale(robo, end, 0.1, 1, 0); // 2nd part with scaling package
        } else {
            robo.setPos(end);
            robo.setTargetVelocity(Vec3.ZERO);
            openPort(robo.getTarget().asBeePortBlockEntity(), false);
            setState(RoboBeeState.DELIVER_PACKAGE);
        }
    }

    private void handleDeliverPackage(VirtualRobo robo) {
        boolean delivered = false;
        // Try to deliver to player
        if (robo.getTarget() != null && robo.getTarget().asPlayer() != null && !robo.getItemStack().isEmpty()) {
            delivered = BeePortBlockEntity.sendPackageToPlayer(robo.getTarget().asPlayer(), robo.getItemStack());
            if (delivered) {
                robo.setItemStack(ItemStack.EMPTY);
                robo.setTarget(null);
            }
        }
        // Try to deliver to block entity
        if (robo.getTarget() != null && !delivered && robo.getTarget().asBeePortBlockEntity() != null && !robo.getItemStack().isEmpty()) {
            delivered = robo.getTarget().asBeePortBlockEntity().addItemStack(robo.getItemStack());
            if (delivered) {
                robo.setItemStack(ItemStack.EMPTY);
                robo.setTarget(null);
            }
        }

        // updating target Address with update -> creates new target if target was null
        robo.setTargetAddress(PackageItem.getAddress(robo.getItemStack()), true);

        // if the new taget is a Bee Port and the Robo is in it then shutdown the Robo.
        if (robo.getTarget() != null && robo.getTarget().asBeePortBlockEntity() != null) {
            if (BlockPos.containing(robo.getCurrentPos()).equals(BlockPos.containing(robo.getTargetPosition()))) {
                setState(RoboBeeState.SHUTDOWN);
                return;
            }
        }
        // else go to the new target
        setState(RoboBeeState.TAKEOFF);
    }

    private void handleShutdown(VirtualRobo robo) {
        if (robo.getServerLevel().getBlockEntity(BlockPos.containing(robo.getCurrentPos())) instanceof BeePortBlockEntity bpbe)
            bpbe.addBeeToRoboBeeInventory(1);
        robo.setRemoved(robo.getServerLevel());
    }

    // Helper Functions
    private void openPort(BeePortBlockEntity port, boolean open) {
        if (port != null) {
           BeePortBlockEntity.setOpen(port, open);
        }
    }

    private Vec3 getAbove(Object blockEntityOrPos, double y) {
        if (blockEntityOrPos instanceof BlockPos pos) {
            return pos.getCenter().add(0, y, 0);
        } else if (blockEntityOrPos instanceof BlockEntity blockEntity) {
            return blockEntity.getBlockPos().getCenter().add(0, y, 0);
        } else if (blockEntityOrPos instanceof Vec3 vec) {
            return vec.add(0, y, 0);
        }
        return Vec3.ZERO;
    }

    private Vec3 getBelow(Object blockEntityOrPos, double y) {
        if (blockEntityOrPos instanceof BlockPos pos) {
            return pos.getCenter().subtract(0, y, 0);
        } else if (blockEntityOrPos instanceof BlockEntity blockEntity) {
            return blockEntity.getBlockPos().getCenter().subtract(0, y, 0);
        } else if (blockEntityOrPos instanceof Vec3 vec) {
            return vec.subtract(0, y, 0);
        }
        return Vec3.ZERO;
    }

    private void moveTo(VirtualRobo robo, Vec3 target, double speed) {
        Vec3 dir = target.subtract(robo.getCurrentPos());
        double dist = dir.length();
        if (dist < 0.05) {
            robo.setPos(target);
            robo.setTargetVelocity(Vec3.ZERO);
        } else {
            dir = dir.normalize();
            robo.setTargetVelocity(dir.scale(speed));
        }
        // look at the target
        robo.setYaw((float) (Math.toDegrees(Math.atan2(-dir.x, dir.z))));
        robo.setPitch((float) (Math.toDegrees(Math.asin(dir.y))));
    }

    private void moveAndScale(VirtualRobo robo, Vec3 target, double speed, float scaleStart, float scaleEnd) {
        Vec3 dir = target.subtract(robo.getCurrentPos());
        double dist = dir.length();
        double totalDist = 2.0;
        float progress = (float) Math.max(0.0, Math.min(1.0, 1.0 - (dist / totalDist)));
        float scale = scaleStart + (scaleEnd - scaleStart) * progress;
        robo.setPackageHeightScale(scale);
        moveTo(robo, target, speed);
    }

    private boolean isAtTarget(VirtualRobo robo, Vec3 target) {
        return robo.getCurrentPos().distanceTo(target) < 0.1;
    }

    public void setState(RoboBeeState newState) {
        this.state = newState;
        this.init = true;
    }
}
