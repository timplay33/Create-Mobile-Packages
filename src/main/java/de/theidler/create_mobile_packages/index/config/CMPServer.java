package de.theidler.create_mobile_packages.index.config;

import net.createmod.catnip.config.ConfigBase;

public class CMPServer extends ConfigBase {

    public final ConfigInt droneSpeed = i(6, 1, Integer.MAX_VALUE, "droneSpeed",
            CMPServer.Comments.droneSpeed);

    @Override
    public String getName() {
        return "server";
    }

    private static class Comments {
        static String droneSpeed = "The Speed of a Package Delivery Drone in Blocks per Second. Default: 6";
    }
}
