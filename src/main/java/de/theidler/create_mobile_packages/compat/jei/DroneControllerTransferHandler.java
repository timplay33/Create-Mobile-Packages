package de.theidler.create_mobile_packages.compat.jei;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.foundation.utility.CreateLang;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTickerMenu;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTickerScreen;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.common.transfer.RecipeTransferOperationsResult;
import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.library.transfer.RecipeTransferErrorMissingSlots;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DroneControllerTransferHandler implements IUniversalRecipeTransferHandler<PortableStockTickerMenu> {

    private IJeiHelpers helpers;

    public DroneControllerTransferHandler(IJeiHelpers helpers) {
        this.helpers = helpers;
    }

    @Override
    public Class<? extends PortableStockTickerMenu> getContainerClass() {
        return PortableStockTickerMenu.class;
    }

    @Override
    public Optional<MenuType<PortableStockTickerMenu>> getMenuType() {
        return Optional.of(CMPMenuTypes.PORTABLE_STOCK_TICKER_MENU.get());
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(PortableStockTickerMenu container, Object object, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        Level level = player.level();
        if (!(object instanceof Recipe<?> recipe))
            return null;
        MutableObject<IRecipeTransferError> result = new MutableObject<>();
        if (level.isClientSide())
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> result
                    .setValue(transferRecipeOnClient(container, recipe, recipeSlots, player, maxTransfer, doTransfer)));
        return result.getValue();
    }

    private IRecipeTransferError transferRecipeOnClient(PortableStockTickerMenu container, Recipe<?> recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (!(container.screenReference instanceof PortableStockTickerScreen screen))
            return null;

        for (CraftableBigItemStack cbis : screen.recipesToOrder)
            if (cbis.recipe == recipe)
                return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.already_ordering_recipe")
                        .component());

        if (screen.itemsToOrder.size() >= 9)
            return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.slots_full")
                    .component());

        InventorySummary summary = new InventorySummary();
        for (BigItemStack stack: screen.displayedItems) {summary.add(stack);}

        Container outputDummy = new RecipeWrapper(new ItemStackHandler(9));
        List<Slot> craftingSlots = new ArrayList<>();
        for (int i = 0; i < outputDummy.getContainerSize(); i++)
            craftingSlots.add(new Slot(outputDummy, i, 0, 0));

        List<BigItemStack> stacksByCount = summary.getStacksByCount();
        Container inputDummy = new RecipeWrapper(new ItemStackHandler(stacksByCount.size()));
        Map<Slot, ItemStack> availableItemStacks = new HashMap<>();
        for (int j = 0; j < stacksByCount.size(); j++) {
            BigItemStack bigItemStack = stacksByCount.get(j);
            availableItemStacks.put(new Slot(inputDummy, j, 0, 0),
                    bigItemStack.stack.copyWithCount(bigItemStack.count));
        }

        RecipeTransferOperationsResult transferOperations =
                RecipeTransferUtil.getRecipeTransferOperations(helpers.getStackHelper(), availableItemStacks,
                        recipeSlots.getSlotViews(RecipeIngredientRole.INPUT), craftingSlots);

        if (!transferOperations.missingItems.isEmpty())
            return new RecipeTransferErrorMissingSlots(CreateLang.translate("gui.stock_keeper.not_in_stock")
                    .component(), transferOperations.missingItems);

        if (!doTransfer)
            return null;

        CraftableBigItemStack cbis = new CraftableBigItemStack(recipe.getResultItem(player.level()
                .registryAccess()), recipe);

        screen.recipesToOrder.add(cbis);
        screen.searchBox.setValue("");
        screen.refreshSearchNextTick = true;
        screen.requestCraftable(cbis, maxTransfer ? cbis.stack.getMaxStackSize() : 1);

        return null;
    }
}
