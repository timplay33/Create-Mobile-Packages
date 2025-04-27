package de.theidler.create_mobile_packages.index;

import de.theidler.create_mobile_packages.blocks.drone_port.DronePortBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    @SubscribeEvent
    public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        DronePortBlockEntity.registerCapabilities(event);
    }
}
