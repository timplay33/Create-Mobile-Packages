package de.theidler.create_mobile_packages.blocks.bee_port;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.BeePortStorage;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import de.theidler.create_mobile_packages.Location;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.states.AdjustRotationToTarget;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

import static de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlock.IS_OPEN_TEXTURE;

/**
 * Represents a Drone Port block entity that handles the processing and sending of Create Mod packages
 * to players or other drone ports using drones.
 */
public class BeePortBlockEntity extends PackagePortBlockEntity {

    private int tickCounter = 0; // Counter to track ticks for periodic processing.
    private int sendItemThisTime = 0; // Flag to indicate if an item was sent this time.
    //    private RoboEntity entityOnTravel = null;
    private final Queue<RoboEntity> entityLandingQueue = new ArrayDeque<>();
    private final Queue<RoboEntity> entityLaunchingQueue = new ArrayDeque<>();
    private final ContainerData data = new SimpleContainerData(2);
    private final ItemStackHandler roboBeeInventory = new ItemStackHandler(1);

    /**
     * Constructor for the BeePortBlockEntity.
     *
     * @param pType       The type of the block entity.
     * @param pPos        The position of the block in the world.
     * @param pBlockState The state of the block.
     */
    public BeePortBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        itemHandler = LazyOptional.of(() -> inventory);
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

        // Update Client Data
        if (level != null && !level.isClientSide()) {
            RoboEntity re = getRoboEntity();
            if (re != null) {
                BeePortalBlockEntity exitPortal = re.getExitPortal();
                if (re.multidimensional()) {
                    if (exitPortal != null)
                        this.data.set(0, RoboEntity.calcETA(re, exitPortal.getBlockPos().getCenter()));
                } else
                    this.data.set(0, RoboEntity.calcETA(re.position(), getBlockPos().getCenter()));
            }

            this.data.set(1, this.entityLandingQueue.isEmpty() ? 0 : 1);
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        tryPullingFromAdjacentInventories();
        if (level != null && level.hasNeighborSignal(worldPosition)) {
            tryPushingToAdjacentInventories();
        }
    }

