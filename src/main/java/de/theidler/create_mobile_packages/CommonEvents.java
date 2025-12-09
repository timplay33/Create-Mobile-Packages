package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.robo.RoboManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommonEvents {

    @SubscribeEvent
    public static void onServerWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        if (event.side == LogicalSide.CLIENT) return;
        if (!(event.level instanceof net.minecraft.server.level.ServerLevel)) return;
        RoboManager.get((ServerLevel) event.level).tick((ServerLevel) event.level);
    }

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof RoboEntity)) return;

        event.setCanceled(true);
    }

}
