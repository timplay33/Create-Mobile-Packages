package de.theidler.create_mobile_packages.blocks.drone_port;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.DroneEntity;
import de.theidler.create_mobile_packages.index.CMPEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DronePortBlockEntity extends PackagePortBlockEntity {

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
                    CreateMobilePackages.LOGGER.info("Sending package to player: {}", player.getName().getString());
                    DroneEntity drone = new DroneEntity(CMPEntities.DRONE_ENTITY.get(), level);
                    drone.setTargetPlayerUUID(player.getUUID());
                    drone.setItemStack(itemStack);
                    drone.setPos(this.getBlockPos().getCenter());
                    drone.setOrigin(this.getBlockPos().getCenter());
                    level.addFreshEntity(drone);
                    inventory.setStackInSlot(slot, ItemStack.EMPTY);
                    break;
                }
            }
        }
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

    public static void sendPackageToPlayer(Player player, ItemStack itemStack, DroneEntity droneEntity) {
        if (isPlayerInventoryFull(player)) {
            BlockPos blockPos = droneEntity.blockPosition();
            ItemEntity entityItem = new ItemEntity(player.level(), blockPos.getX(), player.getY(), blockPos.getZ(), itemStack);
            player.level().addFreshEntity(entityItem);
        } else {
            player.getInventory().add(itemStack);
        }
        player.displayClientMessage(Component.translatableWithFallback("create_mobile_packages.drone_port.send_items", "Send Items to Player"), true);

    }

    @Override
    protected void onOpenChange(boolean open) {
        level.playSound(null, worldPosition, open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE,
                SoundSource.BLOCKS);
    }
}
