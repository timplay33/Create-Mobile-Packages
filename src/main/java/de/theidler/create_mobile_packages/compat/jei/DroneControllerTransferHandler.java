package de.theidler.create_mobile_packages.compat.jei;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.foundation.utility.CreateLang;
import de.theidler.create_mobile_packages.compat.fluidlogistics.CFLBridge;
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
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.library.transfer.RecipeTransferErrorMissingSlots;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericIngredient;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.compat.jei.IngredientTransfer;
import ru.zznty.create_factory_abstractions.compat.jei.TransferOperation;
import ru.zznty.create_factory_abstractions.compat.jei.TransferOperationsResult;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.CraftableGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DroneControllerTransferHandler implements IUniversalRecipeTransferHandler<PortableStockTickerMenu> {

    private final IJeiHelpers helpers;

    public DroneControllerTransferHandler(IJeiHelpers helpers) {
        this.helpers = helpers;
    }

    @Override
    public @NotNull Class<? extends PortableStockTickerMenu> getContainerClass() {
        return PortableStockTickerMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<PortableStockTickerMenu>> getMenuType() {
        return Optional.of(CMPMenuTypes.PORTABLE_STOCK_TICKER_MENU.get());
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(@NotNull PortableStockTickerMenu container, @NotNull Object object,
                                                         @NotNull IRecipeSlotsView recipeSlots, Player player,
                                                         boolean maxTransfer, boolean doTransfer) {
        Level level = player.level();
        if (!(object instanceof Recipe<?> recipe))
            return null;
        MutableObject<IRecipeTransferError> result = new MutableObject<>();
        if (level.isClientSide())
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> result
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

        if (CFLBridge.isPresent() && hasFluidIngredients(recipeSlots)) {
            if (!CFLBridge.isAvailable()) {
                return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.not_in_stock")
                        .component());
            }
            return transferFluidRecipe(screen, recipe, recipeSlots, player, maxTransfer, doTransfer);
        }

        if (screen.itemsToOrder.size() >= 9)
            return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.slots_full")
                                                          .component());

        GenericInventorySummary summary = screen.stockSnapshot();

        List<GenericStack> availableStacks = summary.get();
        Container outputDummy = new RecipeWrapper(new ItemStackHandler(9));
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

    private boolean hasFluidIngredients(IRecipeSlotsView recipeSlots) {
        for (IRecipeSlotView slotView : recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)) {
            if (slotView.getIngredients(ForgeTypes.FLUID_STACK).anyMatch(fluid -> !fluid.isEmpty())) {
                return true;
            }
        }
        for (IRecipeSlotView slotView : recipeSlots.getSlotViews(RecipeIngredientRole.OUTPUT)) {
            if (slotView.getIngredients(ForgeTypes.FLUID_STACK).anyMatch(fluid -> !fluid.isEmpty())) {
                return true;
            }
        }
        return false;
    }

    private IRecipeTransferError transferFluidRecipe(PortableStockTickerScreen screen, Recipe<?> recipe,
                                                      IRecipeSlotsView recipeSlots, Player player,
                                                      boolean maxTransfer, boolean doTransfer) {
        GenericInventorySummary summary = screen.stockSnapshot();
        List<BigItemStack> requirements = selectRequirements(recipeSlots, summary, screen.itemsToOrder);
        if (requirements.isEmpty()) {
            return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.not_in_stock")
                    .component());
        }

        OutputTarget outputTarget = getOutputTarget(recipeSlots, player, recipe, true);
        if (outputTarget == null) {
            return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.recipe_result_empty")
                    .component());
        }

        if (!canFitNewTypes(screen.itemsToOrder, requirements)) {
            return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.slots_full")
                    .component());
        }

        int requestedSets = maxTransfer
                ? Math.max(1, Mth.ceil(outputTarget.transferLimit() / (float) outputTarget.outputCount()))
                : 1;
        int craftableSets = getCraftableSets(summary, screen.itemsToOrder, requirements);
        int setsToAdd = Math.min(requestedSets, craftableSets);
        if (setsToAdd <= 0) {
            return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.not_in_stock")
                    .component());
        }

        if (!doTransfer)
            return null;

        CraftableBigItemStack cbis = new CraftableBigItemStack(outputTarget.displayStack().copy(), recipe);
        CraftableGenericStack ingredientStack = CraftableGenericStack.of(cbis);
        ingredientStack.setAmount(0);

        if (outputTarget.customRecipeData()) {
            if (!CFLBridge.setCraftingData(cbis, outputTarget.outputCount(), outputTarget.transferLimit(), requirements)) {
                return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.not_in_stock")
                        .component());
            }
        }

        screen.recipesToOrder.add(ingredientStack);
        screen.searchBox.setValue("");
        screen.refreshSearchNextTick = true;
        screen.requestCraftable(ingredientStack, outputTarget.outputCount() * setsToAdd);

        return null;
    }

    private List<BigItemStack> selectRequirements(IRecipeSlotsView recipeSlots, GenericInventorySummary summary,
                                                  List<BigGenericStack> existingOrders) {
        List<BigItemStack> selectedRequirements = new ArrayList<>();

        for (IRecipeSlotView slotView : recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)) {
            List<BigItemStack> candidates = getCandidates(slotView);
            if (candidates.isEmpty()) {
                continue;
            }

            BigItemStack chosen = chooseBestCandidate(candidates, summary, selectedRequirements, existingOrders);
            if (chosen == null) {
                return List.of();
            }

            mergeRequirement(selectedRequirements, chosen);
        }

        return selectedRequirements;
    }

    private List<BigItemStack> getCandidates(IRecipeSlotView slotView) {
        List<BigItemStack> candidates = new ArrayList<>();

        slotView.getItemStacks().forEach(stack -> {
            if (!stack.isEmpty()) {
                candidates.add(new BigItemStack(stack.copyWithCount(1), Math.max(1, stack.getCount())));
            }
        });

        slotView.getIngredients(ForgeTypes.FLUID_STACK).forEach(fluid -> {
            if (!fluid.isEmpty()) {
                CFLBridge.createFluidKey(fluid).ifPresent(key -> candidates.add(
                        new BigItemStack(key.stack(), Math.max(1, fluid.getAmount()))));
            }
        });

        return candidates;
    }

    private BigItemStack chooseBestCandidate(List<BigItemStack> candidates, GenericInventorySummary summary,
                                             List<BigItemStack> selectedRequirements,
                                             List<BigGenericStack> existingOrders) {
        BigItemStack best = null;
        int bestAvailable = -1;
        boolean bestPrefersExisting = false;

        for (BigItemStack candidate : candidates) {
            int alreadySelected = getMatchingCount(selectedRequirements, candidate.stack);
            int alreadyOrdered = getMatchingCount(existingOrders, candidate.stack);
            int available = summary.getCountOf(GenericStack.wrap(candidate.stack).key())
                    - alreadySelected - alreadyOrdered;
            if (available < candidate.count) {
                continue;
            }

            boolean prefersExisting = hasMatchingStack(selectedRequirements, candidate.stack)
                    || hasMatchingStack(existingOrders, candidate.stack);
            if (best != null && prefersExisting == bestPrefersExisting && available <= bestAvailable) {
                continue;
            }
            if (best != null && !prefersExisting && bestPrefersExisting) {
                continue;
            }

            best = new BigItemStack(candidate.stack.copyWithCount(1), candidate.count);
            bestAvailable = available;
            bestPrefersExisting = prefersExisting;
        }

        return best;
    }

    private OutputTarget getOutputTarget(IRecipeSlotsView recipeSlots, Player player, Recipe<?> recipe,
                                         boolean customRecipeData) {
        for (IRecipeSlotView slotView : recipeSlots.getSlotViews(RecipeIngredientRole.OUTPUT)) {
            Optional<ItemStack> itemOutput = slotView.getItemStacks()
                    .filter(stack -> !stack.isEmpty())
                    .findFirst();
            if (itemOutput.isPresent()) {
                ItemStack stack = itemOutput.get();
                return new OutputTarget(stack.copyWithCount(1), Math.max(1, stack.getCount()),
                        stack.getMaxStackSize(), customRecipeData);
            }

            Optional<FluidStack> fluidOutput = slotView.getIngredients(ForgeTypes.FLUID_STACK)
                    .filter(fluid -> !fluid.isEmpty())
                    .findFirst();
            if (fluidOutput.isPresent()) {
                FluidStack fluid = fluidOutput.get();
                return CFLBridge.createFluidKey(fluid)
                        .map(key -> new OutputTarget(key.stack(), Math.max(1, fluid.getAmount()),
                                key.transferLimit(), true))
                        .orElse(null);
            }
        }

        RegistryAccess registryAccess = player.level().registryAccess();
        ItemStack result = recipe.getResultItem(registryAccess);
        if (result.isEmpty()) {
            return null;
        }

        return new OutputTarget(result.copyWithCount(1), Math.max(1, result.getCount()), result.getMaxStackSize(),
                customRecipeData);
    }

    private int getCraftableSets(GenericInventorySummary summary, List<BigGenericStack> existingOrders,
                                 List<BigItemStack> requirements) {
        int craftableSets = Integer.MAX_VALUE;
        for (BigItemStack requirement : requirements) {
            int orderedCount = getMatchingCount(existingOrders, requirement.stack);
            int available = summary.getCountOf(GenericStack.wrap(requirement.stack).key()) - orderedCount;
            craftableSets = Math.min(craftableSets, available / requirement.count);
        }
        return craftableSets == Integer.MAX_VALUE ? 0 : Math.max(0, craftableSets);
    }

    private boolean canFitNewTypes(List<BigGenericStack> existingOrders, List<BigItemStack> requirements) {
        int totalTypes = existingOrders.size();
        List<ItemStack> newTypes = new ArrayList<>();

        for (BigItemStack requirement : requirements) {
            if (hasMatchingStack(existingOrders, requirement.stack) || hasMatchingStack(newTypes, requirement.stack)) {
                continue;
            }
            newTypes.add(requirement.stack);
            totalTypes++;
            if (totalTypes > 9) {
                return false;
            }
        }

        return true;
    }

    private void mergeRequirement(List<BigItemStack> requirements, BigItemStack candidate) {
        BigItemStack existing = findMatchingOrder(requirements, candidate.stack);
        if (existing == null) {
            requirements.add(new BigItemStack(candidate.stack.copyWithCount(1), candidate.count));
            return;
        }
        existing.count += candidate.count;
    }

    private BigItemStack findMatchingOrder(List<BigItemStack> stacks, ItemStack target) {
        for (BigItemStack entry : stacks) {
            if (ItemStack.isSameItemSameTags(entry.stack, target)) {
                return entry;
            }
        }
        return null;
    }

    private int getMatchingCount(List<?> stacks, ItemStack target) {
        int total = 0;
        for (Object entry : stacks) {
            ItemStack stack = entry instanceof BigGenericStack genericStack ? genericStack.asStack().stack
                    : entry instanceof BigItemStack bigItemStack ? bigItemStack.stack
                    : (ItemStack) entry;
            if (ItemStack.isSameItemSameTags(stack, target)) {
                total += entry instanceof BigGenericStack genericStack ? genericStack.get().amount()
                        : entry instanceof BigItemStack bigItemStack ? bigItemStack.count
                        : stack.getCount();
            }
        }
        return total;
    }

    private boolean hasMatchingStack(List<?> stacks, ItemStack target) {
        for (Object entry : stacks) {
            ItemStack stack = entry instanceof BigGenericStack genericStack ? genericStack.asStack().stack
                    : entry instanceof BigItemStack bigItemStack ? bigItemStack.stack
                    : (ItemStack) entry;
            if (ItemStack.isSameItemSameTags(stack, target)) {
                return true;
            }
        }
        return false;
    }

    private record OutputTarget(ItemStack displayStack, int outputCount, int transferLimit,
                                boolean customRecipeData) {
    }
}
