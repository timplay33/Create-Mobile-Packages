package de.theidler.create_mobile_packages.compat.jei;

import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.foundation.blockEntity.LegacyRecipeWrapper;
import com.simibubi.create.foundation.utility.CreateLang;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTickerMenu;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTickerScreen;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.library.transfer.RecipeTransferErrorMissingSlots;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import net.minecraft.core.RegistryAccess;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericIngredient;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.compat.jei.IngredientTransfer;
import ru.zznty.create_factory_abstractions.compat.jei.TransferOperation;
import ru.zznty.create_factory_abstractions.compat.jei.TransferOperationsResult;
import ru.zznty.create_factory_abstractions.generic.support.CraftableGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public @Nullable IRecipeTransferError transferRecipe(PortableStockTickerMenu container, Object object,
                                                         IRecipeSlotsView recipeSlots, Player player,
                                                         boolean maxTransfer, boolean doTransfer) {
        Level level = player.level();
        if (!(object instanceof Recipe<?> recipe))
            return null;
        MutableObject<IRecipeTransferError> result = new MutableObject<>();
        if (level.isClientSide())
            //noinspection unchecked
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> result
                    .setValue(transferRecipeOnClient(container, recipe, recipeSlots, player, maxTransfer, doTransfer)));
        return result.getValue();
    }

    private IRecipeTransferError transferRecipeOnClient(PortableStockTickerMenu container, Recipe<?> recipe,
                                                        IRecipeSlotsView recipeSlots, Player player,
                                                        boolean maxTransfer, boolean doTransfer) {
        if (!(container.screenReference instanceof PortableStockTickerScreen screen))
            return null;

        for (CraftableGenericStack cbis : screen.recipesToOrder)
            if (cbis.asStack().recipe == recipe)
                return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.already_ordering_recipe")
                                                              .component());

        if (screen.itemsToOrder.size() >= 9)
            return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.slots_full")
                                                          .component());

        GenericInventorySummary summary = screen.stockSnapshot();

        List<GenericStack> availableStacks = summary.get();
        Container outputDummy = new LegacyRecipeWrapper(new ItemStackHandler(9));
        List<Slot> craftingSlots = new ArrayList<>();
        for (int i = 0; i < outputDummy.getContainerSize(); i++)
            craftingSlots.add(new Slot(outputDummy, i, 0, 0));

        TransferOperationsResult transferOperations = IngredientTransfer.getRecipeTransferOperations(
                helpers.getIngredientManager(),
                availableStacks, recipeSlots.getSlotViews(RecipeIngredientRole.INPUT), craftingSlots);

        if (!transferOperations.missingItems().isEmpty())
            return new RecipeTransferErrorMissingSlots(CreateLang.translate("gui.stock_keeper.not_in_stock")
                                                               .component(), transferOperations.missingItems());

        if (screen.itemsToOrder.size() + transferOperations.results().stream().mapToInt(
                TransferOperation::from).distinct().count() >= 9)
            return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.slots_full")
                                                          .component());

        if (!doTransfer)
            return null;

        RegistryAccess registryAccess = player.level().registryAccess();
        CraftableBigItemStack cbis = new CraftableBigItemStack(recipe.getResultItem(registryAccess), recipe);
        CraftableGenericStack ingredientStack = CraftableGenericStack.of(cbis);

        ingredientStack.setAmount(0);

        if (CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE) {
            for (TransferOperation operation : transferOperations.results()) {
                IIngredientHelper helper = helpers.getIngredientManager().getIngredientHelper(
                        operation.selectedIngredient().getType());
                ingredientStack.ingredients().add(GenericIngredient.of(availableStacks.get(operation.from()).withAmount(
                        (int) helper.getAmount(operation.selectedIngredient().getIngredient()))));
            }

            for (IRecipeSlotView slotView : recipeSlots.getSlotViews(RecipeIngredientRole.OUTPUT)) {
                Optional<ITypedIngredient<?>> displayedIngredient = slotView.getDisplayedIngredient();
                if (displayedIngredient.isEmpty()) continue;
                Optional<GenericStack> ingredient = IngredientTransfer.tryConvert(helpers.getIngredientManager(),
                                                                                  displayedIngredient.get());
                if (ingredient.isEmpty()) continue;

                ingredientStack.results(registryAccess).add(ingredient.get());
            }

            if (cbis.stack.isEmpty() && !ingredientStack.results(registryAccess).isEmpty()) {
                ingredientStack.set(ingredientStack.results(registryAccess).get(0).withAmount(0));
            }
        }

        screen.recipesToOrder.add(ingredientStack);
        screen.searchBox.setValue("");
        screen.refreshSearchNextTick = true;
        screen.requestCraftable(ingredientStack,
                                maxTransfer && !cbis.stack.isEmpty() ? cbis.stack.getMaxStackSize() : 1);

        return null;
    }
}
