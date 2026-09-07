package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import de.theidler.create_mobile_packages.compat.fluidlogistics.CFLBridge;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_abstractions.generic.support.GenericLogisticsManager;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

import javax.annotation.Nullable;
import java.util.UUID;

public class StockCheckingItem extends LogisticallyLinkedItem {

    public StockCheckingItem(Properties pProperties) {
        super(pProperties);
    }

    // Retrieve an accurate summary of the network
    public static GenericInventorySummary getAccurateSummary(ItemStack stack) {
        UUID Freq = networkFromStack(stack);
        if (Freq == null) {
            return GenericInventorySummary.empty();
        }
        return GenericInventorySummary.of(LogisticsManager.getSummaryOfNetwork(Freq, true));
    }

    // Send a package request
    public boolean broadcastPackageRequest(ItemStack stack, LogisticallyLinkedBehaviour.RequestType type, GenericOrder order,
                                           @Nullable IdentifiedInventory ignoredHandler,
                                           String address) {
        UUID Freq = networkFromStack(stack);
        if (CFLBridge.containsPackageResource(order)) {
            return LogisticsManager.broadcastPackageRequest(
                    Freq, type, order.asCrafting(), ignoredHandler, address);
        }
        return GenericLogisticsManager.broadcastPackageRequest(Freq, type, order, ignoredHandler, address);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isTuned(stack)) {
            player.displayClientMessage(
                    Component.translatable("item.create_mobile_packages.portable_stock_ticker.not_linked"), true);
            return super.use(level, player, hand);
        }
        return super.use(level, player, hand);
    }
}
