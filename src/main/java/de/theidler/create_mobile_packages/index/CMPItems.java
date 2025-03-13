package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.ItemEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.items.DroneController;


public class CMPItems {

    static {
        CreateMobilePackages.REGISTRATE.setCreativeTab(CMPCreativeModeTabs.CREATE_MOBILE_PACKAGES_TAB);
    }

    public static final ItemEntry<DroneController> DRONE_CONTROLLER =
            CreateMobilePackages.REGISTRATE.item("drone_controller", DroneController::new)
                    .register();
    public static void register() {
    }
}
