package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import de.theidler.create_mobile_packages.compat.FactoryLogisticsCompat;
import de.theidler.create_mobile_packages.compat.Mods;
import de.theidler.create_mobile_packages.items.drone_controller.LogisticallyLinkedItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class StockCheckingItem extends LogisticallyLinkedItem {
    protected static UUID Freq;

    @Override
    public boolean isFoil(ItemStack pStack) {
        return isTuned(pStack);
    }

    public StockCheckingItem(Properties pProperties) {
        super(pProperties);
    }

    public static InventorySummary getRecentSummary(ItemStack stack) {
        Freq = networkFromStack(stack);
        return LogisticsManager.getSummaryOfNetwork(Freq, false);
    }

    public static InventorySummary getAccurateSummary(ItemStack stack) {
        Freq = networkFromStack(stack);
        if (Freq == null) {
            return new InventorySummary();
        }
        return LogisticsManager.getSummaryOfNetwork(Freq, true);
    }

    public static boolean broadcastPackageRequest(ItemStack stack, LogisticallyLinkedBehaviour.RequestType type, PackageOrderWithCrafts order, @Nullable IdentifiedInventory ignoredHandler, String address) {
        Freq = networkFromStack(stack);
        return LogisticsManager.broadcastPackageRequest(Freq, type, order, ignoredHandler, address);
    }

    public boolean broadcastPackageRequest(LogisticallyLinkedBehaviour.RequestType type, PackageOrderWithCrafts order, @Nullable IdentifiedInventory ignoredHandler,
                                           String address) {
        if (Mods.CREATE_FACTORY_LOGISTICS.isLoaded()) {
            return FactoryLogisticsCompat.tryBroadcast(Freq, type, order, ignoredHandler, address);
        } else {
            return LogisticsManager.broadcastPackageRequest(Freq, type, order, ignoredHandler, address);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(!isTuned(stack)) {
            player.displayClientMessage(Component.translatable("item.create_mobile_packages.portable_stock_ticker.not_linked"), true);
            return super.use(level, player, hand);
        }
        return super.use(level, player, hand);
    }

    public UUID getFrequency() {
        return Freq;
    }
}
