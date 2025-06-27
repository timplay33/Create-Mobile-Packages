package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.index.CMPKeys;
import de.theidler.create_mobile_packages.index.CMPPackets;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.OpenPortableStockTicker;
import net.minecraft.client.Minecraft;
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
    }
}
