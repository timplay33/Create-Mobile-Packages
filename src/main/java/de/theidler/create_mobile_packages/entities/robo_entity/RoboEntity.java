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
        this.setYRot(getSnapAngle(getAngleToTarget()));
        // don't fly out of the port if target is origin
        if (targetBlockEntity != null && targetBlockEntity.equals(startDronePortBlockEntity)) {
            setState(new LandingDecendFinishState());
        }
        if (startDronePortBlockEntity == null) {
            setState(new AdjustRotationToTarget());
        }
        setState(new LaunchPrepareState());
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
            return targetPlayer.blockPosition();
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
     * @param addressFilter The address filter to apply, or null for no filtering.
     * @return The closest DronePortBlockEntity matching the filter, or null if none found.
     */
    public DronePortBlockEntity getClosestDronePort(String addressFilter) {
        final DronePortBlockEntity[] closestDronePort = {null};
        level().getCapability(ModCapabilities.DRONE_PORT_ENTITY_TRACKER_CAP)
                .ifPresent(tracker -> {
                    List<DronePortBlockEntity> allBEs = tracker.getAll();
                    closestDronePort[0] = allBEs.stream()
                            .filter(dpbe -> addressFilter == null || addressFilter.equals(dpbe.addressFilter))
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

    public double getAngleToTarget(){
        if (targetBlockEntity != null) {
            return Math.atan2(targetBlockEntity.getBlockPos().getZ() - this.getY(), targetBlockEntity.getBlockPos().getX() - this.getX());
        } else if (targetPlayer != null) {
            return Math.atan2(targetPlayer.getY() - this.getY(), targetPlayer.getX() - this.getX());
        } else {
            return 0;
        }
    }

    @Override
    public void remove(RemovalReason pReason) {
        if (pReason == RemovalReason.KILLED) {
            if (itemStack != null) {
                level().addFreshEntity(new ItemEntity(level(), this.getX(), this.getY(), this.getZ(), itemStack));
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
}
