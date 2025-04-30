package de.theidler.create_mobile_packages.blocks.drone_port;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import de.theidler.create_mobile_packages.index.CMPEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;

import static de.theidler.create_mobile_packages.blocks.drone_port.DronePortBlock.IS_OPEN_TEXTURE;

/**
 * Represents a Drone Port block entity that handles the processing and sending of Create Mod packages
 * to players or other drone ports using drones.
 */
public class DronePortBlockEntity extends PackagePortBlockEntity {

    private int tickCounter = 0; // Counter to track ticks for periodic processing.
    private int sendItemThisTime = 0; // Flag to indicate if an item was sent this time.

    /**
     * Constructor for the DronePortBlockEntity.
     *
     * @param pType       The type of the block entity.
     * @param pPos        The position of the block in the world.
     * @param pBlockState The state of the block.
     */
    public DronePortBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        itemHandler = LazyOptional.of(() -> inventory);
    }

    /**
     * Called every tick to perform periodic updates.
     * Processes items every 20 ticks.
     */
    @Override
    public void tick() {
        super.tick();
        if (++tickCounter >= 20) {
            tickCounter = 0;
            processItems();
        }
    }

    /**
     * Processes items in the inventory by attempting to send them to their destination.
     */
    private void processItems() {
        if (level == null || level.isClientSide) return;

        for (int i = 0; i < inventory.getSlots(); i++) {
            if (sendItemThisTime-- > 0) {
                return;
            }
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                sendItem(itemStack, i);
            }
        }
    }

    /**
     * Sends a Create Mod package to its destination, either to a player or another drone port.
     *
     * @param itemStack The Create Mod package to send.
     * @param slot      The inventory slot of the item.
     */
    private void sendItem(ItemStack itemStack, int slot) {
        if (level == null || !PackageItem.isPackage(itemStack)) return;
        String address = PackageItem.getAddress(itemStack);

        // Check if the item can be sent to a player.
        for (Player player : level.players()) {
            if (player.getName().getString().equals(address)) {
                sendToPlayer(player, itemStack, slot);
                return;
            }
        }

        // Check if the item can be sent to another drone port.
        level.getCapability(ModCapabilities.DRONE_PORT_ENTITY_TRACKER_CAP).ifPresent(tracker -> {
            List<DronePortBlockEntity> allBEs = tracker.getAll();
            if (allBEs.stream().anyMatch(dpbe -> PackageItem.matchAddress(address, dpbe.addressFilter) && dpbe != this)) {
                if (!PackageItem.matchAddress(address, this.addressFilter)) {
                    sendDrone(itemStack, slot);
                }
            }
        });
    }

    /**
     * Sends a Create Mod package to a player.
     *
     * @param player    The player to send the package to.
     * @param itemStack The Create Mod package to send.
     * @param slot      The inventory slot of the item.
     */
    private void sendToPlayer(Player player, ItemStack itemStack, int slot) {
        sendItemThisTime = 2;
        CreateMobilePackages.LOGGER.info("Sending package to player: {}", player.getName().getString());
        sendDrone(itemStack, slot);
    }

    /**
     * Sends a Create Mod package.
     *
     * @param itemStack The Create Mod package to send.
     * @param slot      The inventory slot of the item.
     */
    private void sendDrone(ItemStack itemStack, int slot) {
        sendItemThisTime = 2;
        RoboBeeEntity drone = new RoboBeeEntity(CMPEntities.ROBO_BEE_ENTITY.get(), level, itemStack, this.getBlockPos());
        level.addFreshEntity(drone);
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
    }

    /**
     * Sets the open state of the drone port and updates the block state and sound.
     *
     * @param entity The drone port entity.
     * @param open   Whether the port is open.
     */
    public static void setOpen(DronePortBlockEntity entity, boolean open) {
        if (entity == null || entity.level == null) return;

        entity.level.setBlockAndUpdate(entity.getBlockPos(), entity.getBlockState().setValue(IS_OPEN_TEXTURE, open));
        entity.level.playSound(null, entity.getBlockPos(), open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE,
                SoundSource.BLOCKS);

    }

    /**
     * Adds a Create Mod package to the inventory if there is space.
     *
     * @param itemStack The Create Mod package to add.
     * @return True if the package was added, false otherwise.
     */
    public boolean addItemStack(ItemStack itemStack){
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()){
                inventory.insertItem(i, itemStack, false);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the player's inventory is full.
     *
     * @param player The player to check.
     * @return True if the inventory is full, false otherwise.
     */
    public static boolean isPlayerInventoryFull(Player player) {
        return player.getInventory().items.stream().limit(player.getInventory().getContainerSize() - 5).noneMatch(ItemStack::isEmpty);
    }

/**
 * Sends a Create Mod package to a player. If the player's inventory is full, the item is not added.
 *
 * @param player    The player to send the package to. Must not be null.
 * @param itemStack The Create Mod package to send. Must not be empty.
 * @return True if the package was successfully sent to the player, false otherwise.
 */
public static boolean sendPackageToPlayer(Player player, ItemStack itemStack) {
    if (player == null || itemStack.isEmpty()) {
        return false;
    }
    player.displayClientMessage(Component.translatableWithFallback("create_mobile_packages.drone_port.send_items", "Send Items to Player"), true);

    if (isPlayerInventoryFull(player)) {
        return false;
    }
    player.getInventory().add(itemStack);
    return true;
}

    /**
     * Handles changes to the open state of the drone port.
     *
     * @param open Whether the port is open.
     */
    @Override
    protected void onOpenChange(boolean open) {
        if (level == null) { return; }
        level.playSound(null, worldPosition, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS);
        setOpen(this, open);
    }

    /**
     * Called when the block entity is loaded. Registers the entity with the tracker.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide){
            level.getCapability(ModCapabilities.DRONE_PORT_ENTITY_TRACKER_CAP).ifPresent(tracker -> tracker.add(this));
        }
    }

    /**
     * Called when the block entity is removed. Unregisters the entity from the tracker.
     */
    @Override
    public void remove() {
        if (!level.isClientSide) {
            level.getCapability(ModCapabilities.DRONE_PORT_ENTITY_TRACKER_CAP).ifPresent(tracker -> tracker.remove(this));
        }
        super.remove();
    }
}
