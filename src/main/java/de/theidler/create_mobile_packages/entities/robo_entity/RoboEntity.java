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

    private static final EntityDataAccessor<Float> ROT_YAW = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.FLOAT);

    private RoboEntityState state;
    private Vec3 targetVelocity = Vec3.ZERO;
    private ItemStack itemStack;
    private Player targetPlayer;
    private DronePortBlockEntity targetBlockEntity;
    private DronePortBlockEntity startDronePortBlockEntity;

    /**
     * Constructor for RoboEntity. Used for spawning the entity.
     *
     * @param type      The entity type.
     * @param level     The level in which the entity exists.
     * @param itemStack The ItemStack (package) used to determine the target.
     * @param spawnPos  The spawn position of the entity.
     */
    public RoboEntity(EntityType<? extends Mob> type, Level level, ItemStack itemStack, BlockPos spawnPos) {
        super(type, level);
        setItemStack(itemStack);
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
        level().players().stream()
                .filter(player -> player.getName().getString().equals(PackageItem.getAddress(itemStack)))
                .findFirst()
                .ifPresentOrElse(player -> targetPlayer = player,
                        () -> targetBlockEntity = getClosestDronePort(PackageItem.getAddress(itemStack)));
    }

    /**
     * Gets the position of the current target.
     * If no target is set, it defaults to the closest drone port.
     *
     * @return The block position of the target.
     */
    public BlockPos getTargetPosition() {
        if (targetPlayer != null) return targetPlayer.blockPosition().above();
        if (targetBlockEntity != null) return targetBlockEntity.getBlockPos().above();
        targetBlockEntity = getClosestDronePort();
        return targetBlockEntity != null ? targetBlockEntity.getBlockPos().above() : null;
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
        float rotYaw = this.entityData.get(ROT_YAW);
        this.setYRot(rotYaw);
        this.setYHeadRot(rotYaw);
        this.yBodyRot = rotYaw;
    }

    /**
     * Sets the current state of the RoboEntity.
     *
     * @param state The new state to set.
     */
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

    /**
     * Calculates the snap angle for a given angle. (45, 135, 225, 315)
     *
     * @param angle The angle to snap.
     * @return The snapped angle.
     */
    public int getSnapAngle(double angle) {
        return (int) Math.abs(Math.round(angle / 90) * 90 - 45);
    }

    /**
     * Calculates the angle to the current target.
     *
     * @return The angle to the target.
     */
    public double getAngleToTarget() {
        BlockPos targetPos = getTargetPosition();
        return targetPos != null
                ? Math.atan2(targetPos.getZ() - this.getZ(), targetPos.getX() - this.getX())
                : 0;
    }

    @Override
    public void remove(RemovalReason pReason) {
        if (pReason == RemovalReason.KILLED && itemStack != null) {
            level().addFreshEntity(new ItemEntity(level(), this.getX(), this.getY(), this.getZ(), itemStack));
            if (this.targetPlayer != null) {
                targetPlayer.displayClientMessage(Component.literal("Robo Bee died at " + this.getX() + " " + this.getY() + " " + this.getZ() + " with a Package for " + targetPlayer.getName()), false);
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

    /**
     * Updates the display message for the specified player with the estimated time of arrival.
     *
     * @param player The player to update.
     */
    public void updateDisplay(Player player) {
        if (player == null) return;
        player.displayClientMessage(Component.literal("Package will arrive in " + (calcETA(player)) + "s"), true);
    }

    /**
     * Calculates the estimated time of arrival (ETA) to the specified player.
     *
     * @param player The player to calculate the ETA for.
     * @return The ETA in seconds.
     */
    private int calcETA(Player player) {
        if (player == null) return Integer.MAX_VALUE;
        double distance = player.position().distanceTo(this.position());
        return (int) (distance / CMPConfigs.server().droneSpeed.get()) + 1;
    }

    /**
     * Instantly rotates the RoboEntity to look at its target.
     */
    public void lookAtTarget(){
        if (level().isClientSide()) return;
        BlockPos targetPos = getTargetPosition();
        if (targetPos != null) {
            Vec3 direction = new Vec3(targetPos.getX(), targetPos.getY(), targetPos.getZ()).subtract(this.position()).normalize();
            this.entityData.set(ROT_YAW, (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90);
        }
    }

    /**
     * Rotates the RoboEntity to face its target.
     *
     * @return The number of ticks required to complete the rotation.
     */
    public int rotateLookAtTarget(){
        return rotateToAngle((float) getAngleToTarget()+90);
    }

    /**
     * Rotates the RoboEntity to the nearest snap angle.
     *
     * @return The number of ticks required to complete the rotation.
     */
    public int rotateToSnap(){
        return rotateToAngle((float) getSnapAngle(getAngleToTarget())+90);
    }

    /**
     * Rotates the RoboEntity to a specified yaw angle.
     *
     * @param targetYaw The target yaw angle.
     * @return The number of ticks required to complete the rotation.
     */
    private int rotateToAngle(float targetYaw) {
        if (level().isClientSide()) return -1;
        float currentYaw = this.entityData.get(ROT_YAW);
        float deltaYaw = targetYaw - currentYaw;
        deltaYaw = (deltaYaw > 180) ? deltaYaw - 360 : (deltaYaw < -180) ? deltaYaw + 360 : deltaYaw;
        float rotationSpeed = CMPConfigs.server().droneRotationSpeed.get();
        if (Math.abs(deltaYaw) > rotationSpeed) {
            currentYaw += (deltaYaw > 0) ? rotationSpeed : -rotationSpeed;
        } else {
            currentYaw = targetYaw;
        }
        this.entityData.set(ROT_YAW, currentYaw);
        return (int) Math.ceil(Math.abs(deltaYaw) / rotationSpeed);
    }
}
