package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.robo.RoboManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;



@EventBusSubscriber
public class CommonEvents {

    @SubscribeEvent
    public static void onServerWorldTick(LevelTickEvent.Pre event) {
        Level level = event.getLevel();
        if (level instanceof ServerLevel serverLevel && !Minecraft.getInstance().hasSingleplayerServer())
            RoboManager.get(serverLevel).tick(serverLevel);
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @net.neoforged.bus.api.SubscribeEvent
        public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
            BeePortBlockEntity.registerCapabilities(event);
        }
    }
}
