package de.theidler.create_mobile_packages.items;

import com.simibubi.create.content.logistics.BigItemStack;
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
import java.util.*;

import static com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem.*;

public class StockCheckingItem extends Item {
    protected static UUID Freq;
    protected List<List<BigItemStack>> lastClientsideStockSnapshot;
    protected InventorySummary lastClientsideStockSnapshotAsSummary;
    protected List<BigItemStack> newlyReceivedStockSnapshot;
    protected String previouslyUsedAddress;
    protected int activeLinks;
    protected int ticksSinceLastUpdate;
    protected List<ItemStack> categories;
    protected Map<UUID, List<Integer>> hiddenCategoriesByPlayer;
    @Nullable
    protected Level level;

    public StockCheckingItem(Properties pProperties) {
        super(pProperties);
        categories = new ArrayList<>();
    }

    // Retrieve the recent summary of the network
    public static InventorySummary getRecentSummary(ItemStack stack) {
        Freq = networkFromStack(stack);
        return LogisticsManager.getSummaryOfNetwork(Freq, false);
    }

    // Retrieve an accurate summary of the network
    public static InventorySummary getAccurateSummary(ItemStack stack) {
        Freq = networkFromStack(stack);
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
        if(!isTuned(stack)) {
            player.displayClientMessage(Component.literal("Not linked to Network"), true);
            return super.use(level, player, hand);
        }
// dev
        if (!level.isClientSide) {
            InventorySummary summary = getAccurateSummary(stack);
            if (!summary.getStacks().isEmpty()) {
            summary.getStacks().forEach(bigItemStack -> {player.sendSystemMessage(Component.literal(bigItemStack.toString()));});
            }
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

        LogisticallyLinkedBehaviour link = BlockEntityBehaviour.get(level, pos, LogisticallyLinkedBehaviour.TYPE);

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
        return useOn;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
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

    public InventorySummary getLastClientsideStockSnapshotAsSummary() {
        return LogisticsManager.getSummaryOfNetwork(Freq, true);
    }

    public List<List<BigItemStack>> getClientStockSnapshot() {
        return LogisticsManager.getSummaryOfNetwork(Freq, true).getStacks().stream().map(Arrays::asList).toList();
    }

    public @Nullable Level getLevel() {
        return this.level;
    }

    public void setLevel(Level pLevel) {
        this.level = pLevel;
    }
    public boolean hasLevel() {
        return this.level != null;
    }
}
