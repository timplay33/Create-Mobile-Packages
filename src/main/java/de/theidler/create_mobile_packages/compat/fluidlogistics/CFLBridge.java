package de.theidler.create_mobile_packages.compat.fluidlogistics;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.yision.fluidlogistics.api.packager.PackageResourceCrafting;
import com.yision.fluidlogistics.api.packager.PackageResourceCraftingData;
import com.yision.fluidlogistics.api.packager.PackageResourceDisplay;
import com.yision.fluidlogistics.api.packager.PackageResources;
import com.yision.fluidlogistics.api.packager.PackageResourceTypes;
import de.theidler.create_mobile_packages.compat.Mods;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class CFLBridge {

    private static volatile boolean disabled;

    private CFLBridge() {
    }

    public static boolean isPresent() {
        return Mods.FLUIDLOGISTICS.isLoaded();
    }

    public static boolean isAvailable() {
        return safeCall(false, () -> true);
    }

    public static boolean isPackageResource(ItemStack stack) {
        return !stack.isEmpty() && safeCall(false, () -> PackageResources.findType(stack).isPresent());
    }

    public static boolean isPackageResource(GenericStack stack) {
        return isPackageResource(keyAsItemStack(stack));
    }

    public static boolean isPackageResource(BigGenericStack entry) {
        return isPackageResource(entry.get());
    }

    public static boolean isFluidResource(ItemStack stack) {
        return !stack.isEmpty() && safeCall(false, () -> PackageResources.findType(stack)
                .map(type -> PackageResourceTypes.FLUID.equals(type.id()))
                .orElse(false));
    }

    public static boolean isFluidResource(GenericStack stack) {
        return isFluidResource(keyAsItemStack(stack));
    }

    public static boolean isFluidResource(BigGenericStack entry) {
        return isFluidResource(entry.get());
    }

    public static boolean isFluidPackage(ItemStack stack) {
        return !stack.isEmpty() && safeCall(false, () -> PackageResources.inspectPackage(stack).resources().stream()
                .anyMatch(resource -> PackageResourceTypes.FLUID.equals(resource.typeId())));
    }

    public static Optional<FluidKey> createFluidKey(FluidStack fluid) {
        if (fluid.isEmpty()) {
            return Optional.empty();
        }
        return safeCall(Optional.empty(), () -> {
            ItemStack key = PackageResourceTypes.createFluidKey(fluid);
            if (key.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new FluidKey(key, PackageResourceTypes.getFluidPerPackage()));
        });
    }

    public static FluidStack fluidOf(GenericStack stack) {
        ItemStack itemStack = keyAsItemStack(stack);
        return safeCall(FluidStack.EMPTY, () -> PackageResourceTypes.getFluid(itemStack));
    }

    public static FluidStack fluidOf(BigGenericStack entry) {
        return fluidOf(entry.get());
    }

    public static boolean containsPackageResource(GenericOrder order) {
        for (GenericStack stack : order.stacks()) {
            if (isPackageResource(stack)) {
                return true;
            }
        }
        return false;
    }

    public static int adjustAmount(ItemStack stack, int currentAmount, boolean forward, boolean shift, boolean control,
            int minAmount, int maxAmount, int steps, StockInteraction interaction) {
        return safeCall(currentAmount, () -> {
            PackageResourceDisplay.Interaction apiInteraction = switch (interaction) {
                case INVENTORY -> PackageResourceDisplay.Interaction.STOCK_KEEPER_INVENTORY;
                case ORDER -> PackageResourceDisplay.Interaction.STOCK_KEEPER_ORDER;
            };
            return PackageResources.adjustAmount(stack, new PackageResourceDisplay.Adjustment(
                    currentAmount, forward, shift, control, minAmount, maxAmount, steps, apiInteraction))
                    .orElse(currentAmount);
        });
    }

    public static Optional<CraftingData> getCraftingData(CraftableBigItemStack stack) {
        return safeCall(Optional.empty(), () -> PackageResourceCrafting.get(stack)
                .map(data -> new CraftingData(data.outputCount(), data.transferLimit(), data.requirements())));
    }

    public static boolean setCraftingData(CraftableBigItemStack stack, int outputCount, int transferLimit,
            List<BigItemStack> requirements) {
        return safeCall(false, () -> {
            PackageResourceCrafting.set(
                    stack, new PackageResourceCraftingData(outputCount, transferLimit, requirements));
            return true;
        });
    }

    public static ItemStack keyAsItemStack(GenericStack stack) {
        return BigGenericStack.of(stack.withAmount(1)).asStack().stack;
    }

    static <T> T safeCall(T fallback, Supplier<T> call) {
        if (!isPresent() || disabled) {
            return fallback;
        }
        try {
            if (!PackageResources.isBootstrapped()) {
                return fallback;
            }
            return call.get();
        } catch (LinkageError | RuntimeException e) {
            disabled = true;
            return fallback;
        }
    }

    public enum StockInteraction {
        INVENTORY,
        ORDER
    }

    public record FluidKey(ItemStack stack, int transferLimit) {
    }

    public record CraftingData(int outputCount, int transferLimit, List<BigItemStack> requirements) {
        public CraftingData {
            requirements = List.copyOf(requirements);
        }
    }
}
