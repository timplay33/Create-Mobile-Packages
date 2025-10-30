package de.theidler.create_mobile_packages.compat.curios;

import de.theidler.create_mobile_packages.index.CMPItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

public class Curios {

    private static Optional<Map<String, ICurioStacksHandler>> resolveCuriosMap(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(ICuriosItemHandler::getCurios);
    }

    public static ItemStack findPortableStockTickerCurios(LivingEntity entity) {
        return resolveCuriosMap(entity).map(curiosMap -> {
            for (ICurioStacksHandler stacksHandler : curiosMap.values()) {
                // Search all the curio slots for PST existing
                int slots = stacksHandler.getSlots();
                for (int slot = 0; slot < slots; slot++) {
                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(slot);
                    if (CMPItems.PORTABLE_STOCK_TICKER.isIn(stack)) {
                        return stack;
                    }
                }
            }
            return ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
    }
}
