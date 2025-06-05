package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.index.CMPKeys;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.OpenPortableStockTicker;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(Dist.CLIENT)
public class InputEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null)
            return;
        if (CMPKeys.OPEN_PORTABLE_STOCK_TICKER.isPressed()) {
            CatnipServices.NETWORK.sendToServer(OpenPortableStockTicker.INSTANCE);
        }
    }
}
