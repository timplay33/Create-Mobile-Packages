package de.theidler.create_mobile_packages.blocks;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import de.theidler.create_mobile_packages.entities.DroneEntity;
import de.theidler.create_mobile_packages.index.CMPEntities;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DronePortBlockEntity extends SmartBlockEntity implements MenuProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ItemStackHandler inventory = new ItemStackHandler(CMPConfigs.server().dronePortMaxSize.get());
    private final LazyOptional<IItemHandler> inventoryCapability = LazyOptional.of(() -> inventory);

    public DronePortBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void tick() {
        super.tick();
        senderTick();
    }

    private int tickCounter = 0;

    private void senderTick() {
        if (tickCounter++ < 20) {
            return;
        }
        if (level.isClientSide) {
            return;
        }

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                sendItemFromQueueIfPossible(itemStack, i);
            }
        }

        tickCounter = 0;
    }

    private void sendItemFromQueueIfPossible(ItemStack itemStack, int slot) {
        if (itemStack != null) {
            if (!PackageItem.isPackage(itemStack)) {
                return;
            }

            for (Player player : level.players()) {
                if (player.getName().getString().equals(PackageItem.getAddress(itemStack))) {
                    LOGGER.info("Sending package to player: {}", player.getName().getString());
                    /*Vec3 spawnPos = findSpawnPositionNearPlayer(player);
                    DroneEntity drone = new DroneEntity(CMPEntities.DRONE_ENTITY.get(), level);
                    drone.setTargetPlayerUUID(player.getUUID());
                    drone.setPos(spawnPos);
                    level.addFreshEntity(drone);*/
                    spawnAndMoveDrone(player);
                    sendPackageToPlayerWithDelay(player, itemStack);
                    inventory.setStackInSlot(slot, ItemStack.EMPTY);
                    break;
                }
            }
        }
    }
    private Vec3 findSpawnPositionFromPort(Player player) {
        Level level = player.level();
        Vec3 portPos = this.getBlockPos().getCenter();  // Get the port's position
        Vec3 playerPos = player.position();

        // Calculate direction from the port to the player
        Vec3 directionToPlayer = playerPos.subtract(portPos).normalize();

        // Adjust spawn position: we want the drone to spawn slightly behind the player
        double offsetDistance = 2; // You can adjust this distance
        Vec3 spawnPos = portPos.add(directionToPlayer.scale(offsetDistance));
        // Height adjustment for better visibility

        // Check if the spawn position is valid (not colliding with blocks)
        ClipContext context = new ClipContext(portPos.add(0, 1, 0), spawnPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
        if (level.clip(context).getType() == HitResult.Type.MISS) {
            return spawnPos;
        } else {
            // Fallback if the position is not valid (defaulting to above the port)
            return portPos.add(0, 6, 0);
        }
    }


    public void spawnAndMoveDrone(Player player) {
        // Get the spawn position based on the port
        Vec3 spawnPos = findSpawnPositionFromPort(player);

        // Calculate the delay (time it would take to reach the player)
        int delay = calcTimeDelay(spawnPos, player.position());

        // Create the drone entity
        DroneEntity drone = new DroneEntity(CMPEntities.DRONE_ENTITY.get(), player.level());
        drone.setPos(spawnPos);
        drone.setTargetPlayerUUID(player.getUUID());

        // Add the drone to the world
        player.level().addFreshEntity(drone);

        // Now move the drone to the player after the calculated delay
        Executors.newScheduledThreadPool(1).schedule(() -> {
            Vec3 targetPos = player.position();
            moveDroneToTarget(drone, targetPos, delay);
        }, delay, TimeUnit.MILLISECONDS);
    }


    private void moveDroneToTarget(DroneEntity drone, Vec3 targetPos, int delay) {
        // Logic to move the drone towards the player smoothly after the delay
        Vec3 currentPos = drone.position();
        double speedPerTick = CMPConfigs.server().droneSpeed.get() / 20.0;

        // Move in a straight line toward the target position
        Vec3 direction = targetPos.subtract(currentPos).normalize();
        Vec3 velocity = direction.scale(speedPerTick);
        drone.setDeltaMovement(velocity);
        drone.hasImpulse = true;
    }

    private void sendPackageToPlayerWithDelay(Player player, ItemStack itemStack) {
        int delay = calcTimeDelay(this.worldPosition.getCenter(), player.blockPosition().getCenter());
        if (delay == 0) {
            sendPackageToPlayer(player, itemStack);
        } else {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            player.displayClientMessage(Component.literal("Package will arrive in " + (delay) + "s"), true);
            for (int i = 0; i < delay; i++) {
                final int countdown = i;
                scheduler.schedule(() -> {
                    player.displayClientMessage(Component.literal("Package will arrive in " + (delay - countdown - 1) + "s"), true);
                    if (countdown == delay - 1) {
                        sendPackageToPlayer(player, itemStack);
                    }
                    scheduler.shutdown();
                }, countdown + 1, TimeUnit.SECONDS);
            }
        }
    }

    private int calcTimeDelay(Vec3 startPos, Vec3 targetPos) {
        double distance = startPos.distanceTo(new Vec3(targetPos.x, startPos.y, targetPos.z));
        double speed = CMPConfigs.server().droneSpeed.get(); // Get speed from config (blocks per second)
        double time = distance / speed; // Time in seconds

        return (int) (time); // Convert to game ticks (20 ticks per second)
    }

    public static BlockPos getBlockPosInFront(Player player) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookDirection = player.getLookAngle();
        Vec3 blockPosition = eyePosition.add(lookDirection);
        return new BlockPos(
                (int) (lookDirection.x > 0 ? Math.ceil(blockPosition.x) : Math.floor(blockPosition.x)),
                (int) (Math.floor(player.position().y) + 2),
                (int) (lookDirection.z > 0 ? Math.ceil(blockPosition.z) : Math.floor(blockPosition.z))
        );
    }

    public static boolean isPlayerInventoryFull(Player player) {
        int containerSize = player.getInventory().getContainerSize(); // Total player inventory slots
        int armorSlotsStart = containerSize - 5; // Last 5 slots: [offhand, boots, leggings, chestplate, helmet]

        // Check all main inventory slots (excluding armor and offhand slots)
        for (int i = 0; i < armorSlotsStart; i++) {
            if (player.getInventory().getItem(i).isEmpty()) {
                return false;
            }
        }
        // Check if armor slots and the offhand are the only empty slots
        for (int i = armorSlotsStart; i < containerSize; i++) {
            if (!player.getInventory().getItem(i).isEmpty()) {
                continue;
            }
            return true;
        }
        return true;
    }

    public void sendPackageToPlayer(Player player, ItemStack itemStack) {
        if (isPlayerInventoryFull(player)) {
            BlockPos blockPos = getBlockPosInFront(player);
            ItemEntity entityItem = new ItemEntity(player.level(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack);
            player.level().addFreshEntity(entityItem);
        } else {
            player.getInventory().add(itemStack);
        }
        player.displayClientMessage(Component.translatableWithFallback("create_mobile_packages.drone_port.send_items", "Send Items to Player"), true);

    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryCapability.invalidate();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.create_mobile_packages.drone_port");
    }


    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return null;
    }
}
