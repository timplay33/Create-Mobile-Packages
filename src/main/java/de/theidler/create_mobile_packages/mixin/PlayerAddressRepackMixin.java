package de.theidler.create_mobile_packages.mixin;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.repackager.PackageRepackageHelper;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = PackageRepackageHelper.class, remap = false)
public class PlayerAddressRepackMixin {
    @Inject(method = "repackBasedOnRecipes", at = @At("HEAD"), cancellable = true, remap = false)
    private void create_mobile_packages$bundlePlayerAddress(
            InventorySummary summary, PackageOrderWithCrafts order, String address, RandomSource random,
            CallbackInfoReturnable<List<BigItemStack>> cir
    ) {
        // A player address has no crafter waiting on one-recipe slices.
        if (address != null && address.startsWith("@")) cir.setReturnValue(List.of());
    }
}
