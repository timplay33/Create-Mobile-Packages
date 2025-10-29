package de.theidler.create_mobile_packages.robo;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.CMPHelper;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboBeeBehaviorController;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.index.CMPEntities;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static de.theidler.create_mobile_packages.CMPHelper.readVec3FromTag;
import static de.theidler.create_mobile_packages.CMPHelper.writeVec3ToTag;


public class VirtualRobo {
    private final UUID id;
    private final UUID logisticsNetworkId;
    private ItemStack itemStack;
    private Vec3 currentPos = Vec3.ZERO;
    private float yaw;
    private float pitch;
    private UUID entityId; // if a RoboEntity is spawned
    private int speed;
    private final RoboBeeBehaviorController behaviorController;
    private RoboTarget target;
    private String targetAddress;
    private Vec3 targetVelocity = Vec3.ZERO;
    private ServerLevel serverLevel;
    private float packageHeightScale;

    public VirtualRobo(ServerLevel level, UUID id, ItemStack itemStack, BlockPos spawnPos, UUID logisticsNetworkId) {
        this.id = id;
        this.logisticsNetworkId = logisticsNetworkId;
        this.serverLevel = level;
        this.speed = CMPConfigs.server().beeSpeed.get();
        this.itemStack = itemStack;
        setTargetFromItemStack(itemStack);
        this.currentPos = spawnPos.getCenter().subtract(0, 0.5, 0);
        this.behaviorController = new RoboBeeBehaviorController();
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

        VirtualRobo virtualRobo = new VirtualRobo(level, id, itemStack, BlockPos.containing(pos), logisticsNetworkId);
        virtualRobo.setSpeed(speed);
        if (!virtualRobo.getItemStack().isEmpty()) {
            virtualRobo.setPackageHeightScale(1.0f);
        }
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
        Vec3 targetPos = getTargetPosition();
        return targetPos != null ? Math.atan2(targetPos.z - this.currentPos.z, targetPos.x - this.currentPos.x()) : 0;
    }

    public Vec3 getTargetPosition() {
        updateTarget();
        return target.getTargetPos();
    }

    private void setTargetFromItemStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) setTargetAddress(null, false);
        else setTargetAddress(PackageItem.getAddress(itemStack), false);
    }

    private void updateTarget() {
        // if the target is still valid, do nothing
        if (target != null && target.isValid()) return;

        // check if the old target was a BeePortBlockEntity if so, then remove the reference
        if (target != null && target.asBeePortBlockEntity() != null) {
            target.asBeePortBlockEntity().trySetEntityOnTravel(this, false );
        }

        // try finding a Player first
        target = PlayerTarget.fromAddress(serverLevel, targetAddress);
        if (target.isValid()) {return;}

        // if no player found, try finding a BeePortBlockEntity within the network
        BeePortBlockEntity targetBlockEntity = CMPHelper.getClosestBeePort(serverLevel, targetAddress, BlockPos.containing(currentPos), this, logisticsNetworkId);
        if (targetBlockEntity != null) {
            target = new BeePortBlockEntityTarget(targetBlockEntity);
        }
        if (target.isValid() && target.asBeePortBlockEntity() != null) {
            target.asBeePortBlockEntity().trySetEntityOnTravel(this, true );
            return;
        }
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

    public void tick(ServerLevel level) {
        this.serverLevel = level;
        updateEntity();
        updateTarget();
        if (behaviorController != null) behaviorController.tick(this);
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
    public int getSpeed() {
        return speed;
    }

    public Vec3 getCurrentPos() {
        return currentPos;
    }

    public UUID getId() {
        return id;
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

    public @Nullable RoboTarget getTarget() {
        return target;
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
        if (serverLevel.getBlockEntity(BlockPos.containing(currentPos)) instanceof BeePortBlockEntity bpbe) {
            return bpbe;
        } else if (serverLevel.getBlockEntity(BlockPos.containing(currentPos.subtract(0,1,0))) instanceof BeePortBlockEntity bpbe) {
            return bpbe;
        } else if (serverLevel.getBlockEntity(BlockPos.containing(currentPos.subtract(0,2,0))) instanceof BeePortBlockEntity bpbe) {
            return bpbe;
        }
        return null;
    }

    public void setRemoved(ServerLevel level) {
        RoboManager.get(level).remove(this.getId());
        target.asBeePortBlockEntity().trySetEntityOnTravel(null, false );
        despawnEntity();
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(String address, boolean update) {
        this.targetAddress = address;
        if (update) {
            updateTarget();
        }
    }

    public float getPackageHeightScale() {
        return packageHeightScale;
    }

    public void setPackageHeightScale(float scale) {
        if (scale < 0.0f || scale > 1.0f) return;
        this.packageHeightScale = scale;
    }

    public void setTarget(RoboTarget target) {
        this.target = target;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
