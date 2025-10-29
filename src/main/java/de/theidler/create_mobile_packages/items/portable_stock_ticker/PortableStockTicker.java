package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import de.theidler.create_mobile_packages.compat.Mods;
import de.theidler.create_mobile_packages.compat.curios.Curios;
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
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

import java.util.*;
import java.util.stream.IntStream;

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

        // Check Curios if installed
        if (Mods.CURIOS.isLoaded()) {
            pst = Curios.findPortableStockTickerCurios(playerInventory.player);
            if (pst.getItem() instanceof PortableStockTicker) {
                return pst;
            }
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
    public @NotNull Rarity getRarity(@NotNull ItemStack pStack) {
        return Rarity.UNCOMMON;
    }

    public boolean broadcastPackageRequest(ItemStack stack, LogisticallyLinkedBehaviour.RequestType type, GenericOrder order,
                                           IdentifiedInventory ignoredHandler,
                                           String address, Player player) {
        boolean result = super.broadcastPackageRequest(stack, type, order, ignoredHandler, address);
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
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        Player player = pContext.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;

        if (!level.isClientSide() && player.isShiftKeyDown()) {
            if (level.getBlockEntity(pos) instanceof StockTickerBlockEntity stbe) {
                // Copy categories from StockTickerBlockEntity
                CompoundTag tag = new CompoundTag();
                stbe.saveAdditional(tag);
                categories = NBTHelper.readItemList(tag.getList("Categories", Tag.TAG_COMPOUND));
            } else if (level.getBlockEntity(pos) instanceof PackagerLinkBlockEntity) {
                // Clear categories from an old link
                categories = new ArrayList<>();
            }
            saveCategoriesToStack(stack, categories);
            saveHiddenCategoriesByPlayerToStack(stack, hiddenCategoriesByPlayer);
            return super.useOn(pContext);
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        previouslyUsedAddress = loadAddressFromStack(stack);
        categories = loadCategoriesFromStack(stack);
        hiddenCategoriesByPlayer = getHiddenCategoriesByPlayerFromStack(stack);
        if (!pLevel.isClientSide && !pPlayer.isShiftKeyDown()) {
            if (!isTuned(stack)) {
                pPlayer.displayClientMessage(
                        Component.translatable("item.create_mobile_packages.portable_stock_ticker.not_linked"), true);
                return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
            }
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                MenuProvider provider = new SimpleMenuProvider((id, inv, p) -> new PortableStockTickerMenu(id, inv),
                        Component.translatable("item.create_mobile_packages.portable_stock_ticker"));
                NetworkHooks.openScreen(serverPlayer, provider, buf -> {
                });
            }
            return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
        }
        return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
    }

    private static final String ADDRESS_TAG = "PreviousAddress";

    public void saveAddressToStack(ItemStack stack, String address) {
        if (address != null && !address.isEmpty()) {
            stack.getOrCreateTag().putString(ADDRESS_TAG, address);
        }
    }

    public String loadAddressFromStack(ItemStack stack) {
        if (stack.getTag() != null && stack.hasTag() && stack.getTag().contains(ADDRESS_TAG)) {
            return stack.getTag().getString(ADDRESS_TAG);
        }
        return null;
    }

    public void saveCategoriesToStack(ItemStack stack, List<ItemStack> categories) {
        if (categories != null) {
            stack.getOrCreateTag().put("Categories", NBTHelper.writeItemList(categories));
        }
    }

    public List<ItemStack> loadCategoriesFromStack(ItemStack stack) {
        if (stack.getTag() != null && stack.hasTag() && stack.getTag().contains("Categories")) {
            List<ItemStack> readCategories = NBTHelper.readItemList(
                    stack.getTag().getList("Categories", Tag.TAG_COMPOUND));
            readCategories.removeIf(itemStack -> !itemStack.isEmpty() && !(itemStack.getItem() instanceof FilterItem));
            return readCategories;
        }
        return new ArrayList<>();
    }

    public void saveHiddenCategoriesByPlayerToStack(ItemStack stack,
                                                    Map<UUID, List<Integer>> hiddenCategoriesByPlayer) {
        if (hiddenCategoriesByPlayer != null) {
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
        if (stack.getTag() != null && stack.hasTag() && stack.getTag().contains("HiddenCategories")) {
            CompoundTag tag = stack.getTag().getCompound("HiddenCategories");
            NBTHelper.iterateCompoundList(tag.getList("HiddenCategories", Tag.TAG_COMPOUND),
                    c -> hiddenCategoriesByPlayer.put(c.getUUID("Id"),
                            IntStream.of(c.getIntArray("Indices"))
                                    .boxed()
                                    .toList()));
        }
        return hiddenCategoriesByPlayer;
    }
}
