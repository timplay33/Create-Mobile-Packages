package de.theidler.create_mobile_packages.index;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    @SubscribeEvent
    public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        BeePortBlockEntity.registerCapabilities(event);
    }
}
