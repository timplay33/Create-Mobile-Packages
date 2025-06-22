package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

import java.util.*;

import static de.theidler.create_mobile_packages.index.CMPDataComponents.*;

public class PortableStockTicker extends StockCheckingItem {

    public Map<UUID, List<Integer>> hiddenCategoriesByPlayer;
    protected String previouslyUsedAddress;
    protected List<ItemStack> categories;

    public PortableStockTicker(Properties pProperties) {
        super(pProperties.stacksTo(1));
        categories = new ArrayList<>();
        hiddenCategoriesByPlayer = new HashMap<>();
    }

    public static ItemStack find(Inventory playerInventory) {
        // Check the main hand first
        ItemStack pst = playerInventory.player.getMainHandItem();
        if (playerInventory.player.getMainHandItem().getItem() instanceof PortableStockTicker) {
            return pst;
        }
        // take first PST in inventory
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack portableStockTicker = playerInventory.getItem(i);
            if (playerInventory.getItem(i).getItem() instanceof PortableStockTicker) {
                return portableStockTicker;
            }
        }
        // no PST found
        return null;
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.UNCOMMON;
    }

    public boolean broadcastPackageRequest(LogisticallyLinkedBehaviour.RequestType type, GenericOrder order,
                                           IdentifiedInventory ignoredHandler,
                                           String address, Player player) {
        boolean result = super.broadcastPackageRequest(type, order, ignoredHandler, address);
        previouslyUsedAddress = address;

        if (player instanceof ServerPlayer) {
            ItemStack itemStack = PortableStockTicker.find(player.getInventory());
                if (itemStack != null && itemStack.getItem() instanceof PortableStockTicker) {
                    saveAddressToStack(itemStack, address);
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

        if (!level.isClientSide() && player.isShiftKeyDown()) {
            if (level.getBlockEntity(pos) instanceof StockTickerBlockEntity stbe) {
                CompoundTag tag = new CompoundTag();
                stbe.saveAdditional(tag, level.registryAccess());
                categories = NBTHelper.readItemList(tag.getList("Categories", Tag.TAG_COMPOUND), level.registryAccess());
            } else if (level.getBlockEntity(pos) instanceof PackagerLinkBlockEntity) {
                categories = new ArrayList<>();
            }
            saveCategoriesToStack(stack, categories);
            saveHiddenCategoriesByPlayerToStack(stack, hiddenCategoriesByPlayer);
            return super.useOn(pContext);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        previouslyUsedAddress = loadAddressFromStack(stack);
        categories = loadCategoriesFromStack(stack);
        hiddenCategoriesByPlayer = getHiddenCategoriesByPlayerFromStack(stack);
        if (!pLevel.isClientSide) {
            if (!isTuned(stack)) {
                pPlayer.displayClientMessage(
                        Component.translatable("item.create_mobile_packages.portable_stock_ticker.not_linked"), true);
                return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
            }
            MenuProvider provider = new SimpleMenuProvider(
                    (id, inv, p) -> new PortableStockTickerMenu(id, inv),
                    Component.translatable("item.create_mobile_packages.portable_stock_ticker")
            );
            pPlayer.openMenu(provider);
            return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
        }
        return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
    }

    public void saveAddressToStack(ItemStack stack, String address) {
        if (address != null && !address.isEmpty()) {
            stack.set(ADDRESS_TAG, address);
        }
    }

    public String loadAddressFromStack(ItemStack stack) {
        return stack.getOrDefault(ADDRESS_TAG, null);
    }

    public void saveCategoriesToStack(ItemStack stack, List<ItemStack> categories) {
        if (categories != null) {
            stack.set(CATEGORIES, categories);
        }
    }

    public List<ItemStack> loadCategoriesFromStack(ItemStack stack) {
        List<ItemStack> readCategories = new ArrayList<>(stack.getOrDefault(CATEGORIES, List.of()));
        readCategories.removeIf(itemStack -> !itemStack.isEmpty() && !(itemStack.getItem() instanceof FilterItem));
        return readCategories;
    }

    public void saveHiddenCategoriesByPlayerToStack(ItemStack stack,
                                                    Map<UUID, List<Integer>> hiddenCategoriesByPlayer) {
        if (hiddenCategoriesByPlayer != null) {
            stack.set(HIDDEN_CATEGORIES, hiddenCategoriesByPlayer);
        }
    }

    public Map<UUID, List<Integer>> getHiddenCategoriesByPlayerFromStack(ItemStack stack) {
        return stack.getOrDefault(HIDDEN_CATEGORIES, new HashMap<>());
    }
}

