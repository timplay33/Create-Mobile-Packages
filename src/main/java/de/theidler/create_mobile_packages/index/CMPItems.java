package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.ItemEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.items.drone_controller.DroneController;
import de.theidler.create_mobile_packages.items.mobile_packager.MobilePackager;


public class CMPItems {

    static {
        CreateMobilePackages.REGISTRATE.setCreativeTab(CMPCreativeModeTabs.CREATE_MOBILE_PACKAGES_TAB);
    }

    public static final ItemEntry<DroneController> DRONE_CONTROLLER =
            CreateMobilePackages.REGISTRATE.item("drone_controller", DroneController::new)
                    .register();

    public static final ItemEntry<MobilePackager> MOBILE_PACKAGER =
            CreateMobilePackages.REGISTRATE.item("mobile_packager", MobilePackager::new)
                    .register();

    public static void register() {
    }
}
