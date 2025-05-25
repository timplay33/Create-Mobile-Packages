package de.theidler.create_mobile_packages.compat.create_factory_logistics;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientGui;
import ru.zznty.create_factory_logistics.logistics.stock.IngredientInventorySummary;

import java.util.List;
import java.util.UUID;

public class FactoryLogisticsCompat {
    public static boolean tryBroadcast(UUID freq, LogisticallyLinkedBehaviour.RequestType type, PackageOrderWithCrafts order, IdentifiedInventory ignoredHandler, String address) {
        return ru.zznty.create_factory_logistics.logistics.panel.request.IngredientLogisticsManager.broadcastPackageRequest(
                freq, type,
                ru.zznty.create_factory_logistics.logistics.panel.request.IngredientOrder.of(order),
                ignoredHandler, address
        );
    }

    public static void renderSlot(GuiGraphics graphics, BigItemStack entry, int x, int y) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        IngredientGui.renderSlot(graphics, stack.ingredient().key(), x, y);
    }

    public static void renderComponentTooltip(GuiGraphics graphics, Font font, BigItemStack entry, int mouseX, int mouseY) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        graphics.renderComponentTooltip(font, IngredientGui.tooltipBuilder(stack.ingredient().key(), stack.getCount()), mouseX, mouseY);
    }

    public static void renderItemDecorations(GuiGraphics graphics, BigItemStack entry, int x, int y) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        IngredientGui.renderDecorations(graphics, stack.ingredient(), x, y);
    }

    public static int getMaxStackSize(BigItemStack entry) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        return IngredientGui.stackSize(stack.ingredient().key());
    }

    public static List<Component> getCraftableTooltip(BigItemStack entry) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        return IngredientGui.tooltipBuilder(stack.ingredient().key(), stack.ingredient().amount());
    }

    public static int getCountIn(InventorySummary summary, BigItemStack stack) {
        BigIngredientStack bigStack = (BigIngredientStack) stack;
        return ((IngredientInventorySummary) summary).getCountOf(bigStack);
    }

    public static boolean eraseFromForced(InventorySummary forcedEntries, BigItemStack stack) {
        BigIngredientStack bigIngredientStack = (BigIngredientStack) stack;
        IngredientInventorySummary summary = (IngredientInventorySummary) forcedEntries;
        return summary.erase(bigIngredientStack.ingredient().key());
    }

    public static BigItemStack getOrderForItem(ItemStack stack, List<BigItemStack> itemsToOrder) {
        BigIngredientStack bigIngredientStack = (BigIngredientStack) new BigItemStack(stack, stack.getCount());
        BigIngredientStack order = getOrderForIngredient(bigIngredientStack.ingredient(), itemsToOrder);
        return order == null ? null : order.asStack();
    }

    private static BigIngredientStack getOrderForIngredient(BoardIngredient ingredient, List<BigItemStack> itemsToOrder) {
        for (BigItemStack entry : itemsToOrder) {
            BigIngredientStack stack = (BigIngredientStack) entry;
            if (stack.ingredient().canStack(ingredient))
                return stack;
        }
        return null;
    }

    public static void renderIngredientEntryAmount(GuiGraphics graphics, int customCount, BigItemStack entry, boolean isStackHovered, boolean isRenderingOrders) {
        if (isStackHovered && isRenderingOrders && !(entry instanceof CraftableBigItemStack)) return;
        BigIngredientStack stack = (BigIngredientStack) entry;
        IngredientGui.renderDecorations(graphics, stack.ingredient().withAmount(customCount), 1, 1);
    }
}
