package de.theidler.create_mobile_packages.index.config;

import net.createmod.catnip.config.ConfigBase;

public class CMPServer extends ConfigBase {

    public final ConfigInt dronePortDeliveryDelay = i(0, 0, 60 * 60, "dronePortDeliveryDelay",
            CMPServer.Comments.dronePortDeliveryDelay);

    @Override
    public String getName() {
        return "server";
    }

    private static class Comments {
        static String dronePortDeliveryDelay = "Delay for Drone Port deliveries in Seconds. Default: 0";
    }
}
