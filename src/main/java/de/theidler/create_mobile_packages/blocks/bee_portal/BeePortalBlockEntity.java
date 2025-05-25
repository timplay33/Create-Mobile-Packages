package de.theidler.create_mobile_packages.blocks.bee_portal;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.Location;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.states.AdjustRotationToTarget;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

import static de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlock.IS_OPEN_TEXTURE;
import static de.theidler.create_mobile_packages.index.CMPBlockEntities.beePortStorage;

/**
 * Represents a Drone Port block entity that handles the processing and sending of Create Mod packages
 * to players or other drone ports using drones.
 */
public class BeePortalBlockEntity extends BlockEntity {
    private RoboEntity entityOnTravel = null;
    private final ContainerData data = new SimpleContainerData(2);
    private final ItemStackHandler roboBeeInventory = new ItemStackHandler(1);

    /**
     * Constructor for the BeePortalBlockEntity.
     *
     * @param pType       The type of the block entity.
     * @param pPos        The position of the block in the world.
     * @param pBlockState The state of the block.
     */
    public BeePortalBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    private static void requestRoboEntity(Location location) {
        AtomicReference<BeePortalBlockEntity> result = new AtomicReference<>();
        beePortStorage.getAllBeePortals().forEach((currentLevel, allBEs) -> {
            allBEs.removeIf(be -> currentLevel == location.dimensionType());
            allBEs.removeIf(be -> be.getRoboBeeInventory().getStackInSlot(0).getCount() <= 0);
            result.set(allBEs.stream()
                    .min(Comparator.comparingDouble(a -> a.getBlockPos().distSqr(location.position())))
                    .orElse(null));
        });

//        if (result.get() != null) result.get().sendDrone(location);
    }

    /**
     * Sets the open state of the drone portal and updates the block state and sound.
     *
     * @param entity The drone portal entity.
     * @param open   Whether the portal is open.
     */
    public static void setOpen(BeePortalBlockEntity entity, boolean open) {
        if (entity == null || entity.level == null) return;

        entity.level.setBlockAndUpdate(entity.getBlockPos(), entity.getBlockState().setValue(IS_OPEN_TEXTURE, open));
        entity.level.playSound(null, entity.getBlockPos(), open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE,
                SoundSource.BLOCKS);
    }

    public void sendDrone(ItemStack itemStack) {
        if (roboBeeInventory.getStackInSlot(0).getCount() <= 0) {
//            if (this.entityOnTravel == null) requestRoboEntity(this.location());
            return;
        }

        RoboBeeEntity drone = new RoboBeeEntity(level, itemStack, null, this.getBlockPos());
        level.addFreshEntity(drone);
        roboBeeInventory.getStackInSlot(0).shrink(1);
    }

    public void sendDrone(RoboEntity re) {
        if (roboBeeInventory.getStackInSlot(0).getCount() <= 0) {
            return;
        }

        BeePortalBlockEntity targetPortal = re.getTargetPortalEntity();
        BeePortBlockEntity targetBlock = re.getTargetBlockEntity();
        Level targetLevel = targetBlock.getLevel();
        Vec3 exitPosition = targetPortal.getBlockPos().getCenter().multiply(1 / 8d, 1, 1 / 8d);
        BlockPos exitBlockPos = new BlockPos((int) Math.round(exitPosition.x), (int) Math.round(exitPosition.y), (int) Math.round(exitPosition.z));
        BeePortalBlockEntity exitPortal = RoboEntity.getClosestBeePortal(targetLevel.dimensionType(), new Location(exitBlockPos, targetLevel.dimensionType()));
        CMPPackets.getChannel()
                .sendToServer(new RequestDimensionTeleport(targetLevel.dimension().location(), exitPortal.getBlockPos().getCenter(), targetBlock.getBlockPos(), re.getItemStack()));
    }

    /**
     * Called when the block entity is loaded. Registers the entity with the tracker.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        beePortStorage.addBeePortal(this, level.dimensionType());
    }

    @Override
    public void setRemoved() {
        if (!level.isClientSide) {
            beePortStorage.removeBeePortal(this, level.dimensionType());
            if (entityOnTravel != null) {
                entityOnTravel.setTargetVelocity(Vec3.ZERO);
                entityOnTravel.setState(new AdjustRotationToTarget());
            }
            if (roboBeeInventory.getStackInSlot(0).getCount() > 0) {
                level.addFreshEntity(new ItemEntity(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), roboBeeInventory.getStackInSlot(0)));
            }
        }
        super.setRemoved();
    }

    public synchronized boolean trySetEntityOnTravel(RoboEntity entity) {
        if (entityOnTravel == null || entity == null) {
            entityOnTravel = entity;
            return true;
        }
        return false;
    }

    public ItemStackHandler getRoboBeeInventory() {
        return roboBeeInventory;
    }

    public void addBeeToRoboBeeInventory(int amount) {
        roboBeeInventory.insertItem(0, new ItemStack(CMPItems.ROBO_BEE.get(), amount), false);
    }

    public RoboEntity getRoboEntity() {
        return entityOnTravel;
    }

    public ContainerData getData() {
        return data;
    }

    public Location location() {
        return new Location(getBlockPos(), getLevel().dimensionType());
    }
}
