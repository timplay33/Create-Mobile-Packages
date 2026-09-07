package de.theidler.create_mobile_packages.blocks.bee_port;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import de.theidler.create_mobile_packages.CMPHelper;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPBlockEntities;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import de.theidler.create_mobile_packages.items.robo_bee.RoboBeeItem;
import de.theidler.create_mobile_packages.network_settings.NetworkHelper;
import de.theidler.create_mobile_packages.robo.BeePortBlockEntityTarget;
import de.theidler.create_mobile_packages.robo.PlayerNameCache;
import de.theidler.create_mobile_packages.robo.RoboManager;
import de.theidler.create_mobile_packages.robo.VirtualRobo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlock.IS_OPEN_TEXTURE;

/**
 * Represents a Drone Port block entity that handles the processing and sending of Create Mod packages
 * to players or other drone ports using drones.
 */
public class BeePortBlockEntity extends PackagePortBlockEntity {

    private static final int ROBOBEE_INVENTORY_STACK_SIZE = 64;
    private UUID placerUUID;

    private final ContainerData data = new SimpleContainerData(4);
    private final ItemStackHandler roboBeeInventory = new ItemStackHandler(1);
    private boolean beeReturnModeEnabled = false;

    private FilterMode filterMode = FilterMode.ALL;

    private final IItemHandler handler = new IItemHandler() {
        @Override
        public int getSlots() {
            return switch (filterMode) {
                case ALL -> inventory.getSlots() + roboBeeInventory.getSlots();
                case PACKAGES_ONLY -> inventory.getSlots();
                case ROBO_ONLY -> roboBeeInventory.getSlots();
            };
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return switch (filterMode) {
                case ALL -> {
                    if (slot < inventory.getSlots()) {
                        yield inventory.getStackInSlot(slot);
                    } else {
                        yield roboBeeInventory.getStackInSlot(slot - inventory.getSlots());
                    }
                }
                case PACKAGES_ONLY -> inventory.getStackInSlot(slot);
                case ROBO_ONLY -> roboBeeInventory.getStackInSlot(slot);
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return switch (filterMode) {
                case ALL -> {
                    if (stack.getItem() instanceof RoboBeeItem) {
                        if (slot >= inventory.getSlots()) {
                            yield roboBeeInventory.insertItem(slot - inventory.getSlots(), stack, simulate);
                        } else {
                            yield stack;
                        }
                    } else {
                        if (slot < inventory.getSlots()) {
                            yield inventory.insertItem(slot, stack, simulate);
                        } else {
                            yield stack;
                        }
                    }
                }
                case PACKAGES_ONLY -> {
                    if (stack.getItem() instanceof RoboBeeItem) {
                        yield stack;
                    } else {
                        yield inventory.insertItem(slot, stack, simulate);
                    }
                }
                case ROBO_ONLY -> {
                    if (stack.getItem() instanceof RoboBeeItem) {
                        yield roboBeeInventory.insertItem(slot, stack, simulate);
                    } else {
                        yield stack;
                    }
                }
            };
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return switch (filterMode) {
                case ALL -> {
                    if (slot < inventory.getSlots()) {
                        yield inventory.extractItem(slot, amount, simulate);
                    } else {
                        yield roboBeeInventory.extractItem(slot - inventory.getSlots(), amount, simulate);
                    }
                }
                case PACKAGES_ONLY -> inventory.extractItem(slot, amount, simulate);
                case ROBO_ONLY -> roboBeeInventory.extractItem(slot, amount, simulate);
            };
        }

        @Override
        public int getSlotLimit(int slot) {
            return switch (filterMode) {
                case ALL -> {
                    if (slot < inventory.getSlots()) {
                        yield inventory.getSlotLimit(slot);
                    } else {
                        yield roboBeeInventory.getSlotLimit(slot - inventory.getSlots());
                    }
                }
                case PACKAGES_ONLY -> inventory.getSlotLimit(slot);
                case ROBO_ONLY -> roboBeeInventory.getSlotLimit(slot);
            };
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (filterMode) {
                case ALL -> {
                    if (stack.getItem() instanceof RoboBeeItem) {
                        yield slot >= inventory.getSlots() && roboBeeInventory.isItemValid(slot - inventory.getSlots(), stack);
                    } else {
                        yield slot < inventory.getSlots() && inventory.isItemValid(slot, stack);
                    }
                }
                case PACKAGES_ONLY -> !(stack.getItem() instanceof RoboBeeItem) && inventory.isItemValid(slot, stack);
                case ROBO_ONLY -> (stack.getItem() instanceof RoboBeeItem) && roboBeeInventory.isItemValid(slot, stack);
            };
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
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CMPBlockEntities.BEE_PORT.get(),
                (be, direction) -> be.handler
        );
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
            RoboManager.get(serverLevel).requestRobo(new BeePortBlockEntityTarget(this), this.getLogisticsNetworkId(), RoboRequest.Mission.RESTOCK);
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("RoboBeeInventory", roboBeeInventory.serializeNBT(registries));
        tag.putBoolean("BeeReturnModeEnabled", beeReturnModeEnabled);
        if (placerUUID != null) {
            tag.putUUID("PlacerUUID", placerUUID);
        }
        tag.putInt("FilterMode", filterMode.getId());
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("RoboBeeInventory")) {
            roboBeeInventory.deserializeNBT(registries, tag.getCompound("RoboBeeInventory"));
        }
        beeReturnModeEnabled = tag.getBoolean("BeeReturnModeEnabled");
        if (tag.contains("PlacerUUID")) {
            placerUUID = tag.getUUID("PlacerUUID");
        }
        if (tag.contains("FilterMode")) {
            filterMode = FilterMode.fromId(tag.getInt("FilterMode"));
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
            this.data.set(2, beeReturnModeEnabled ? 1 : 0);
            this.data.set(3, filterMode.getId());
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

    private @Nullable IItemHandler getAdjacentInventory(Direction side) {
        if (level == null) return null;
        BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(side));
        if (blockEntity == null || blockEntity instanceof FrogportBlockEntity)
            return null;
        return level.getCapability(Capabilities.ItemHandler.BLOCK, blockEntity.getBlockPos(), side.getOpposite());
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

        Set<UUID> playerUUIDs = NetworkHelper.getPlayerUUIDs(getLogisticsNetworkId());
        // Check if the item can be sent to a player.
        for (Player player : level.players()) {
            if (!playerUUIDs.contains(player.getUUID())) {
                continue; // skip players not in the logistics network
            }
            if (CMPHelper.doesAddressMatchPlayer(player, address) && CMPHelper.isWithinRange(level, player.blockPosition(), this.getBlockPos())) {
                sendToPlayer(player, itemStack, slot);
                return;
            }
        }

        // An address starting with '@' always targets a player, so never route it to a drone port.
        if (address.startsWith("@")) {
            PlayerNameCache cache = PlayerNameCache.get((ServerLevel) level);
            String playerName = address.substring(1);
            String status;
            Optional<String> knownPlayerName = cache.matchPlayerNameToAddress(address);
            if (knownPlayerName.isEmpty()) {
                status = "not found";
            } else if (!playerUUIDs.contains(cache.getPlayerUUID(knownPlayerName.get()))) {
                status = "not in network";
            } else if (!cache.isPlayerOnline(cache.getPlayerUUID(knownPlayerName.get()))) {
                status = "offline";
            } else {
                status = "out of range";
            }
            CreateMobilePackages.LOGGER.warn("Cannot send package to player '{}' from port at {}: {}", playerName, getBlockPos(), status);
            return;
        }

        // Check if the item can be sent to another drone port.
        if (CMPConfigs.server().portToPort.get() && !PackageItem.matchAddress(address, addressFilter)) {
            BeePortBlockEntity beePortBlockEntity = CMPHelper.getClosestBeePort(level, address, this.getBlockPos(), null, getLogisticsNetworkId());
            if (beePortBlockEntity != null && beePortBlockEntity.hasSpaceForPackageAndRobo()) {
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
            RoboManager.get(serverLevel).newRobo(serverLevel, itemStack, this.getBlockPos(), this.getLogisticsNetworkId(), 0, this.getBlockPos(), beeReturnModeEnabled);
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
        if (level instanceof ServerLevel serverLevel) {
            DronePortTracker tracker = DronePortTracker.get(serverLevel);
            tracker.add(this);
        }
        // update network data
        if (level != null)
            Create.LOGISTICS.linkAdded(behaviour.freqId, GlobalPos.of(level.dimension(), getBlockPos()), placerUUID);
    }

    /**
     * Unregisters the entity from the tracker and halts any incoming bees.
     */
    private void invalidateTarget() {
        if (level instanceof ServerLevel serverLevel) {
            DronePortTracker tracker = DronePortTracker.get(serverLevel);
            tracker.remove(this);
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
     * Checks if the at least one of the PackageSlots is empty.
     *
     * @return True if at least one slot is empty, false if all slots are full.
     */
    public boolean hasSpaceForPackage() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is space for at least one Robo-Bee in the RoboBeeInventory.
     *
     * @return True if there is space for at least one Robo-Bee, false otherwise.
     */
    public synchronized boolean hasSpaceForRobo() {
        return roboBeeInventory.getStackInSlot(0).getCount() < ROBOBEE_INVENTORY_STACK_SIZE;
    }

    /**
     * Checks if there is space for at least one Package and one Robo-Bee in the respective inventories.
     *
     * @return True if there is space for at least one Package and one Robo-Bee, false otherwise.
     */
    public boolean hasSpaceForPackageAndRobo() {
        return hasSpaceForPackage() && hasSpaceForRobo();
    }

    /**
     * Checks if the drone port can accept a Create Mod package or a Robo-Bee.
     *
     * @param entity     The incoming RoboEntity (can be null).
     * @param hasPackage True if the entity carries a package, false otherwise.
     * @return True if the port can accept the entity, false otherwise.
     */
    public synchronized boolean canAcceptEntity(VirtualRobo entity, Boolean hasPackage) {
        if (this.isRemoved()) return false;
        if (entity == null) return hasPackage ? hasSpaceForPackageAndRobo() : hasSpaceForRobo();
        if (hasRoboRequest()) return false;
        return hasPackage ? hasSpaceForPackageAndRobo() : hasSpaceForRobo();
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

    public void setPlacerUUID(UUID uuid) {
        this.placerUUID = uuid;
        if (level != null)
            Create.LOGISTICS.linkAdded(behaviour.freqId, GlobalPos.of(level.dimension(), getBlockPos()), placerUUID);
    }

    public UUID getLogisticsNetworkId() {
        return behaviour.freqId;
    }

    @Override
    public ItemInteractionResult use(Player player) {
        IExtendedLogisticsNetwork network = NetworkHelper.getExtendedLogisticsNetwork(behaviour.freqId);
        if (network != null
                && !network.create_mobile_packages$getPlayers().contains(player.getUUID())
                && !behaviour.mayInteractMessage(player)
        ) {
            return ItemInteractionResult.SUCCESS;
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

    public void setBeeReturnModeEnabled(boolean beeReturnModeEnabled) {
        if (this.beeReturnModeEnabled == beeReturnModeEnabled) return;
        this.beeReturnModeEnabled = beeReturnModeEnabled;
        if (level != null && !level.isClientSide) {
            level.blockEntityChanged(worldPosition);
        }
        setChanged();
    }

    public boolean setBeeReturnMode() {
        return beeReturnModeEnabled;
    }

    public void toggleFilterMode() {
        filterMode = FilterMode.fromId((filterMode.getId() + 1) % FilterMode.values().length);
        if (level != null && !level.isClientSide) {
            level.blockEntityChanged(worldPosition);
        }
        setChanged();
    }
}