    private void tryPushingToAdjacentInventories() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (!PackageItem.isPackage(itemStack) || !PackageItem.matchAddress(itemStack, addressFilter)) {
                continue;
            }
            for (IItemHandler adjacentInventory : getAdjacentInventories()) {
                if (tryPushingToInventory(adjacentInventory, i)) {
                    return;
                }
            }
        }
    }

    private boolean tryPushingToInventory(IItemHandler inventory, int extractSlot) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                inventory.insertItem(i, this.inventory.extractItem(extractSlot, 1, false), false);
                return true;
            }
        }

        return false;
    }

    private void tryPullingFromAdjacentInventories() {
        if (hasFullInventory(entityLandingQueue.size())) return;

        getAdjacentInventories().forEach((inventory) -> {
            if (inventory == null) return;
            if (hasFullInventory(entityLandingQueue.size())) return;
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
        if (level == null) return null;
        BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(side));
        if (blockEntity == null || blockEntity instanceof FrogportBlockEntity)
            return null;
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side.getOpposite()).resolve().orElse(null);
    }

    /**
     * Processes items in the inventory by attempting to send them to their destination.
     */
    private void processItems() {
        if (level == null || level.isClientSide) return;

        for (int i = 0; i < inventory.getSlots(); i++) {
            if (sendItemThisTime-- > 0) return;
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (!itemStack.isEmpty()) sendItem(itemStack, i);
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
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null)
            return; // TODO: Add support for multiplayer

        // Check if the item can be sent to a player.
        Iterable<ServerLevel> serverLevels = server.getAllLevels();
        for (ServerLevel serverLevel : serverLevels)
            for (Player player : serverLevel.players()) {
                if (player.getName().getString().equals(address)) {
                    if (player.level().dimensionType() != level.dimensionType()) {
                        BeePortalBlockEntity targetPortal = RoboEntity.getClosestBeePortal(level, getBlockPos().getCenter(), player.level());
                        if (targetPortal != null && targetPortal.getLevel() != null) {
                            BeePortalBlockEntity exitPortal = RoboEntity.getExitPortal(player.level(), targetPortal.getBlockPos().getCenter());
                            if (exitPortal != null && RoboEntity.isWithinRange(player.position(), exitPortal.getBlockPos().getCenter()))
                                sendToPlayer(player, itemStack, slot);
                        }
                    } else if (RoboEntity.isWithinRange(player.position(), getBlockPos().getCenter()))
                        sendToPlayer(player, itemStack, slot);
                    return;
                }
            }

        // Check if the item can be sent to a drone port.
        if (CMPConfigs.server().portToPort.get() && !PackageItem.matchAddress(address, addressFilter)) {
            BeePortBlockEntity beePortBlockEntity = RoboEntity.getClosestBeePort(level, address, getBlockPos().getCenter(), null);
            if (beePortBlockEntity == null) {
                List<BeePortBlockEntity> BEs = RoboEntity.getMultidimensionalBeePorts(level, address, getBlockPos().getCenter(), null);
                if (!BEs.isEmpty())
                    beePortBlockEntity = BEs.get(0);
            }

            if (beePortBlockEntity != null && beePortBlockEntity.hasSpace())
                sendDrone(itemStack, slot);
        }
    }

    private static void requestRoboEntity(Location location, ServerLevel serverLevel) {
        BeePortBlockEntity result;
        BeePortStorage storage = BeePortStorage.get(serverLevel);
        Stream<BeePortBlockEntity> allBEs = storage.getAllPorts(serverLevel).stream()
                .filter((be) -> (be.level != location.level()
                        || !be.getBlockPos().equals(location.position()))
                        && be.getRoboBeeInventory().getStackInSlot(0).getCount() > 0
                );

        result = allBEs.min(Comparator.comparingDouble(a -> a.getBlockPos().distSqr(location.position()))).orElse(null);
        if (result != null) result.sendDrone(location);
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
            if (getRoboEntity() == null) {
                if (level instanceof ServerLevel serverLevel)
                    requestRoboEntity(new Location(getBlockPos(), level), serverLevel);
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
        if (roboBeeInventory.getStackInSlot(0).getCount() <= 0) {
            if (level instanceof ServerLevel serverLevel)
                if (entityLandingQueue.isEmpty())
                    requestRoboEntity(new Location(getBlockPos(), level), serverLevel);
            return;
        }

        sendItemThisTime = 2;
        if (level == null) return;
        RoboBeeEntity re = new RoboBeeEntity(level, itemStack, null, this.getBlockPos());
        level.addFreshEntity(re);
        roboBeeInventory.getStackInSlot(0).shrink(1);
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
        tryAddToLaunchingQueue(re);
    }

    private void sendDrone(Location tagetLocation) {
        if (roboBeeInventory.getStackInSlot(0).getCount() <= 0) return;
        sendItemThisTime = 2;
        if (level == null) return;
        RoboBeeEntity drone = new RoboBeeEntity(level, ItemStack.EMPTY, tagetLocation, this.getBlockPos());
        level.addFreshEntity(drone);
        roboBeeInventory.getStackInSlot(0).shrink(1);
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
    public boolean addItemStack(ItemStack itemStack) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
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
        if (player == null || itemStack.isEmpty()) return false;
        player.displayClientMessage(Component.translatableWithFallback("create_mobile_packages.drone_port.send_items", "Send Items to Player"), true);
        if (isPlayerInventoryFull(player)) return false;
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
        if (level == null) return;
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
            BeePortStorage storage = BeePortStorage.get(serverLevel);
            storage.add(this);
        }
    }

    /**
     * Called when the block entity is removed. Unregisters the entity from the tracker.
     */
    @Override
    public void remove() {
        if (level instanceof ServerLevel serverLevel) {
            BeePortStorage storage = BeePortStorage.get(serverLevel);
            storage.remove(this);

            if (!entityLaunchingQueue.isEmpty()) {
                entityLaunchingQueue.forEach(e -> {
                    e.setTargetVelocity(Vec3.ZERO);
                    e.setState(new AdjustRotationToTarget());
                });
            }

            if (roboBeeInventory.getStackInSlot(0).getCount() > 0)
                level.addFreshEntity(new ItemEntity(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), roboBeeInventory.getStackInSlot(0)));
        }

        super.remove();
    }

    public synchronized void tryAddToLandingQueue(@NotNull RoboEntity entity) {
        if (entityLandingQueue.stream().noneMatch(e -> e == entity)) {
            BeePortBlockEntity.setOpen(this, true);
            entityLandingQueue.add(entity);
        }
    }

    public synchronized void tryRemoveFromLandingQueue(@NotNull RoboEntity entity) {
        if (!entityLandingQueue.isEmpty())
            entityLandingQueue.remove(entity);

        if (entityLandingQueue.isEmpty() && entityLaunchingQueue.isEmpty())
            BeePortBlockEntity.setOpen(this, false);
    }

    public synchronized void tryAddToLaunchingQueue(@NotNull RoboEntity entity) {
        if (entityLaunchingQueue.stream().noneMatch(e -> e == entity)) {
            BeePortBlockEntity.setOpen(this, true);
            entityLaunchingQueue.add(entity);
        }
    }

    public synchronized void tryRemoveFromLaunchingQueue(@NotNull RoboEntity entity) {
        if (!entityLaunchingQueue.isEmpty())
            entityLaunchingQueue.remove(entity);

        if (entityLandingQueue.isEmpty() && entityLaunchingQueue.isEmpty())
            BeePortBlockEntity.setOpen(this, false);
    }

    public boolean isLandingPeek(RoboEntity entity) {
        return entityLandingQueue.peek() == entity;
    }

    public boolean isLaunchingPeek(RoboEntity entity) {
        return entityLaunchingQueue.peek() == entity;
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
     * Checks if the drone port has space.
     *
     * @return True if the drone port is not full, false otherwise.
     */
    public boolean hasSpace() {
        return !(hasFullInventory(0) || hasFullRoboSlot(1));
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
        if (entity == null) return hasPackage ? hasSpace() : !hasFullRoboSlot(1);
        if (!entityLandingQueue.contains(entity)) return false;
        return hasPackage ? hasSpace() : !hasFullRoboSlot(1);
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

    public RoboEntity getRoboEntity() {
        return entityLandingQueue.peek();
    }

    public ContainerData getData() {
        return data;
    }
}
