package de.theidler.create_mobile_packages.compat.emi;

import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTickerScreen;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.minecraft.client.renderer.Rect2i;

public class CMPEMI implements EmiPlugin {

    /**
     * Sets the search text in EMI's search field
     *
     * @param searchText The text to set in EMI's search field
     */
    public static void setSearchText(String searchText) {
        EmiApi.setSearchText(searchText);
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addExclusionArea(PortableStockTickerScreen.class, (screen, consumer) -> {
            for (Rect2i rect : screen.getExtraAreas()) {
                consumer.accept(
                        new dev.emi.emi.api.widget.Bounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
            }
        });
    }
}
