package de.theidler.create_mobile_packages.compat.jei;

import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTickerScreen;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class CMPJEIGuiHandler implements IGuiContainerHandler<PortableStockTickerScreen> {
    private final IIngredientManager ingredientManager;

    public CMPJEIGuiHandler(IIngredientManager ingredientManager) {
        this.ingredientManager = ingredientManager;
    }

    @Override
    public @NotNull List<Rect2i> getGuiExtraAreas(PortableStockTickerScreen containerScreen) {
        return containerScreen.getExtraAreas();
    }

    @Override
    public @NotNull Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
            PortableStockTickerScreen containerScreen, double mouseX, double mouseY) {
        return containerScreen.getHoveredIngredient((int) mouseX, (int) mouseY)
                .flatMap(pair -> ingredientManager.createClickableIngredient(pair.getFirst(), pair.getSecond(), true));
    }
}
