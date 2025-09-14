package de.theidler.create_mobile_packages.robo;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.CMPHelper;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import de.theidler.create_mobile_packages.entities.robo_entity.states.AdjustRotationToTarget;
import de.theidler.create_mobile_packages.entities.robo_entity.states.LandingDescendFinishState;
import de.theidler.create_mobile_packages.entities.robo_entity.states.LaunchPrepareState;
import de.theidler.create_mobile_packages.index.CMPEntities;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.UUID;

import static de.theidler.create_mobile_packages.CMPHelper.readVec3FromTag;
import static de.theidler.create_mobile_packages.CMPHelper.writeVec3ToTag;


public class VirtualRobo {
    private final UUID id;
    private final UUID logisticsNetworkId;
    private BeePortBlockEntity targetBlockEntity;
    private BeePortBlockEntity startBeePortBlockEntity;
    private Player targetPlayer;
    private ItemStack itemStack = ItemStack.EMPTY;
    private Vec3 currentPos = Vec3.ZERO;
    private float yaw;
    private float pitch;
    private UUID entityId; // if a RoboEntity is spawned
    private int speed;
    private RoboEntityState state;
    private String targetAddress;
    private String activeTargetAddress;
    private Vec3 targetVelocity = Vec3.ZERO;
    private boolean isRequest = true;
    private ServerLevel serverLevel;
    private float packageHeightScale;

    public VirtualRobo(ServerLevel level, UUID id, ItemStack itemStack, BlockPos spawnPos, BlockPos targetPos, UUID logisticsNetworkId) {
        this.id = id;
        this.logisticsNetworkId = logisticsNetworkId;
        this.serverLevel = level;
        this.speed = CMPConfigs.server().beeSpeed.get();
        if (targetPos != null) {
            this.targetBlockEntity = level.getBlockEntity(targetPos) instanceof BeePortBlockEntity dpbe ? dpbe : null;
            if (this.targetBlockEntity != null) {
                setState(new LaunchPrepareState());
            }
        }
        this.itemStack = itemStack;
        setTargetFromItemStack(itemStack);
        this.currentPos = spawnPos.getCenter().subtract(0, 0.5, 0);
        if (targetBlockEntity != null) {
            targetBlockEntity.trySetEntityOnTravel(this);
        }
        if (level.getBlockEntity(spawnPos) instanceof BeePortBlockEntity dpbe) {
            startBeePortBlockEntity = dpbe;
        }
        this.yaw = getSnapAngle(getAngleToTarget());
        // don't fly out of the port if target is origin
        if (targetBlockEntity != null && targetBlockEntity.equals(startBeePortBlockEntity)) {
            setState(new LandingDescendFinishState());
            return;
        }
        if (startBeePortBlockEntity == null) {
            setState(new AdjustRotationToTarget());
            return;
        }
        setState(new LaunchPrepareState());
    }

    public static VirtualRobo deserializeNBT(ServerLevel level, CompoundTag roboTag) {
        UUID id = roboTag.getUUID("id");
        Vec3 pos = readVec3FromTag(roboTag, "pos");
        int speed = roboTag.getInt("speed");
        UUID logisticsNetworkId = roboTag.getUUID("logisticsNetworkId");

        ItemStack itemStack = ItemStack.EMPTY;
        if (roboTag.contains("itemStack", Tag.TAG_COMPOUND)) {
            itemStack = (ItemStack.of(roboTag.getCompound("itemStack")));
        }

        VirtualRobo virtualRobo = new VirtualRobo(level, id, itemStack, BlockPos.containing(pos), null, logisticsNetworkId);
        virtualRobo.setSpeed(speed);
        virtualRobo.setTargetFromItemStack(virtualRobo.getItemStack());
        if (!virtualRobo.getItemStack().isEmpty()) {
            virtualRobo.setPackageHeightScale(1.0f);
        }
        virtualRobo.setState(new AdjustRotationToTarget());
        return virtualRobo;
    }

    /**
     * Calculates the snap angle for a given angle. (45, 135, 225, 315)
     *
     * @param angle The angle to snap.
     * @return The snapped angle.
     */
    private int getSnapAngle(double angle) {
        return (int) Math.abs(Math.round(angle / 90) * 90 - 45);
    }

