package de.theidler.create_mobile_packages.blocks;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
                    sendPackageToPlayerWithDelay(player, itemStack);
                    inventory.setStackInSlot(slot, ItemStack.EMPTY);
                    break;
                }
            }
        }
    }

    private void sendPackageToPlayerWithDelay(Player player, ItemStack itemStack) {
        int delay = CMPConfigs.server().dronePortDeliveryDelay.get();
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
