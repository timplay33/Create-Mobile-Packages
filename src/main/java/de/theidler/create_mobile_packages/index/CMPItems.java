package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.ItemEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.items.drone_controller.DroneController;
import de.theidler.create_mobile_packages.items.robo_bee.RoboBeeItem;


public class CMPItems {

    static {
        CreateMobilePackages.REGISTRATE.setCreativeTab(CMPCreativeModeTabs.CREATE_MOBILE_PACKAGES_TAB);
    }

    public static final ItemEntry<DroneController> DRONE_CONTROLLER =
            CreateMobilePackages.REGISTRATE.item("drone_controller", DroneController::new)
                    .register();

    public static final ItemEntry<RoboBeeItem> ROBO_BEE =
            CreateMobilePackages.REGISTRATE.item("robo_bee",RoboBeeItem::new)
                    .register();

    public static void register() {
    }
}
