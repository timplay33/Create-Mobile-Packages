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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
    private final ItemStackHandler inventory = new ItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot) {
            if (level != null && !level.isClientSide) {
                if (!getStackInSlot(slot).isEmpty()) {
                    if (PackageItem.isPackage(getStackInSlot(slot))) {
                        LOGGER.info("Item inserted: {} x{} -> {}", getStackInSlot(slot).getItem(), getStackInSlot(slot).getCount(), PackageItem.getAddress(getStackInSlot(slot)));
                        level.players().forEach(player -> {
                            if (player.getDisplayName().getString().equals(PackageItem.getAddress(getStackInSlot(slot)))) {
                                LOGGER.info("waiting {}s", CMPConfigs.server().dronePortDeliveryDelay.get());
                                int delay = CMPConfigs.server().dronePortDeliveryDelay.get();

                                if (delay == 0) {
                                    // Direkt ausführen, wenn keine Verzögerung eingestellt ist
                                    player.drop(getStackInSlot(slot), false);
                                    player.displayClientMessage(Component.translatableWithFallback("create_mobile_packages.drone_port.send_items", "Send Items to Player"), true);
                                    setStackInSlot(slot, ItemStack.EMPTY);

                                } else {
                                    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                                    player.displayClientMessage(Component.literal("Package will arrive in " + (delay) + "s"), true);
                                    for (int i = 0; i < delay; i++) {
                                    final int countdown = i;
                                    scheduler.schedule(() -> {
                                        player.displayClientMessage(Component.literal("Package will arrive in " + (delay - countdown-1) + "s"), true);
                                        if (countdown == delay - 1) {
                                            player.drop(getStackInSlot(slot), false);
                                            player.displayClientMessage(Component.translatableWithFallback("create_mobile_packages.drone_port.send_items", "Send Items to Player"), true);
                                            setStackInSlot(slot, ItemStack.EMPTY);
                                        }
                                        scheduler.shutdown();
                                        }, countdown + 1, TimeUnit.SECONDS);
                                    }
                                }
                            }
                        });
                    } else {
                        LOGGER.info("Item inserted: {} x{} ->x no Package", getStackInSlot(slot).getItem(), getStackInSlot(slot).getCount());
                    }
                }
            }
        }
    };
    private final LazyOptional<IItemHandler> inventoryCapability = LazyOptional.of(() -> inventory);

    public DronePortBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);

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
