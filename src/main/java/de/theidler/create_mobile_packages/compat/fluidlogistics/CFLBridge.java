package de.theidler.create_mobile_packages.compat.fluidlogistics;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.yision.fluidlogistics.config.Config;
import com.yision.fluidlogistics.item.CompressedTankItem;
import com.yision.fluidlogistics.registry.AllItems;
import com.yision.fluidlogistics.util.FluidAmountHelper;
import com.yision.fluidlogistics.util.IFluidCraftableBigItemStack;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

import java.util.List;

public class CFLBridge {

    public static boolean isLoaded() {
        return de.theidler.create_mobile_packages.compat.Mods.FLUIDLOGISTICS.isLoaded();
    }

    public static boolean isVirtualFluid(ItemStack stack) {
        return stack.getItem() instanceof CompressedTankItem && CompressedTankItem.isVirtual(stack);
    }

    public static boolean isVirtualFluid(GenericStack stack) {
        ItemStack itemStack = keyAsItemStack(stack);
        if (itemStack.getItem() instanceof CompressedTankItem) {
            return CompressedTankItem.isVirtual(itemStack);
        }
        return false;
    }

    public static boolean isVirtualFluid(BigGenericStack entry) {
        return isVirtualFluid(entry.get());
    }

    public static GenericStack toVirtualFluidStack(FluidStack fluid, int amountMb) {
        ItemStack virtualTank = new ItemStack(AllItems.COMPRESSED_STORAGE_TANK.get());
        CompressedTankItem.setFluidVirtual(virtualTank, fluid.copyWithAmount(1));
        return GenericStack.wrap(virtualTank).withAmount(amountMb);
    }

    public static FluidStack fluidOf(GenericStack stack) {
        if (!isVirtualFluid(stack)) return FluidStack.EMPTY;
        return CompressedTankItem.getFluid(keyAsItemStack(stack));
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

    public static int adjustFluidRequestAmount(int currentAmount, boolean forward, boolean shift, boolean control,
                                                int minAmount, int maxAmount, int steps) {
        return FluidAmountHelper.adjustFluidRequestAmount(currentAmount, forward, shift, control, minAmount, maxAmount, steps);
    }

    public static int adjustFluidRequestAmount(int currentAmount, boolean forward, boolean shift, boolean control,
                                                int minAmount, int maxAmount) {
        return FluidAmountHelper.adjustFluidRequestAmount(currentAmount, forward, shift, control, minAmount, maxAmount);
    }

    public static String formatFluidAmount(int amount) {
        return FluidAmountHelper.formatStockKeeper(amount);
    }

    public static int getFluidPerPackage() {
        return Config.getFluidPerPackage();
    }

    public static boolean hasCustomRecipeData(CraftableBigItemStack cbis) {
        if (cbis instanceof IFluidCraftableBigItemStack fluidCbis) {
            return fluidCbis.fluidlogistics$hasCustomRecipeData();
        }
        return false;
    }

    public static int getCustomOutputCount(CraftableBigItemStack cbis) {
        if (cbis instanceof IFluidCraftableBigItemStack fluidCbis) {
            return fluidCbis.fluidlogistics$getCustomOutputCount();
        }
        return 0;
    }

    public static int getCustomTransferLimit(CraftableBigItemStack cbis) {
        if (cbis instanceof IFluidCraftableBigItemStack fluidCbis) {
            return fluidCbis.fluidlogistics$getCustomTransferLimit();
        }
        return 0;
    }

    public static List<BigItemStack> getCustomRequirements(CraftableBigItemStack cbis) {
        if (cbis instanceof IFluidCraftableBigItemStack fluidCbis) {
            return fluidCbis.fluidlogistics$getCustomRequirements();
        }
        return List.of();
    }

    public static void setCustomRecipeData(CraftableBigItemStack cbis, int outputCount, int transferLimit,
                                            List<BigItemStack> requirements) {
        if (cbis instanceof IFluidCraftableBigItemStack fluidCbis) {
            fluidCbis.fluidlogistics$setCustomRecipeData(outputCount, transferLimit, requirements);
        }
    }

    public static ItemStack keyAsItemStack(GenericStack stack) {
        return BigGenericStack.of(stack.withAmount(1)).asStack().stack;
    }
}
