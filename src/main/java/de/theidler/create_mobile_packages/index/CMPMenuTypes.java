package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.MenuEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTicker;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.DroneControllerMenu;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTickerScreen;

public class CMPMenuTypes {

    public static final MenuEntry<DroneControllerMenu> DRONE_CONTROLLER_MENU =
            CreateMobilePackages.REGISTRATE.menu(
                    "drone_controller_menu",
                    (droneControllerMenuMenuType, containerId, playerInventory) -> new DroneControllerMenu(containerId, playerInventory, (PortableStockTicker) playerInventory.player.getMainHandItem().getItem()),
                    () -> PortableStockTickerScreen::new
            ).register();

    public static void register() {
    }
}
