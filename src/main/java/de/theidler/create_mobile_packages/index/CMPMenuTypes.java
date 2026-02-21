package de.theidler.create_mobile_packages.index;

import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import com.tterrag.registrate.util.entry.MenuEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortMenu;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortScreen;
import de.theidler.create_mobile_packages.items.mobile_packager.*;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTicker;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTickerMenu;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTickerScreen;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu.TrashMenu;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu.TrashScreen;

public class CMPMenuTypes {

    public static final MenuEntry<PortableStockTickerMenu> PORTABLE_STOCK_TICKER_MENU =
            CreateMobilePackages.REGISTRATE.menu(
                    "portable_stock_ticker_menu",
                    (menuType, containerId, playerInventory) -> new PortableStockTickerMenu(containerId, playerInventory),
                    () -> PortableStockTickerScreen::new
            ).register();

    public static final MenuEntry<PackagePortMenu> BEE_PORT_MENU =
            CreateMobilePackages.REGISTRATE.menu(
                    "bee_port_menu",
                    BeePortMenu::new,
                    () -> BeePortScreen::new
            ).register();

    public static final MenuEntry<MobilePackagerMenu> MOBILE_PACKAGER_MENU =
            CreateMobilePackages.REGISTRATE.menu(
                    "mobile_packager_menu",
                    (mobilePackagerMenuType, containerId, playerInventory) -> new MobilePackagerMenu(containerId, playerInventory, (MobilePackager) playerInventory.player.getMainHandItem().getItem()),
                    () -> MobilePackagerScreen::new
            ).register();

    public static final MenuEntry<MobilePackagerEditMenu> MOBILE_PACKAGER_EDIT_MENU =
            CreateMobilePackages.REGISTRATE.menu(
                    "mobile_packager_edit_menu",
                    MobilePackagerEditMenu::new,
                    () -> MobilePackagerEditScreen::new
            ).register();

    public static final MenuEntry<TrashMenu> TRASH_MENU =
            CreateMobilePackages.REGISTRATE.menu(
                    "trash_menu",
                    (trashMenuType, containerId, playerInventory) -> new TrashMenu(containerId, playerInventory, (PortableStockTicker) PortableStockTicker.find(playerInventory).getItem()),
                    () -> TrashScreen::new
            ).register();

    public static void register() {
    }
}
