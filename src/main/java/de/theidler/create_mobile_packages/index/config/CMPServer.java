package de.theidler.create_mobile_packages.index.config;

import com.google.common.collect.Multimap;
import net.createmod.catnip.config.ConfigBase;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CMPServer extends ConfigBase {

    public final ConfigInt droneSpeed = i(6, 1, Integer.MAX_VALUE, "droneSpeed",
            CMPServer.Comments.droneSpeed);
    public final ConfigInt droneRotationSpeed = i(45, 1, Integer.MAX_VALUE, "droneRotationSpeed",
            CMPServer.Comments.droneRotationSpeed);
    public final ConfigBool portToPort = b(true, "portToPort",
            CMPServer.Comments.portToPort);
    public final ConfigInt droneMaxDistance = i(-1, -1, Integer.MAX_VALUE, "droneMaxDistance",
            CMPServer.Comments.droneMaxDistance);

    @Override
    public String getName() {
        return "server";
    }

    private static class Comments {
        static String droneSpeed = "The Speed of a Package Delivery Drone in Blocks per Second. Default: 6";
        static String droneRotationSpeed = "The Speed of the rotation of a Package Delivery Drone in degrees per Second. Default: 45";
        static String portToPort = "If true, the Robo Bee will be able to deliver packages from one port to another. Default: true";
        static String droneMaxDistance = "The maximum distance a RoboBee can travel. Default: -1 (unlimited)";
    }
}
