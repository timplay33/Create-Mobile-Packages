package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.items.package_cannon.PackageCannonRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
            event.register(CMPItems.PACKAGE_CANNON, PackageCannonRenderer.DECORATOR);
        }
    }
}
