package de.theidler.create_mobile_packages.items;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem.*;

public class StockCheckingItem extends Item {
    public StockCheckingItem(Properties pProperties) {
        super(pProperties);
    }

    // Retrieve the recent summary of the network
    public static InventorySummary getRecentSummary(ItemStack stack) {
        UUID Freq = networkFromStack(stack);
        return LogisticsManager.getSummaryOfNetwork(Freq, false);
    }

    // Retrieve an accurate summary of the network
    public static InventorySummary getAccurateSummary(ItemStack stack) {
        UUID Freq = networkFromStack(stack);
        return LogisticsManager.getSummaryOfNetwork(Freq, true);
    }

    // Send a package request
    public static boolean broadcastPackageRequest(ItemStack stack, RequestType type, PackageOrder order, @Nullable IdentifiedInventory ignoredHandler, String address) {
        return broadcastPackageRequest(stack, type, order, ignoredHandler, address, null);
    }

    public static boolean broadcastPackageRequest(ItemStack stack, RequestType type, PackageOrder order, @Nullable IdentifiedInventory ignoredHandler, String address, @Nullable PackageOrder orderContext) {
        UUID Freq = networkFromStack(stack);
        return LogisticsManager.broadcastPackageRequest(Freq, type, order, ignoredHandler, address, orderContext);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            InventorySummary summary = getAccurateSummary(stack);
            player.displayClientMessage(Component.literal("Stock Info: " + summary.toString()), true);
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        //from com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        Player player = pContext.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;
        /*
        if (player.isShiftKeyDown())
            return super.useOn(pContext);
        */

        LogisticallyLinkedBehaviour link = BlockEntityBehaviour.get(level, pos, LogisticallyLinkedBehaviour.TYPE);
        boolean tuned = isTuned(stack);

        if (link != null) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            if (!link.mayInteractMessage(player))
                return InteractionResult.SUCCESS;

            assignFrequency(stack, player, link.freqId);
            return InteractionResult.SUCCESS;
        }

        InteractionResult useOn = super.useOn(pContext);
        if (level.isClientSide || useOn == InteractionResult.FAIL)
            return useOn;

        player.displayClientMessage(tuned ? CreateLang.translateDirect("logistically_linked.connected")
                : CreateLang.translateDirect("logistically_linked.new_network_started"), true);
        return useOn;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        UUID Freq = networkFromStack(pStack);
        pTooltip.add(Component.literal("Logistics Network ID: " + Freq));

        if (!isTuned(pStack))
            return;

        CompoundTag tag = pStack.getTag()
                .getCompound(BLOCK_ENTITY_TAG);
        if (!tag.hasUUID("Freq"))
            return;

        CreateLang.translate("logistically_linked.tooltip")
                .style(ChatFormatting.GOLD)
                .addTo(pTooltip);

        CreateLang.translate("logistically_linked.tooltip_clear")
                .style(ChatFormatting.GRAY)
                .addTo(pTooltip);
    }

}
