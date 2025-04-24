package de.theidler.create_mobile_packages.entities.robo_entity;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.blocks.drone_port.DronePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.drone_port.ModCapabilities;
import de.theidler.create_mobile_packages.entities.robo_entity.states.AdjustRotationToTarget;
import de.theidler.create_mobile_packages.entities.robo_entity.states.LandingDecendFinishState;
import de.theidler.create_mobile_packages.entities.robo_entity.states.LaunchPrepareState;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RoboEntity extends Mob {

    private static final EntityDataAccessor<Float> ROT_YAW =
            SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.FLOAT);

    private RoboEntityState state;
    private Vec3 targetVelocity;

    private ItemStack itemStack;
    private Player targetPlayer;
    private DronePortBlockEntity targetBlockEntity;
    private DronePortBlockEntity startDronePortBlockEntity;

    /**
     * Constructor for RoboEntity. Used for spawning the entity.
     * @param type type
     * @param level level
     * @param itemStack The ItemStack (package) used to determine the target.
     */
    public RoboEntity(EntityType<? extends Mob> type, Level level, ItemStack itemStack, BlockPos spawnPos) {
        super(type, level);
        setItemStack(itemStack);
        this.targetVelocity = Vec3.ZERO;
        setTargetFromItemStack(itemStack);
        this.setPos(spawnPos.getCenter().subtract(0, 0.5, 0));
        if (level().getBlockEntity(spawnPos) instanceof DronePortBlockEntity dpbe) {
            startDronePortBlockEntity = dpbe;
        }
        if (!level().isClientSide()) {
            this.entityData.set(ROT_YAW, (float) getSnapAngle(getAngleToTarget()));
        }
        // don't fly out of the port if target is origin
        if (targetBlockEntity != null && targetBlockEntity.equals(startDronePortBlockEntity)) {
            setState(new LandingDecendFinishState());
        }
        if (startDronePortBlockEntity == null) {
            setState(new AdjustRotationToTarget());
        }
        setState(new LaunchPrepareState());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROT_YAW, getYRot());
    }

    /**
     * Sets the target for the RoboEntity based on the provided ItemStack.
     * If the ItemStack is not a package, the target is set to the closest drone port.
     * If the ItemStack is a package, it attempts to find a player or drone port
     * matching the address specified in the package.
     *
     * @param itemStack The ItemStack used to determine the target.
     */
    private void setTargetFromItemStack(ItemStack itemStack) {
        if (itemStack == null) return;
        if (!PackageItem.isPackage(itemStack)) {
            this.targetBlockEntity = getClosestDronePort();
            return;
        }
        for (Player player : level().players()) {
            if (player.getName().getString().equals(PackageItem.getAddress(itemStack))) {
                this.targetPlayer = player;
                return;
            }
        }
        targetBlockEntity = getClosestDronePort(PackageItem.getAddress(itemStack));
    }

    /**
     * Gets the position of the current target.
     * If no target is set, it defaults to the closest drone port.
     *
     * @return The block position of the target.
     */
    public BlockPos getTargetPosition() {
        if (targetPlayer != null) {
            return targetPlayer.blockPosition().above();
        } else if (targetBlockEntity != null) {
            return targetBlockEntity.getBlockPos().above();
        } else {
            DronePortBlockEntity closest = getClosestDronePort();
            targetBlockEntity = closest;
            if (closest != null) {
                return closest.getBlockPos().above();
            } else {
                return null;
            }
        }
    }

    /**
     * Finds the closest drone port to the RoboEntity.
     *
     * @return The closest DronePortBlockEntity.
     */
    public DronePortBlockEntity getClosestDronePort() {
        return getClosestDronePort(null);
    }

    /**
     * Finds the closest drone port to the RoboEntity, filtered by an address.
     *
     * @param address The address filter to apply, or null for no filtering.
     * @return The closest DronePortBlockEntity matching the filter, or null if none found.
     */
    public DronePortBlockEntity getClosestDronePort(String address) {
        final DronePortBlockEntity[] closestDronePort = {null};
        level().getCapability(ModCapabilities.DRONE_PORT_ENTITY_TRACKER_CAP)
                .ifPresent(tracker -> {
                    List<DronePortBlockEntity> allBEs = tracker.getAll();
                    closestDronePort[0] = allBEs.stream()
                            .filter(dpbe -> address == null || PackageItem.matchAddress(address, dpbe.addressFilter))
                            .min((dpbe1, dpbe2) -> Double.compare(
                                    dpbe1.getBlockPos().distSqr(this.blockPosition()),
                                    dpbe2.getBlockPos().distSqr(this.blockPosition())
                            ))
                            .orElse(null);
                });
        return closestDronePort[0];
    }

    @Override
    public void tick() {
        super.tick();
        state.tick(this);
        this.setDeltaMovement(targetVelocity);
        this.move(MoverType.SELF, targetVelocity);

        this.setYRot(this.entityData.get(ROT_YAW));
    }

    public void setState(RoboEntityState state) {
        if (state == null) return;
        this.state = state;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null) return;
        this.itemStack = itemStack;
    }
    public DronePortBlockEntity getStartDronePortBlockEntity() {
        return startDronePortBlockEntity;
    }

    public void setTargetVelocity(Vec3 targetVelocity) {
        if (targetVelocity == null) return;
        this.targetVelocity = targetVelocity;
    }

    public int getSnapAngle(double angle) {
        return (int) Math.abs(Math.round(angle / 90) * 90 - 45);
    }

    public double getAngleToTarget() {
        BlockPos targetPos = getTargetPosition();
        return targetPos != null
                ? Math.atan2(targetPos.getZ() - this.getZ(), targetPos.getX() - this.getX())
                : 0;
    }

    @Override
    public void remove(RemovalReason pReason) {
        if (pReason == RemovalReason.KILLED) {
            if (itemStack != null) {
                level().addFreshEntity(new ItemEntity(level(), this.getX(), this.getY(), this.getZ(), itemStack));
                if (this.targetPlayer != null) {
                    targetPlayer.displayClientMessage(Component.literal("Robo Bee died at " + this.getX() + " " + this.getY() + " " + this.getZ() + " with a Package for " + targetPlayer.getName()), false);
                }
            }
        }
        super.remove(pReason);
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }
    public DronePortBlockEntity getTargetBlockEntity() {
        return targetBlockEntity;
    }

    public void setTargetPlayer(Player targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    public void setTargetBlockEntity(DronePortBlockEntity targetBlockEntity) {
        this.targetBlockEntity = targetBlockEntity;
    }

    public void updateDisplay(Player player) {
        if (player == null) return;
        player.displayClientMessage(Component.literal("Package will arrive in " + (calcETA(player)) + "s"), true);
    }

    private int calcETA(Player player) {
        if (player == null) return Integer.MAX_VALUE;
        double distance = player.position().distanceTo(this.position());
        return (int) (distance / CMPConfigs.server().droneSpeed.get()) + 1;
    }

    public void lookAtTarget(){
        if (level().isClientSide()) return;
        BlockPos targetPos = getTargetPosition();
        if (targetPos != null) {
            Vec3 direction = new Vec3(targetPos.getX(), targetPos.getY(), targetPos.getZ()).subtract(this.position()).normalize();
            this.entityData.set(ROT_YAW, (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90);
        }
    }

    /**
     * Rotates the RoboEntity to face its target position.
     * Calculates the shortest rotation angle to the target and adjusts the yaw
     * incrementally based on a fixed rotation speed. Returns the number of ticks
     * required to complete the rotation.
     *
     * @return The number of ticks remaining to complete the rotation, or 0 if already aligned.
     */
    public int rotateLookAtTarget(){
        if (level().isClientSide()) return -1;
        float currentYaw = this.getYRot();
        float targetYaw = (float) getAngleToTarget();
        float deltaYaw = targetYaw - currentYaw;
        if (deltaYaw > 180) {
            deltaYaw -= 360;
        } else if (deltaYaw < -180) {
            deltaYaw += 360;
        }
        float rotationSpeed = 1f; // degrees per tick
        if (Math.abs(deltaYaw) > rotationSpeed) {
            if (deltaYaw > 0) {
                currentYaw += rotationSpeed;
            } else {
                currentYaw -= rotationSpeed;
            }
        } else {
            currentYaw = targetYaw;
            this.entityData.set(ROT_YAW,currentYaw);

            return 0;
        }
        this.entityData.set(ROT_YAW,currentYaw);
        return (int) Math.ceil(Math.abs(deltaYaw) / rotationSpeed);
    }

    public int rotateToSnap(){
        if (level().isClientSide()) return -1;
        float currentYaw = this.getYRot();
        float targetYaw = (float) getSnapAngle(getAngleToTarget());
        float deltaYaw = targetYaw - currentYaw;
        if (deltaYaw > 180) {
            deltaYaw -= 360;
        } else if (deltaYaw < -180) {
            deltaYaw += 360;
        }
        float rotationSpeed = 1f; // degrees per tick
        if (Math.abs(deltaYaw) > rotationSpeed) {
            if (deltaYaw > 0) {
                currentYaw += rotationSpeed;
            } else {
                currentYaw -= rotationSpeed;
            }
        } else {
            currentYaw = targetYaw;
            this.entityData.set(ROT_YAW,currentYaw);
            return 0;
        }
        this.entityData.set(ROT_YAW,currentYaw);
        return (int) Math.ceil(Math.abs(deltaYaw) / rotationSpeed);
    }
}
