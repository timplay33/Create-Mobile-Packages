package de.theidler.create_mobile_packages;

import net.minecraftforge.event.TickEvent;
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
}
