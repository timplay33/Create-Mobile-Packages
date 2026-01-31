package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.index.CMPKeys;
import de.theidler.create_mobile_packages.index.CMPPackets;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.OpenPortableStockTicker;
import de.theidler.create_mobile_packages.network_settings.PlayerNetworksScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class InputEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null)
            return;
        if (CMPKeys.OPEN_PORTABLE_STOCK_TICKER.isPressed()) {
            CMPPackets.getChannel().sendToServer(new OpenPortableStockTicker());
        }
        if (CMPKeys.OPEN_PLAYER_NETWORKS_SCREEN.isPressed()) {
            Minecraft.getInstance().setScreen(new PlayerNetworksScreen(
                    Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getDisplayName() : Component.empty()
            ));
        }
    }
}
