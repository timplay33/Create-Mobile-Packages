package de.theidler.create_mobile_packages.blocks.bee_portal;

import de.theidler.create_mobile_packages.entities.robo_entity.Location;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.index.CMPItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlock.IS_OPEN_TEXTURE;
import static de.theidler.create_mobile_packages.blocks.bee_portal.ModCapabilities.BEE_PORTAL_ENTITY_TRACKER_CAP;
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

    private static void requestRoboEntity(Level level, BlockPos blockPos) {
        level.getCapability(BEE_PORTAL_ENTITY_TRACKER_CAP).ifPresent(tracker -> {
            List<BeePortalBlockEntity> allBEs = new ArrayList<>(tracker.getAll());
            allBEs.removeIf(be -> be.getBlockPos().equals(blockPos));
            BeePortalBlockEntity target = allBEs.stream()
                    .min(Comparator.comparingDouble(a -> a.getBlockPos().distSqr(blockPos)))
                    .orElse(null);
        });
    }

    /**
     * Sets the open state of the drone port and updates the block state and sound.
     *
     * @param entity The drone port entity.
     * @param open   Whether the port is open.
     */
    public static void setOpen(BeePortalBlockEntity entity, boolean open) {
        if (entity == null || entity.level == null) return;

        entity.level.setBlockAndUpdate(entity.getBlockPos(), entity.getBlockState().setValue(IS_OPEN_TEXTURE, open));
        entity.level.playSound(null, entity.getBlockPos(), open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE,
                SoundSource.BLOCKS);

    }

    /**
     * Called when the block entity is loaded. Registers the entity with the tracker.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            level.getCapability(BEE_PORTAL_ENTITY_TRACKER_CAP).ifPresent(tracker -> tracker.add(this));
        }

        if (!beePortStorage.hasLevel(level)) {
            beePortStorage.addBeePortLevel(level);
            beePortStorage.addBeePortalLevel(level);
        }
        beePortStorage.addBeePortal(this, level);
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
