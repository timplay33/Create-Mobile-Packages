package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.index.ponder.CMPPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;

public class CreateMobilePackagesClient {
    public static void registerClientEvents() {
        registerPonder();
    }

    private static void registerPonder() {
        CreateMobilePackages.LOGGER.info("Registering PonderIndex");
        PonderIndex.addPlugin(new CMPPonderPlugin());
    }


}
