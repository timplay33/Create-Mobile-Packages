package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.MenuEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.items.DroneControllerMenu;
import de.theidler.create_mobile_packages.items.DroneControllerScreen;

public class CMPMenuTypes {

    public static final MenuEntry<DroneControllerMenu> DRONE_CONTROLLER_MENU =
            CreateMobilePackages.REGISTRATE.menu(
                    "drone_controller_menu",
                    DroneControllerMenu::new,
                    () -> DroneControllerScreen::new
            ).register();

    public static void register() {
    }
}
