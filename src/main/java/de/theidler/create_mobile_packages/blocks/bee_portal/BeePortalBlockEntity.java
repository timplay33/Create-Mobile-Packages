package de.theidler.create_mobile_packages.blocks.bee_portal;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.Location;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import static de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlock.IS_OPEN_TEXTURE;
import static de.theidler.create_mobile_packages.index.CMPBlockEntities.beePortStorage;

/**
 * Represents a Drone Port block entity that handles the processing and sending of Create Mod packages
 * to players or other drone ports using drones.
 */
public class BeePortalBlockEntity extends BlockEntity {
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

    public void sendDrone(RoboEntity re) {
        if (roboBeeInventory.getStackInSlot(0).getCount() <= 0) {
            return;
        }

        BeePortBlockEntity targetBlock = re.getTargetBlockEntity();
        Level targetLevel = targetBlock.getLevel();
        if (targetLevel == null) return;
        CMPPackets.getChannel()
                .sendToServer(new RequestDimensionTeleport(targetLevel.dimension().location(), getBlockPos().getCenter(), targetBlock.getBlockPos(), re.getItemStack()));
    }

    /**
     * Called when the block entity is loaded. Registers the entity with the tracker.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null) return;
        beePortStorage.addBeePortal(this, level.dimensionType());
    }

    @Override
    public void setRemoved() {
        if (level == null) return;
        if (!level.isClientSide) {
            beePortStorage.removeBeePortal(this, level.dimensionType());
            if (roboBeeInventory.getStackInSlot(0).getCount() > 0) {
                level.addFreshEntity(new ItemEntity(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), roboBeeInventory.getStackInSlot(0)));
            }
        }
        super.setRemoved();
    }

    public void addBeeToRoboBeeInventory(int amount) {
        roboBeeInventory.insertItem(0, new ItemStack(CMPItems.ROBO_BEE.get(), amount), false);
    }

    public Location location() {
        Level level = getLevel();
        return level == null ? new Location(getBlockPos(), null) : new Location(getBlockPos(), level.dimensionType());
    }
}
