package de.theidler.create_mobile_packages.index.config;

import com.google.common.collect.Multimap;
import net.createmod.catnip.config.ConfigBase;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CMPServer extends ConfigBase {

    public final ConfigInt droneSpeed = i(6, 1, Integer.MAX_VALUE, "droneSpeed",
            CMPServer.Comments.droneSpeed);
    public final ConfigInt droneRotationSpeed = i(45, 1, Integer.MAX_VALUE, "droneRotationSpeed",
            CMPServer.Comments.droneRotationSpeed);

    @Override
    public String getName() {
        return "server";
    }

    private static class Comments {
        static String droneSpeed = "The Speed of a Package Delivery Drone in Blocks per Second. Default: 6";
        static String droneRotationSpeed = "The Speed of the rotation of a Package Delivery Drone in degrees per Second. Default: 45";
    }
}
