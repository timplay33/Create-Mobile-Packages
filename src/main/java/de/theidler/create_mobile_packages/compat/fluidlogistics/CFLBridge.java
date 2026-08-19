package de.theidler.create_mobile_packages.compat.fluidlogistics;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.yision.fluidlogistics.api.packager.PackageResourceCrafting;
import com.yision.fluidlogistics.api.packager.PackageResourceCraftingData;
import com.yision.fluidlogistics.api.packager.PackageResourceDisplay;
import com.yision.fluidlogistics.api.packager.PackageResourceTypes;
import com.yision.fluidlogistics.api.packager.PackageResources;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

import java.util.List;
import java.util.Optional;

public class CFLBridge {

    public static boolean isLoaded() {
        return de.theidler.create_mobile_packages.compat.Mods.FLUIDLOGISTICS.isLoaded();
    }

    public static boolean isVirtualFluid(ItemStack stack) {
        return !PackageResourceTypes.getFluid(stack).isEmpty();
    }

    public static boolean isVirtualFluid(GenericStack stack) {
        return isVirtualFluid(keyAsItemStack(stack));
    }

    public static boolean isVirtualFluid(BigGenericStack entry) {
        return isVirtualFluid(entry.get());
    }

    public static GenericStack toVirtualFluidStack(FluidStack fluid, int amountMb) {
        return GenericStack.wrap(PackageResourceTypes.createFluidKey(fluid)).withAmount(amountMb);
    }

    public static FluidStack fluidOf(GenericStack stack) {
        return PackageResourceTypes.getFluid(keyAsItemStack(stack));
    }

    public static FluidStack fluidOf(BigGenericStack entry) {
        return fluidOf(entry.get());
    }

    public static boolean containsVirtualFluid(GenericOrder order) {
        for (GenericStack stack : order.stacks()) {
            if (isVirtualFluid(stack)) return true;
        }
        return false;
    }

    public static int adjustFluidRequestAmount(GenericStack stack, int currentAmount, boolean forward, boolean shift,
                                                boolean control, int minAmount, int maxAmount, int steps,
                                                boolean orderInteraction) {
        PackageResourceDisplay.Interaction interaction = orderInteraction
                ? PackageResourceDisplay.Interaction.STOCK_KEEPER_ORDER
                : PackageResourceDisplay.Interaction.STOCK_KEEPER_INVENTORY;
        return PackageResources.adjustAmount(keyAsItemStack(stack), new PackageResourceDisplay.Adjustment(
                currentAmount, forward, shift, control, minAmount, maxAmount, steps, interaction))
                .orElse(currentAmount);
    }

    public static int getFluidRecipeStepAmount(GenericStack stack, boolean shift, boolean control) {
        return adjustFluidRequestAmount(
                stack, 0, true, shift, control, 0, BigItemStack.INF, 1, false);
    }

    public static int getFluidPerPackage() {
        return PackageResourceTypes.getFluidPerPackage();
    }

    public static boolean hasCustomRecipeData(CraftableBigItemStack cbis) {
        return PackageResourceCrafting.has(cbis);
    }

    public static int getCustomOutputCount(CraftableBigItemStack cbis) {
        return PackageResourceCrafting.get(cbis)
                .map(PackageResourceCraftingData::outputCount)
                .orElse(0);
    }

    public static int getCustomTransferLimit(CraftableBigItemStack cbis) {
        return PackageResourceCrafting.get(cbis)
                .map(PackageResourceCraftingData::transferLimit)
                .orElse(0);
    }

    public static List<BigItemStack> getCustomRequirements(CraftableBigItemStack cbis) {
        return PackageResourceCrafting.get(cbis)
                .map(PackageResourceCraftingData::requirements)
                .orElseGet(List::of);
    }

    public static void setCustomRecipeData(CraftableBigItemStack cbis, int outputCount, int transferLimit,
                                            List<BigItemStack> requirements) {
        PackageResourceCrafting.set(cbis,
                new PackageResourceCraftingData(outputCount, transferLimit, requirements));
    }

    public static ItemStack keyAsItemStack(GenericStack stack) {
        return BigGenericStack.of(stack.withAmount(1)).asStack().stack;
    }

    public static boolean shouldDisplayAsFluidInPackage(ItemStack stack) {
        return getPackageFluidAmount(stack) > 0;
    }

    public static int getPackageFluidAmount(ItemStack stack) {
        FluidStack fluid = PackageResourceTypes.getFluid(stack);
        return fluid.isEmpty() ? 0 : fluid.getAmount();
    }

    public static Optional<List<Component>> getStockKeeperTooltip(BigGenericStack entry, boolean recipe, boolean order,
                                                                   boolean advanced) {
        PackageResourceDisplay.TooltipContext context = recipe
                ? PackageResourceDisplay.TooltipContext.STOCK_KEEPER_CRAFTABLE
                : order
                        ? PackageResourceDisplay.TooltipContext.STOCK_KEEPER_ORDER
                        : PackageResourceDisplay.TooltipContext.STOCK_KEEPER_INVENTORY;
        return PackageResources.tooltipOf(keyAsItemStack(entry.get()), entry.get().amount(), advanced, context);
    }
}
