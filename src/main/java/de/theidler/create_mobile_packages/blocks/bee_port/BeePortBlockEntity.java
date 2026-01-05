package de.theidler.create_mobile_packages.blocks.bee_port;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import de.theidler.create_mobile_packages.CMPHelper;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import de.theidler.create_mobile_packages.items.robo_bee.RoboBeeItem;
import de.theidler.create_mobile_packages.robo.RoboManager;
import de.theidler.create_mobile_packages.robo.VirtualRobo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlock.IS_OPEN_TEXTURE;

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
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (slot < inventory.getSlots()) {
                return inventory.getStackInSlot(slot);
            } else {
                return roboBeeInventory.getStackInSlot(slot - inventory.getSlots());
            }
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
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
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
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
    public LogisticallyLinkedBehaviour behaviour;
    private int tickCounter = 0; // Counter to track ticks for periodic processing.
    private int roboSendCooldown = 0; // Flag to indicate if an item was sent this time.

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

    /**
     * Sets the open state of the drone port and updates the block state and sound.
     *
     * @param entity The drone port entity.
     * @param open   Whether the port is open.
     */
    public static void setOpen(BeePortBlockEntity entity, boolean open) {
        if (entity == null || entity.level == null) return;

        if (entity.isRemoved()) return;

        entity.level.setBlockAndUpdate(entity.getBlockPos(), entity.getBlockState().setValue(IS_OPEN_TEXTURE, open));
        entity.level.playSound(null, entity.getBlockPos(), open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS);

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
        player.displayClientMessage(Component.translatableWithFallback("create_mobile_packages.bee_port.send_items", "Send Items to Player"), true);

        if (isPlayerInventoryFull(player)) {
            return false;
        }
        player.getInventory().add(itemStack);
        return true;
    }

    private synchronized void requestRoboEntity() {
        if (level instanceof ServerLevel serverLevel) {
            RoboManager.get(serverLevel).requestRobo(this.getBlockPos(), this.getLogisticsNetworkId());
        }
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
        if (level instanceof ServerLevel serverLevel) {
            List<Integer> eta = RoboManager.get(serverLevel).getETAs(this.getBlockPos());
            // find min eta and set it
            int minEta = eta.stream().min(Comparator.naturalOrder()).orElse(-1);
            this.data.set(0, minEta);
            this.data.set(1, eta.isEmpty() ? 0 : 1);
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(behaviour = new LogisticallyLinkedBehaviour(this, true));
        super.addBehaviours(behaviours);
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
                if (stackInSlot.isEmpty()) continue;
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(adjacentInventory, stackInSlot, false);
                if (remainder.isEmpty() && level != null) {
                    inventory.extractItem(i, 1, false);
                    level.blockEntityChanged(worldPosition);
                }
            }
        }
    }

    private void tryPullingFromAdjacentInventories() {
        getAdjacentInventories().forEach((inventory) -> {
            if (inventory == null) return;
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack itemStack = inventory.getStackInSlot(i);
                if (!itemStack.isEmpty() && PackageItem.isPackage(itemStack)) {
                    ItemStack extractSim = inventory.extractItem(i, 1, true);
                    if (!extractSim.isEmpty() && addItemStack(extractSim, true)) {
                        addItemStack(inventory.extractItem(i, 1, false), false);
                    }
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
        if (level == null) return null;
        BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(side));
        if (blockEntity == null || blockEntity instanceof FrogportBlockEntity) return null;
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side.getOpposite()).orElse(null);
    }

    /**
     * Processes items in the inventory by attempting to send them to their destination.
     */
    private void processItems() {
        if (level == null || level.isClientSide) return;

        for (int i = 0; i < inventory.getSlots(); i++) {
            if (roboSendCooldown-- > 0) {
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
        if (address.isBlank()) return; // return if the package has no address

        // Check if the item can be sent to a player.
        for (Player player : level.players()) {
            if (CMPHelper.doesAddressMatchPlayer(player, address) && CMPHelper.isWithinRange(player.blockPosition(), this.getBlockPos())) {
                sendToPlayer(player, itemStack, slot);
                return;
            }
        }

        // Check if the item can be sent to another drone port.
        if (CMPConfigs.server().portToPort.get() && !PackageItem.matchAddress(address, addressFilter)) {
            BeePortBlockEntity beePortBlockEntity = CMPHelper.getClosestBeePort(level, address, this.getBlockPos(), null, getLogisticsNetworkId());
            if (beePortBlockEntity != null && !beePortBlockEntity.isFull()) {
                sendDrone(itemStack, slot);
            }
        }
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
            if (!hasRoboRequest() && level != null) {
                requestRoboEntity();
                return;
            }
            return;
        }
        roboSendCooldown = 2;
        CreateMobilePackages.LOGGER.info("Sending package to player: {}", player.getName().getString());
        sendDrone(itemStack, slot);
    }

    private boolean hasRoboRequest() {
        if (level instanceof ServerLevel serverLevel) {
            return RoboManager.get(serverLevel).getRoboRequests(this.getBlockPos()).stream().anyMatch(roboRequest -> roboRequest.getStatus() == RoboRequest.Status.PENDING || roboRequest.getStatus() == RoboRequest.Status.IN_PROGRESS);
        }
        return false;
    }

    /**
     * Sends a Create Mod package.
     *
     * @param itemStack The Create Mod package to send.
     * @param slot      The inventory slot of the item.
     */
    private void sendDrone(ItemStack itemStack, int slot) {
        if (!tryConsumeDrone()) {
            if (!hasRoboRequest() && level != null) {
                requestRoboEntity();
                return;
            }
            return;
        }
        roboSendCooldown = 2;
        if (level instanceof ServerLevel serverLevel) {
            RoboManager.get(serverLevel).newRobo(serverLevel, itemStack, this.getBlockPos(), this.getLogisticsNetworkId(), 0);
        }
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
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
     * Adds a Create Mod package to the inventory if there is space.
     *
     * @param itemStack The Create Mod package to add.
     * @param simulate  Whether to simulate the addition.
     * @return True if the package was added, false otherwise.
     */
    public boolean addItemStack(ItemStack itemStack, boolean simulate) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                if (!simulate) {
                    inventory.insertItem(i, itemStack, false);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Handles changes to the open state of the drone port.
     *
     * @param open Whether the port is open.
     */
    @Override
    protected void onOpenChange(boolean open) {
        if (level == null) {
            return;
        }
        level.playSound(null, worldPosition, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS);
        setOpen(this, open);
    }

    /**
     * Called when the block entity is loaded. Registers the entity with the tracker.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            level.getCapability(ModCapabilities.BEE_PORT_ENTITY_TRACKER_CAP).ifPresent(tracker -> tracker.add(this));
        }
    }

    /**
     * Unregisters the entity from the tracker and halts any incoming bees.
     */
    private void invalidateTarget() {
        if (level != null) {
            level.getCapability(ModCapabilities.BEE_PORT_ENTITY_TRACKER_CAP).ifPresent(tracker -> tracker.remove(this));
        }

        if (level instanceof ServerLevel serverLevel) {
            RoboManager.get(serverLevel).getRoboRequests(this.getBlockPos()).forEach(roboRequest -> roboRequest.setStatus(RoboRequest.Status.CANCELLED));
        }
    }

    /**
     * Meant to be called when the bee port is broken. Drops any bees from the
     * inventory. Does not update the inventory.
     */
    private void dropBees() {
        ItemStack bees = roboBeeInventory.getStackInSlot(0);

        if (bees.getCount() > 0 && level != null) {
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), bees));
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (level != null && !level.isClientSide) {
            this.invalidateTarget();
        }
        super.onChunkUnloaded();
    }

    @Override
    public void remove() {
        if (level != null && !level.isClientSide) {
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
    public boolean canAcceptEntity(VirtualRobo entity, Boolean hasPackage) {
        if (this.isRemoved()) return false;
        if (entity == null) return hasPackage ? !isFull() : !hasFullRoboSlot(0);
        if (hasRoboRequest()) return false;
        return hasPackage ? !isFull() : !hasFullRoboSlot(0);
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

    public ContainerData getData() {
        return data;
    }

    public UUID getLogisticsNetworkId() {
        return behaviour.freqId;
    }

    @Override
    public InteractionResult use(Player player) {
        if (!behaviour.mayInteractMessage(player)) {
            return InteractionResult.SUCCESS;
        }
        return super.use(player);
    }

    public void handleRequest(RoboRequest request) {
        if (!tryConsumeDrone()) return; // return if there is no bee available
        request.setStatus(RoboRequest.Status.IN_PROGRESS);
        roboSendCooldown = 2;
        if (level instanceof ServerLevel serverLevel) {
            RoboManager.get(serverLevel).newRequestRobo(serverLevel, this.getBlockPos(), request);
        }
    }
}
