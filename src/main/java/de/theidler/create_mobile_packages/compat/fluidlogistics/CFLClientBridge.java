package de.theidler.create_mobile_packages.compat.fluidlogistics;

import com.yision.fluidlogistics.render.FluidSlotAmountRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class CFLClientBridge {

    public static void renderPackageFluidAmount(GuiGraphics graphics, ItemStack stack, int itemX, int itemY) {
        int amount = CFLBridge.getPackageFluidAmount(stack);
        if (amount <= 0) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(itemX, itemY, 0);
        FluidSlotAmountRenderer.renderInStockKeeper(graphics, amount);
        graphics.pose().popPose();
    }
}
