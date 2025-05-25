package de.theidler.create_mobile_packages.index.ponder.scenes;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class RoboBeePortScenes {
    public static void roboBeePort(SceneBuilder sceneBuilder, SceneBuildingUtil sceneBuildingUtil) {
        CreateSceneBuilder scene = new CreateSceneBuilder(sceneBuilder);
        scene.title("robo_bee_port", "Robo Bee Port");
        scene.configureBasePlate(0,0,3);
        scene.showBasePlate();
        scene.world().showSection(sceneBuildingUtil.select().everywhere(), Direction.UP);

        BlockPos beePort = sceneBuildingUtil.grid().at(1, 1, 1);

        scene.overlay()
                .showText(60)
                .text("The Robo Bee Port can send a Package to a Player")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(sceneBuildingUtil.vector()
                        .blockSurface(beePort, Direction.SOUTH));
        scene.idle(80);

        scene.overlay()
                .showText(80)
                .text("Place a package with an address matching the Player's name in the Robo Bee Port")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(sceneBuildingUtil.vector()
                        .blockSurface(beePort, Direction.SOUTH));
        scene.idle(100);

        scene.overlay()
                .showText(60)
                .text("Place a Robo Bee in the Robo Bee Slot in the Robo Bee Port")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(sceneBuildingUtil.vector()
                        .blockSurface(beePort, Direction.SOUTH));
        scene.idle(80);

        scene.overlay()
                .showText(40)
                .text("The Port will now send the Robo Bee to the Player who's name matches the address in the package")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(sceneBuildingUtil.vector()
                        .blockSurface(beePort, Direction.SOUTH));
        scene.idle(60);
    }

    public static void pullFromChest(SceneBuilder sceneBuilder, SceneBuildingUtil sceneBuildingUtil) {
        CreateSceneBuilder scene = new CreateSceneBuilder(sceneBuilder);
        scene.title("pull_from_chest", "Pull from Chest");
        scene.configureBasePlate(0,0,4);
        scene.showBasePlate();
        scene.world().showSection(sceneBuildingUtil.select().everywhere(), Direction.UP);

        BlockPos beePort = sceneBuildingUtil.grid().at(1, 1, 1);
        BlockPos chest = sceneBuildingUtil.grid().at(2, 1, 1);

        scene.overlay()
            .showText(40)
            .text("Put a package into the chest")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(chest, Direction.UP));
        scene.idle(60);

        scene.overlay()
            .showText(60)
            .text("The port will pull the package from the any adjacent inventory's and take it in")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(beePort, Direction.SOUTH));
        scene.idle(80);
    }

    public static void simpleSetup(SceneBuilder sceneBuilder, SceneBuildingUtil sceneBuildingUtil) {
        CreateSceneBuilder scene = new CreateSceneBuilder(sceneBuilder);
        scene.title("simple_setup", "Simple Setup");
        scene.configureBasePlate(0,0,5);
        scene.showBasePlate();
        scene.world().showSection(sceneBuildingUtil.select().everywhere(), Direction.UP);

        BlockPos packager = sceneBuildingUtil.grid().at(2, 1, 2);
        BlockPos stocklink = sceneBuildingUtil.grid().at(2, 2, 2);
        BlockPos chest = sceneBuildingUtil.grid().at(3, 1, 2);
        BlockPos beePort = sceneBuildingUtil.grid().at(1, 1, 2);

        scene.overlay()
            .showText(40)
            .text("This is a basic setup for automated package handling")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(beePort, Direction.DOWN));
        scene.idle(60);

        scene.overlay()
                .showText(40)
                .text("Place a Chest or any other storage")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(sceneBuildingUtil.vector().blockSurface(chest, Direction.UP));
        scene.idle(60);

        scene.overlay()
            .showText(40)
            .text("Place a Packager facing the storage to create packages")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(packager, Direction.DOWN));
        scene.idle(60);

        scene.overlay()
            .showText(40)
            .text("Connect a Stocklink to the Packager to connect it to a logistics network")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(stocklink, Direction.DOWN));
        scene.idle(60);

        scene.overlay()
            .showText(40)
            .text("Finally, place a Robo Bee Port to send packages")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(beePort, Direction.UP));
        scene.idle(60);
    }

    public static void pushToChest(SceneBuilder sceneBuilder, SceneBuildingUtil sceneBuildingUtil) {
        CreateSceneBuilder scene = new CreateSceneBuilder(sceneBuilder);
        scene.title("push_to_chest", "Push to Chest");
        scene.configureBasePlate(0,0,4);
        scene.showBasePlate();
        scene.world().showSection(sceneBuildingUtil.select().everywhere(), Direction.UP);

        BlockPos beePort = sceneBuildingUtil.grid().at(2, 1, 1);
        BlockPos chest = sceneBuildingUtil.grid().at(1, 1, 1);

        scene.overlay()
            .showText(40)
            .text("The Robo Bee Port can push it to adjacent inventories when powered by redstone.")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(beePort, Direction.SOUTH));
        scene.idle(60);

        scene.overlay()
            .showText(40)
            .text("Make sure the port is powered by redstone.")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(beePort, Direction.DOWN));
        scene.idle(60);

        scene.overlay()
            .showText(40)
            .text("The package will be pushed into any adjacent inventory, such as a chest.")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(chest, Direction.UP));
        scene.idle(60);
    }

    public static void crafterSetup(SceneBuilder sceneBuilder, SceneBuildingUtil sceneBuildingUtil) {
        CreateSceneBuilder scene = new CreateSceneBuilder(sceneBuilder);
        scene.title("crafter_setup", "Crafter Setup");
        scene.configureBasePlate(0,0,8);
        scene.showBasePlate();
        scene.world().showSection(sceneBuildingUtil.select().everywhere(), Direction.UP);

        BlockPos beePort = sceneBuildingUtil.grid().at(1, 3, 3);
        BlockPos repackager = sceneBuildingUtil.grid().at(2, 3, 3);
        BlockPos chute = sceneBuildingUtil.grid().at(2, 2, 3);
        BlockPos crafter = sceneBuildingUtil.grid().at(4, 2, 3);
        BlockPos chest = sceneBuildingUtil.grid().at(6, 1, 3);

        scene.overlay()
            .showText(60)
            .text("This is a setup for automated crafting and with the Robo Bee Port.")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(beePort, Direction.UP));
        scene.idle(80);

        scene.overlay()
            .showText(60)
            .text("The Re-Packager splits the package into multiple smaller packages.")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(repackager, Direction.UP));
        scene.idle(80);

        scene.overlay()
            .showText(60)
            .text("Packages are sent through a Chute to the Packager.")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(chute, Direction.DOWN));
        scene.idle(80);

        scene.overlay()
            .showText(60)
            .text("The Packager is connected to a 3x3 Mechanical Crafter, which crafts the items.")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(crafter, Direction.UP));
        scene.idle(80);

        scene.overlay()
            .showText(60)
            .text("The crafted output is placed into a Chest.")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(chest, Direction.UP));
        scene.idle(80);
    }

    public static void portToPort(SceneBuilder sceneBuilder, SceneBuildingUtil sceneBuildingUtil) {
        CreateSceneBuilder scene = new CreateSceneBuilder(sceneBuilder);
        scene.title("port_to_port", "Port to Port");
        scene.configureBasePlate(0,0,6);
        scene.showBasePlate();
        scene.world().showSection(sceneBuildingUtil.select().everywhere(), Direction.UP);

        BlockPos portA = sceneBuildingUtil.grid().at(1, 1, 2);
        BlockPos portB = sceneBuildingUtil.grid().at(4, 1, 2);

        scene.overlay()
            .showText(60)
            .text("The Robo Bee Port can also send packages to another port.")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(portA, Direction.UP));
        scene.idle(80);

        scene.overlay()
            .showText(60)
            .text("Set an address in the destination port.")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(portB, Direction.UP));
        scene.idle(80);

        scene.overlay()
            .showText(60)
            .text("Set the same address as the package address and place the package in the first port.")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(portA, Direction.UP));
        scene.idle(80);

        scene.overlay()
            .showText(60)
            .text("The package will be sent from one port to the other.")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(portB, Direction.UP));
        scene.idle(80);

        scene.overlay()
            .showText(60)
            .text("This feature can be disabled in the configuration files.")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(sceneBuildingUtil.vector().blockSurface(portA, Direction.UP));
        scene.idle(80);
    }
}
