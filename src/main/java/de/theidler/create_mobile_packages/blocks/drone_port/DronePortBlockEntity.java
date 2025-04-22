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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.io.Console;
import java.util.List;

import static de.theidler.create_mobile_packages.blocks.drone_port.DronePortBlock.IS_OPEN_TEXTURE;

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
        if (level == null || level.isClientSide) {
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
        if (level == null || itemStack == null || !PackageItem.isPackage(itemStack)) { return; }

        for (Player player : level.players()) {
            if (player.getName().getString().equals(PackageItem.getAddress(itemStack))) {
                CreateMobilePackages.LOGGER.info("Sending package to player: {}", player.getName().getString());
                RoboBeeEntity drone = new RoboBeeEntity(CMPEntities.ROBO_BEE_ENTITY.get(), level, this);
                drone.setTargetPlayerUUID(player.getUUID());
                drone.setItemStack(itemStack);
                drone.setPos(this.getBlockPos().getCenter().subtract(0,0.5,0));
                drone.setOrigin(this.getBlockPos().getCenter().subtract(0,0.5,0));
                level.addFreshEntity(drone);
                setOpen(this,true);
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
                break;
            }
        }
        level.getCapability(ModCapabilities.DRONE_PORT_ENTITY_TRACKER_CAP)
                .ifPresent(tracker -> {
                    List<DronePortBlockEntity> allBEs = tracker.getAll();
                    allBEs.forEach(be -> System.out.printf("%s %s\n", be.getBlockPos(), be.addressFilter));
                });

    }

    public static void setOpen(DronePortBlockEntity dronePortBlockEntity,boolean open) {
        if (dronePortBlockEntity == null) {
            return;
        }
        if (dronePortBlockEntity.level == null) {
            return;
        }
        dronePortBlockEntity.level.setBlockAndUpdate(dronePortBlockEntity.getBlockPos(), dronePortBlockEntity.getBlockState().setValue(IS_OPEN_TEXTURE, open));
        dronePortBlockEntity.level.playSound(null, dronePortBlockEntity.getBlockPos(), open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE,
                SoundSource.BLOCKS);

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

    public static void sendPackageToPlayer(Player player, ItemStack itemStack, RoboBeeEntity roboBeeEntity) {
        if (isPlayerInventoryFull(player)) {
            BlockPos blockPos = roboBeeEntity.blockPosition();
            ItemEntity entityItem = new ItemEntity(player.level(), blockPos.getX(), player.getY(), blockPos.getZ(), itemStack);
            player.level().addFreshEntity(entityItem);
        } else {
            player.getInventory().add(itemStack);
        }
        player.displayClientMessage(Component.translatableWithFallback("create_mobile_packages.drone_port.send_items", "Send Items to Player"), true);

    }

    @Override
    protected void onOpenChange(boolean open) {
        if (level == null) { return; }
        level.playSound(null, worldPosition, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE,
                SoundSource.BLOCKS);
        setOpen(this, open);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide){
            level.getCapability(ModCapabilities.DRONE_PORT_ENTITY_TRACKER_CAP)
                    .ifPresent(tracker -> tracker.add(this));
        }
    }

    @Override
    public void remove() {
        if (!level.isClientSide) {
            level.getCapability(ModCapabilities.DRONE_PORT_ENTITY_TRACKER_CAP)
                    .ifPresent(tracker -> tracker.remove(this));
        }
        super.remove();
    }
}
