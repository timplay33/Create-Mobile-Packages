package de.theidler.create_mobile_packages;

import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommonEvents {

    @SubscribeEvent
    public static void onServerWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START)
            return;
        if (event.side == LogicalSide.CLIENT)
            return;
        CreateMobilePackages.ROBO_MANAGER.tick(event.level);
    }

    @SubscribeEvent
    public static void onLoadWorld(LevelEvent.Load event) {
        LevelAccessor world = event.getLevel();
        CreateMobilePackages.ROBO_MANAGER.levelLoaded(world);
    }
}
