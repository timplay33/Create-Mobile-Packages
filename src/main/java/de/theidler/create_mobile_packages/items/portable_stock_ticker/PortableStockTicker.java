package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.checkerframework.checker.units.qual.K;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.IntStream;

import static com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem.isTuned;

public class PortableStockTicker extends StockCheckingItem {

    public Map<UUID, List<Integer>> hiddenCategoriesByPlayer;
    protected String previouslyUsedAddress;
    protected List<ItemStack> categories;

    public PortableStockTicker(Properties pProperties) {
        super(pProperties.stacksTo(1));
        categories = new ArrayList<>();
        hiddenCategoriesByPlayer = new HashMap<>();
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.UNCOMMON;
    }

    public boolean broadcastPackageRequest(LogisticallyLinkedBehaviour.RequestType type, PackageOrderWithCrafts order, IdentifiedInventory ignoredHandler,
                                           String address, Player player) {
        boolean result = super.broadcastPackageRequest(type, order, ignoredHandler, address);
        previouslyUsedAddress = address;

        if (player instanceof ServerPlayer) {
            for (ItemStack itemStack : player.getInventory().items) {
                if (itemStack.getItem() instanceof PortableStockTicker) {
                    saveAddressToStack(itemStack, address);
                }
            }
        }
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        Player player = pContext.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;

        if (level.getBlockEntity(pos) instanceof StockTickerBlockEntity stbe) {
            CompoundTag tag = new CompoundTag();
            stbe.saveAdditional(tag);
            categories = NBTHelper.readItemList(tag.getList("Categories", Tag.TAG_COMPOUND));
            saveCategoriesToStack(stack, categories);
            saveHiddenCategoriesByPlayerToStack(stack , hiddenCategoriesByPlayer);
        }
        return super.useOn(pContext);

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide) {
            ItemStack stack = pPlayer.getItemInHand(pUsedHand);
            previouslyUsedAddress = loadAddressFromStack(stack);
            categories = loadCategoriesFromStack(stack);
            hiddenCategoriesByPlayer = getHiddenCategoriesByPlayerFromStack(stack);
            InventorySummary summary = getAccurateSummary(stack);

            List<BigItemStack> bigItemStacks = summary.getStacks();

            if(!isTuned(stack)) {
                pPlayer.displayClientMessage(Component.translatable("item.create_mobile_packages.portable_stock_ticker.not_linked"), true);
                return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
            }
            MenuProvider provider = new SimpleMenuProvider(
                    (id, inv, p) -> new PortableStockTickerMenu(id, inv, this),
                    Component.translatable("item.create_mobile_packages.portable_stock_ticker")
            );
            NetworkHooks.openScreen((ServerPlayer) pPlayer, provider);
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (id, inv, ply) -> new PortableStockTickerMenu(id, inv, this),
                        Component.translatable("item.create_mobile_packages.portable_stock_ticker")
                ), buf -> {});
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        if (Screen.hasShiftDown()) {
            pTooltip.add(Component.literal(""));
            pTooltip.add(Component.translatable("item.create_mobile_packages.portable_stock_ticker.tooltip.summary").withStyle(ChatFormatting.YELLOW));
            pTooltip.add(Component.literal(""));
            pTooltip.add(Component.translatable("item.create_mobile_packages.portable_stock_ticker.tooltip.condition1").withStyle(ChatFormatting.GRAY));
            pTooltip.add(Component.translatable("item.create_mobile_packages.portable_stock_ticker.tooltip.behaviour1").withStyle(ChatFormatting.YELLOW));
            pTooltip.add(Component.translatable("item.create_mobile_packages.portable_stock_ticker.tooltip.condition2").withStyle(ChatFormatting.GRAY));
            pTooltip.add(Component.translatable("item.create_mobile_packages.portable_stock_ticker.tooltip.behaviour2").withStyle(ChatFormatting.YELLOW));
        } else {
            pTooltip.add(Component.translatable("create.tooltip.holdForDescription", Component.translatable("create.tooltip.keyShift").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
        }
    }
    private static final String ADDRESS_TAG = "PreviousAddress";

    public void saveAddressToStack(ItemStack stack, String address) {
        if (address != null && !address.isEmpty()) {
            stack.getOrCreateTag().putString(ADDRESS_TAG, address);
        }
    }

    public String loadAddressFromStack(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(ADDRESS_TAG)) {
            return stack.getTag().getString(ADDRESS_TAG);
        }
        return null;
    }

    public void saveCategoriesToStack(ItemStack stack, List<ItemStack> categories) {
        if (categories != null && !categories.isEmpty()) {
            stack.getOrCreateTag().put("Categories", NBTHelper.writeItemList(categories));
        }
    }

    public List<ItemStack> loadCategoriesFromStack(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Categories")) {
            List<ItemStack> readCategories = NBTHelper.readItemList(stack.getTag().getList("Categories", Tag.TAG_COMPOUND));
            readCategories.removeIf(itemStack -> !itemStack.isEmpty() && !(itemStack.getItem() instanceof FilterItem));
            return readCategories;
        }
        return new ArrayList<>();
    }

    public void saveHiddenCategoriesByPlayerToStack(ItemStack stack, Map<UUID, List<Integer>> hiddenCategoriesByPlayer) {
        if (hiddenCategoriesByPlayer != null && !hiddenCategoriesByPlayer.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.put("HiddenCategories", NBTHelper.writeCompoundList(hiddenCategoriesByPlayer.entrySet(), e -> {
                CompoundTag c = new CompoundTag();
                c.putUUID("Id", e.getKey());
                c.putIntArray("Indices", e.getValue());
                return c;
            }));
            stack.getOrCreateTag().put("HiddenCategories", tag);
        }
    }

    public Map<UUID, List<Integer>> getHiddenCategoriesByPlayerFromStack(ItemStack stack) {
        Map<UUID, List<Integer>> hiddenCategoriesByPlayer = new HashMap<>();
        if (stack.hasTag() && stack.getTag().contains("HiddenCategories")) {
            CompoundTag tag = stack.getTag().getCompound("HiddenCategories");
            NBTHelper.iterateCompoundList(tag.getList("HiddenCategories", Tag.TAG_COMPOUND),
                    c -> hiddenCategoriesByPlayer.put(c.getUUID("Id"), IntStream.of(c.getIntArray("Indices"))
                            .boxed()
                            .toList()));
        }
        return hiddenCategoriesByPlayer;
    }
}
