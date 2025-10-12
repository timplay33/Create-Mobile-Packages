package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.robo.RoboManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class CommonEvents {

    @SubscribeEvent
    public static void onServerWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        if (event.side == LogicalSide.CLIENT) return;
        if (!(event.level instanceof net.minecraft.server.level.ServerLevel)) return;
        RoboManager.get((ServerLevel) event.level).tick((ServerLevel) event.level);
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @net.neoforged.bus.api.SubscribeEvent
        public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {

            BeePortBlockEntity.registerCapabilities(event);
        }
    }
}
