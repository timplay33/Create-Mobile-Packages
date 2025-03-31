package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.MenuEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.items.drone_controller.DroneController;
import de.theidler.create_mobile_packages.items.drone_controller.DroneControllerMenu;
import de.theidler.create_mobile_packages.items.drone_controller.DroneControllerScreen;

import static de.theidler.create_mobile_packages.items.drone_controller.StockCheckingItem.getAccurateSummary;

public class CMPMenuTypes {

    public static final MenuEntry<DroneControllerMenu> DRONE_CONTROLLER_MENU =
            CreateMobilePackages.REGISTRATE.menu(
                    "drone_controller_menu",
                    (droneControllerMenuMenuType, containerId, playerInventory) -> new DroneControllerMenu(containerId, playerInventory, getAccurateSummary(playerInventory.player.getMainHandItem()).getStacks(), (DroneController) playerInventory.player.getMainHandItem().getItem()),
                    () -> DroneControllerScreen::new
            ).register();

    public static void register() {
    }
}
