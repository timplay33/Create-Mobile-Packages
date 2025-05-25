package de.theidler.create_mobile_packages.index.config;

import net.createmod.catnip.config.ConfigBase;

public class CMPServer extends ConfigBase {

    public final ConfigInt beeSpeed = i(6, 1, Integer.MAX_VALUE, "beeSpeed",
            CMPServer.Comments.beeSpeed);
    public final ConfigInt beeRotationSpeed = i(45, 1, Integer.MAX_VALUE, "beeRotationSpeed",
            CMPServer.Comments.beeRotationSpeed);
    public final ConfigBool portToPort = b(true, "portToPort",
            CMPServer.Comments.portToPort);
    public final ConfigInt beeMaxDistance = i(-1, -1, Integer.MAX_VALUE, "beeMaxDistance",
            CMPServer.Comments.beeMaxDistance);
    public final ConfigBool displayNametag = b(true, "displayNametag",
            Comments.displayNametag);
    public final ConfigBool allowRoboBeeSpawnPackageTransport = b(true, "allowRoboBeeSpawnPackageTransport",
            CMPServer.Comments.allowRoboBeeSpawnPackageTransport);

    @Override
    public String getName() {
        return "server";
    }

    private static class Comments {
        static String beeSpeed = "The Speed of a Robo Bee in Blocks per Second. Default: 6";
        static String beeRotationSpeed = "The Speed of the rotation of a Robo Bee in degrees per Second. Default: 45";
        static String portToPort = "If true, the Robo Bee will be able to deliver packages from one port to another. Default: true";
        static String beeMaxDistance = "The maximum distance a Robo Bee can travel. Default: -1 (unlimited)";
        static String displayNametag = "If true, the Robo Bee will display a nametag with the address of the package it is carrying. Default: true";
        static String allowRoboBeeSpawnPackageTransport = "If true, when Spawning a Robo Bee with the Item it will take any Package from the offhand and deliver it. Default: true";
    }
}