    /**
     * Calculates the angle to the current target.
     *
     * @return The angle to the target.
     */
    private double getAngleToTarget() {
        BlockPos targetPos = getTargetPosition();
        return targetPos != null ? Math.atan2(targetPos.getZ() - this.currentPos.z, targetPos.getX() - this.currentPos.x()) : 0;
    }

    public BlockPos getTargetPosition() {
        updateTarget();
        if (targetPlayer != null) {
            return CMPHelper.isWithinRange(targetPlayer.blockPosition(), BlockPos.containing(currentPos)) ? targetPlayer.blockPosition().above().above() : null;
        }
        if (targetBlockEntity != null) {
            return CMPHelper.isWithinRange(targetBlockEntity.getBlockPos(), BlockPos.containing(currentPos)) ? targetBlockEntity.getBlockPos().above().above() : null;
        }
        return null;
    }

    private void setTargetFromItemStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) setTargetAddress(null);
        else setTargetAddress(PackageItem.getAddress(itemStack));
    }

    private void updateTarget() {
        targetPlayer = getTargetPlayerFromAddress();
        if (targetPlayer != null) {
            return;
        }
        if (targetBlockEntity == null || targetBlockEntity.isRemoved() || !targetBlockEntity.canAcceptEntity(this, !itemStack.isEmpty()) || !Objects.equals(activeTargetAddress, targetAddress)) {
            BeePortBlockEntity oldTarget = targetBlockEntity;
            activeTargetAddress = targetAddress;
            targetBlockEntity = CMPHelper.getClosestBeePort(serverLevel, targetAddress, BlockPos.containing(currentPos), this);
            if (oldTarget != targetBlockEntity) {
                if (oldTarget != null) {
                    oldTarget.trySetEntityOnTravel(null);
                }
                if (targetBlockEntity != null) {
                    targetBlockEntity.trySetEntityOnTravel(this);
                }
            }
            if (targetBlockEntity == null && targetPlayer == null) {
                setTargetVelocity(Vec3.ZERO);
            }
        }
        if (!isRequest) {
            // Check if there is a new target block entity that is closer than the current one
            BeePortBlockEntity newTargetBlockEntity = CMPHelper.getClosestBeePort(serverLevel, targetAddress, BlockPos.containing(currentPos), this);
            if (newTargetBlockEntity != null && newTargetBlockEntity != targetBlockEntity) {
                if (targetBlockEntity != null) {
                    targetBlockEntity.trySetEntityOnTravel(null);
                }
                targetBlockEntity = newTargetBlockEntity;
                targetBlockEntity.trySetEntityOnTravel(this);
            }
        }
    }

    private Player getTargetPlayerFromAddress() {
        return serverLevel.players().stream().filter(player -> BeePortBlockEntity.doesAddressStringMatchPlayerName(player, PackageItem.getAddress(this.itemStack))).findFirst().orElse(null);
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setTargetVelocity(Vec3 targetVelocity) {
        if (targetVelocity == null) return;
        this.targetVelocity = targetVelocity;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null) return;
        this.itemStack = itemStack;
    }

    public void setRequest(boolean isRequest) {
        this.isRequest = isRequest;
    }

    public void tick(ServerLevel level) {
        this.serverLevel = level;
        updateEntity();

        if (state != null) state.tick(this);
        //this.setDeltaMovement(targetVelocity);
        this.move(targetVelocity);
        //updateNametag(); -> client side only

        // Spawn / despawn RoboEntity if needed
        BlockPos pos = BlockPos.containing(currentPos);
        if (level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            if (entityId == null) {
                spawnAndRememberEntity();
            }
        } else if (entityId != null) {
            despawnEntity();
        }
    }

    private void move(Vec3 targetVelocity) {
        this.currentPos = this.currentPos.add(targetVelocity);
    }

    private void updateEntity() {
        if (this.entityId != null && (serverLevel.getEntity(entityId) instanceof RoboEntity roboEntity)) {
            roboEntity.syncFromVirtual(this);
        } else {
            entityId = null;
        }
    }

    public void setState(RoboEntityState state) {
        this.state = state;
    }

    private void moveTo(BlockPos pos) {
        Vec3 targetVec = Vec3.atCenterOf(pos);
        Vec3 direction = targetVec.subtract(currentPos);
        double distance = direction.length();
        if (distance < 0.1) {
            currentPos = targetVec;
            return;
        }
        direction = direction.normalize();
        double moveDistance = Math.min(speed / 20.0, distance);
        currentPos = currentPos.add(direction.scale(moveDistance));
        yaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90;
        pitch = (float) Math.toDegrees(Math.asin(direction.y));
    }

    public void despawnEntity() {
        Entity entity = serverLevel.getEntity(entityId);
        if (entity != null) {
            entity.discard();
        }
        entityId = null;
    }

    private void spawnAndRememberEntity() {
        Entity entity = new RoboEntity(CMPEntities.ROBO_BEE_ENTITY.get(), serverLevel, id);
        entity.setPos(currentPos.x, currentPos.y, currentPos.z);
        serverLevel.addFreshEntity(entity);
        this.entityId = entity.getUUID();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        writeVec3ToTag(tag, "pos", currentPos);
        tag.putInt("speed", speed);
        tag.putUUID("logisticsNetworkId", logisticsNetworkId);
        if (!getItemStack().isEmpty()) {
            tag.put("itemStack", getItemStack().save(new CompoundTag()));
        }
        return tag;
    }

    private void setSpeed(int speed) {
        this.speed = speed;
    }

    public UUID getLogisticsNetworkId() {
        return logisticsNetworkId;
    }

    public Vec3 getCurrentPos() {
        return currentPos;
    }

    public UUID getId() {
        return id;
    }

    /**
     * Rotates the RoboEntity to face its target.
     *
     * @return The number of ticks required to complete the rotation.
     */
    public int rotateLookAtTarget() {
        return rotateToAngle((float) getAngleToTarget() + 90);
    }

    /**
     * Rotates the RoboEntity to a specified yaw angle.
     *
     * @param targetYaw The target yaw angle.
     * @return The number of ticks required to complete the rotation.
     */
    private int rotateToAngle(float targetYaw) {
        float currentYaw = this.yaw;
        float deltaYaw = targetYaw - currentYaw;
        deltaYaw = (deltaYaw > 180) ? deltaYaw - 360 : (deltaYaw < -180) ? deltaYaw + 360 : deltaYaw;
        float rotationSpeed = CMPConfigs.server().beeRotationSpeed.get();
        if (Math.abs(deltaYaw) > rotationSpeed) {
            currentYaw += (deltaYaw > 0) ? rotationSpeed : -rotationSpeed;
        } else {
            currentYaw = targetYaw;
        }
        this.yaw = currentYaw;
        return (int) Math.ceil(Math.abs(deltaYaw) / rotationSpeed);
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }

    public BeePortBlockEntity getTargetBlockEntity() {
        return targetBlockEntity;
    }

    public void lookAtTarget() {
        BlockPos targetPos = getTargetPosition();
        if (targetPos != null) {
            Vec3 direction = new Vec3(targetPos.getX(), targetPos.getY(), targetPos.getZ()).subtract(this.currentPos).normalize();
            this.yaw = (float) (Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90);
        }
    }

    public ServerLevel getServerLevel() {
        return serverLevel;
    }

    public void setPos(Vec3 pos) {
        this.currentPos = pos;
    }

    /**
     * Rotates the RoboEntity to the nearest snap angle.
     *
     * @return The number of ticks required to complete the rotation.
     */
    public int rotateToSnap() {
        return rotateToAngle((float) getSnapAngle(getAngleToTarget()) + 90);
    }

    public BeePortBlockEntity getStartBeePortBlockEntity() {
        return startBeePortBlockEntity;
    }

    public void setRemoved(ServerLevel level) {
        RoboManager.get(level).remove(this.getId());
        getTargetBlockEntity().trySetEntityOnTravel(null);
        despawnEntity();
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    /**
     * Sets the target for the RoboEntity based on the provided address.
     * If the address is null, the target is set to the closest drone port.
     * Otherwise, it attempts to find a player or drone port matching the address.
     *
     * @param address the target address
     */
    public void setTargetAddress(String address) {
        this.targetAddress = address;
        updateTarget();
    }

    public float getPackageHeightScale() {
        return packageHeightScale;
    }

    public void setPackageHeightScale(float scale) {
        if (scale < 0.0f || scale > 1.0f) return;
        this.packageHeightScale = scale;
    }
}
