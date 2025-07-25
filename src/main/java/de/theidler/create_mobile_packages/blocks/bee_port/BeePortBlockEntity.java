package de.theidler.create_mobile_packages.blocks.bee_port;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.states.AdjustRotationToTarget;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import de.theidler.create_mobile_packages.items.robo_bee.RoboBeeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlock.IS_OPEN_TEXTURE;
import static de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity.calcETA;

/**
 * Represents a Drone Port block entity that handles the processing and sending of Create Mod packages
 * to players or other drone ports using drones.
 */
public class BeePortBlockEntity extends PackagePortBlockEntity {

    private final ContainerData data = new SimpleContainerData(2);
    private final ItemStackHandler roboBeeInventory = new ItemStackHandler(1);
    private final IItemHandler handler = new IItemHandler() {
        @Override
        public int getSlots() {
            return inventory.getSlots() + roboBeeInventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot < inventory.getSlots()) {
                return inventory.getStackInSlot(slot);
            } else {
                return roboBeeInventory.getStackInSlot(slot - inventory.getSlots());
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.getItem() instanceof RoboBeeItem) {
                if (slot >= inventory.getSlots()) {
                    return roboBeeInventory.insertItem(slot - inventory.getSlots(), stack, simulate);
                } else {
                    return stack; // Reject insertion into defaultInventory
                }
            } else {
                if (slot < inventory.getSlots()) {
                    return inventory.insertItem(slot, stack, simulate);
                } else {
                    return stack; // Reject insertion into roboInventory
                }
            }
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < inventory.getSlots()) {
                return inventory.extractItem(slot, amount, simulate);
            } else {
                return roboBeeInventory.extractItem(slot - inventory.getSlots(), amount, simulate);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot < inventory.getSlots()) {
                return inventory.getSlotLimit(slot);
            } else {
                return roboBeeInventory.getSlotLimit(slot - inventory.getSlots());
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.getItem() instanceof RoboBeeItem) {
                if (slot >= inventory.getSlots()) {
                    return roboBeeInventory.isItemValid(slot - inventory.getSlots(), stack);
                } else {
                    return false;
                }
            } else {
                if (slot < inventory.getSlots()) {
                    return inventory.isItemValid(slot, stack);
                } else {
                    return false;
                }
            }
        }
    };
    private int tickCounter = 0; // Counter to track ticks for periodic processing.
    private int sendItemThisTime = 0; // Flag to indicate if an item was sent this time.
    private int entityOnTravelID = -1;

    /**
     * Constructor for the BeePortBlockEntity.
     *
     * @param pType       The type of the block entity.
     * @param pPos        The position of the block in the world.
     * @param pBlockState The state of the block.
     */
    public BeePortBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        itemHandler = LazyOptional.of(() -> handler);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.put("RoboBeeInventory", roboBeeInventory.serializeNBT());
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if (tag.contains("RoboBeeInventory")) {
            roboBeeInventory.deserializeNBT(tag.getCompound("RoboBeeInventory"));
        }
    }

    /**
     * Called every tick to perform periodic updates.
     * Processes items every 20 ticks.
     */
    @Override
    public void tick() {
        super.tick();
        if (++tickCounter % 20 == 0) {
            processItems();
        }
        //Update Client Data
        if (!level.isClientSide()) {
            if (this.getRoboEntity() != null)
                this.data.set(0, calcETA(this.getBlockPos().getCenter(), this.getRoboEntity().position()));
            this.data.set(1, this.getRoboEntity() != null ? 1 : 0);
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide()) return;
        if (level.hasNeighborSignal(worldPosition)) {
            tryPushingToAdjacentInventories();
        } else {
            tryPullingFromAdjacentInventories();
        }

    }

    private void tryPushingToAdjacentInventories() {
        boolean stackToPush = false;
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                stackToPush = true;
            }
        }
        if (!stackToPush) return;

        for (IItemHandler adjacentInventory : getAdjacentInventories()) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack stackInSlot = inventory.extractItem(i, 1, true);
                if (stackInSlot.isEmpty())
                    continue;
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(adjacentInventory, stackInSlot, false);
                if (remainder.isEmpty()) {
                    inventory.extractItem(i, 1, false);
                    level.blockEntityChanged(worldPosition);
                }
            }
        }
    }

    private void tryPullingFromAdjacentInventories() {
        RoboEntity currentEntity = this.getRoboEntity();
        if (hasFullInventory(currentEntity != null ? 1 : 0)) return;

        getAdjacentInventories().forEach(( inventory) -> {
            if (inventory == null) return;
            if (hasFullInventory(currentEntity != null  ? 1 : 0)) return;
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack itemStack = inventory.getStackInSlot(i);
                if (!itemStack.isEmpty() && PackageItem.isPackage(itemStack)) {
                    addItemStack(inventory.extractItem(i, 1, false));
                }
            }
        });
    }

    private List<IItemHandler> getAdjacentInventories() {
        List<IItemHandler> inventories = new java.util.ArrayList<>();
        for (Direction side : Direction.values()) {
            IItemHandler inventory = getAdjacentInventory(side);
            if (inventory != null) {
                inventories.add(inventory);
            }
        }
        return inventories;
    }
    private IItemHandler getAdjacentInventory(Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(side));
        if (blockEntity == null || blockEntity instanceof FrogportBlockEntity)
            return null;
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side.getOpposite())
                .orElse(null);
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
            if (doesAddressStringMatchPlayerName(player, address)
                    && RoboEntity.isWithinRange(player.blockPosition(), this.getBlockPos())) {
                sendToPlayer(player, itemStack, slot);
                return;
            }
        }

        // Check if the item can be sent to another drone port.
        if (CMPConfigs.server().portToPort.get() && !PackageItem.matchAddress(address, addressFilter)) {
            BeePortBlockEntity beePortBlockEntity = RoboEntity.getClosestBeePort(level, address, this.getBlockPos(), null);
            if (beePortBlockEntity != null && !beePortBlockEntity.isFull()) {
                sendDrone(itemStack, slot);
            }
        }
    }

    public static boolean doesAddressStringMatchPlayerName(Player player, String address) {
        String playerName = player.getName().getString();
        int atIndex = address.lastIndexOf('@');
        if (atIndex == -1) {
            return address.equals(playerName);
        }
        return address.substring(atIndex + 1).equals(playerName);
    }

    private static void requestRoboEntity(Level level, BlockPos blockPos) {
        level.getCapability(ModCapabilities.BEE_PORT_ENTITY_TRACKER_CAP).ifPresent(tracker -> {
            List<BeePortBlockEntity> allBEs = new ArrayList<>(tracker.getAll());
            allBEs.removeIf(be -> be.isRemoved());
            allBEs.removeIf(be -> be.getBlockPos().equals(blockPos));
            allBEs.removeIf(be -> be.getRoboBeeInventory().getStackInSlot(0).getCount() <= 0);
            BeePortBlockEntity target = allBEs.stream()
                    .min(Comparator.comparingDouble(a -> a.getBlockPos().distSqr(blockPos)))
                    .orElse(null);
            if (target != null) {
                target.sendDrone(blockPos, true);
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
        if (roboBeeInventory.getStackInSlot(0).getCount() <= 0) {
            if (this.getRoboEntity() == null) {
                requestRoboEntity(level, this.getBlockPos());
                return;
            }
            return;
        }
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
        if (!tryConsumeDrone()) {
            if (this.getRoboEntity() == null) {
                requestRoboEntity(level, this.getBlockPos());
                return;
            }
            return;
        }
        sendItemThisTime = 2;
        RoboBeeEntity drone = new RoboBeeEntity(level, itemStack, null, this.getBlockPos());
        level.addFreshEntity(drone);
        drone.setRequest(false);
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
    }
    private void sendDrone(BlockPos tagetPos, boolean request) {
        if (!tryConsumeDrone())
            return;
        sendItemThisTime = 2;
        RoboBeeEntity drone = new RoboBeeEntity(level, ItemStack.EMPTY, tagetPos, this.getBlockPos());
        level.addFreshEntity(drone);
        drone.setRequest(request);
    }

    /**
     * Tries to remove a drone from the inventory.
     * 
     * @return whether a drone was available
     */
    private boolean tryConsumeDrone() {
        ItemStack usedBee = roboBeeInventory.extractItem(0, 1, false);
        return !usedBee.isEmpty();
    }

    /**
     * Sets the open state of the drone port and updates the block state and sound.
     *
     * @param entity The drone port entity.
     * @param open   Whether the port is open.
     */
    public static void setOpen(BeePortBlockEntity entity, boolean open) {
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
            level.getCapability(ModCapabilities.BEE_PORT_ENTITY_TRACKER_CAP).ifPresent(tracker -> tracker.add(this));
        }
    }

    /**
     * Unregisters the entity from the tracker and halts any incoming bees.
     */
    private void invalidateTarget() {
        level
            .getCapability(ModCapabilities.BEE_PORT_ENTITY_TRACKER_CAP)
            .ifPresent(tracker -> tracker.remove(this));

        RoboEntity currentEntity = getRoboEntity();
        if (currentEntity != null) {
            currentEntity.setTargetVelocity(Vec3.ZERO);
            currentEntity.setState(new AdjustRotationToTarget());
        }
    }

    /**
     * Meant to be called when the bee port is broken. Drops any bees from the
     * inventory. Does not update the inventory.
     */
    private void dropBees() {
        ItemStack bees = roboBeeInventory.getStackInSlot(0);

        if (bees.getCount() > 0) {
            level.addFreshEntity(new ItemEntity(
                level,
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ(),
                bees
            ));
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (!level.isClientSide) {
            this.invalidateTarget();
        }
        super.onChunkUnloaded();
    }

    @Override
    public void remove() {
        if (!level.isClientSide) {
            this.invalidateTarget();
        }
        super.remove();
    }

    @Override
    public void destroy() {
        this.dropBees();
        super.destroy();
    }

    /**
     * Checks if the drone port is full, considering a specified number of slots to leave empty.
     *
     * @param slotsToLeaveEmpty The number of slots that should remain empty.
     * @return True if the number of empty slots is less than or equal to the specified slots to leave empty, false otherwise.
     */
    public boolean hasFullInventory(int slotsToLeaveEmpty) {
        int emptySlots = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                emptySlots++;
            }
        }
        return emptySlots <= slotsToLeaveEmpty;
    }

    public boolean hasFullRoboSlot(int leaveEmpty) {
        return roboBeeInventory.getStackInSlot(0).getCount() >= roboBeeInventory.getSlotLimit(0) - leaveEmpty;
    }

    /**
     * Checks if the drone port is full.
     *
     * @return True if the drone port is full, false otherwise.
     */
    public boolean isFull() {
        return isFull(0);
    }

    public boolean isFull(int slotsToLeaveEmpty) {
        return hasFullInventory(slotsToLeaveEmpty) || hasFullRoboSlot(0);
    }

    /**
     * Checks if the drone port can accept a Create Mod package or a Robo-Bee.
     *
     * @param entity     The incoming RoboEntity (can be null).
     * @param hasPackage True if the entity carries a package, false otherwise.
     * @return True if the port can accept the entity, false otherwise.
     */
    public boolean canAcceptEntity(RoboEntity entity, Boolean hasPackage) {
        if (this.isRemoved()) return false;
        if (entity == null) return hasPackage ? !isFull() : !hasFullRoboSlot(0);
        RoboEntity currentEntity = getRoboEntity();
        if (currentEntity != null && currentEntity != entity) return false;
        return hasPackage ? !isFull() : !hasFullRoboSlot(0);
    }

    public synchronized boolean trySetEntityOnTravel(RoboEntity entity) {
        RoboEntity currentEntity = getRoboEntity();
        if (currentEntity == null || entity == null) {
            setRoboEntityOnTravel(entity);
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

    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return BeePortMenu.create(pContainerId, pPlayerInventory, this);
    }

    public RoboEntity getRoboEntity(){
        if (level == null || entityOnTravelID == -1) return null;
        Entity entity = level.getEntity(entityOnTravelID);
        if (entity instanceof RoboEntity roboEntity) {
            return roboEntity;
        }
        return null;
    }

    public void setRoboEntityOnTravel(RoboEntity entity) {
        if (entity == null) {
            this.entityOnTravelID = -1;
        } else {
            this.entityOnTravelID = entity.getId();
        }
    }

    public ContainerData getData() {
        return data;
    }
}
