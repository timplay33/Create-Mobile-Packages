package de.theidler.create_mobile_packages.compat.curios;

import de.theidler.create_mobile_packages.index.CMPItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

// from package com.simibubi.create.compat.curios.Curios
public class Curios {

    private static Optional<Map<String, ICurioStacksHandler>> resolveCuriosMap(LivingEntity entity) {
        return entity.getCapability(CuriosCapability.INVENTORY).map(ICuriosItemHandler::getCurios);
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(Curios::onInterModEnqueue);
        //modEventBus.addListener(onClientSetup);
    }

    public static ItemStack findPortableStockTickerCurios(LivingEntity entity) {
        return resolveCuriosMap(entity).map(curiosMap -> {
            for (ICurioStacksHandler stacksHandler : curiosMap.values()) {
                // Search all the curio slots for PST existing
                int slots = stacksHandler.getSlots();
                for (int slot = 0; slot < slots; slot++) {
                    if (CMPItems.PORTABLE_STOCK_TICKER.isIn(stacksHandler.getStacks().getStackInSlot(slot))) {
                        return stacksHandler.getStacks().getStackInSlot(slot);
                    }
                }
            }
            return ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
    }

    private static void onInterModEnqueue(final InterModEnqueueEvent event) {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BODY.getMessageBuilder()
                .build());
    }

}
