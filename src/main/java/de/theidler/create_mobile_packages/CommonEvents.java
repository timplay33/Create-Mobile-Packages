package de.theidler.create_mobile_packages;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber
public class CommonEvents {

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Pre event) {
        Level world = event.getLevel();
        if (world.isClientSide() && Minecraft.getInstance().hasSingleplayerServer())
            return;
        CreateMobilePackages.ROBO_MANAGER.tick(world);
    }

    @SubscribeEvent
    public static void onLoadWorld(LevelEvent.Load event) {
        LevelAccessor world = event.getLevel();
        CreateMobilePackages.ROBO_MANAGER.levelLoaded(world);
    }
}
