package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import de.theidler.create_mobile_packages.compat.FactoryLogisticsCompat;
import de.theidler.create_mobile_packages.compat.Mods;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_abstractions.generic.support.GenericLogisticsManager;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

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

    public static GenericInventorySummary getRecentSummary(ItemStack stack) {
        Freq = networkFromStack(stack);
        return GenericInventorySummary.of(LogisticsManager.getSummaryOfNetwork(Freq, false));
    }

    public static GenericInventorySummary getAccurateSummary(ItemStack stack) {
        Freq = networkFromStack(stack);
        if (Freq == null) {
            return GenericInventorySummary.empty();
        }
        return GenericInventorySummary.of(LogisticsManager.getSummaryOfNetwork(Freq, true));
    }

    public static boolean broadcastPackageRequest(ItemStack stack, LogisticallyLinkedBehaviour.RequestType type, PackageOrderWithCrafts order,
                                                  @Nullable IdentifiedInventory ignoredHandler, String address) {
        Freq = networkFromStack(stack);
        return LogisticsManager.broadcastPackageRequest(Freq, type, order, ignoredHandler, address);
    }

    public boolean broadcastPackageRequest(LogisticallyLinkedBehaviour.RequestType type, GenericOrder order,
                                           @Nullable IdentifiedInventory ignoredHandler,
                                           String address) {
        return GenericLogisticsManager.broadcastPackageRequest(Freq, type, order, ignoredHandler, address);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isTuned(stack)) {
            player.displayClientMessage(
                    Component.translatable("item.create_mobile_packages.portable_stock_ticker.not_linked"), true);
            return super.use(level, player, hand);
        }
        return super.use(level, player, hand);
    }

    public UUID getFrequency() {
        return Freq;
    }
}
