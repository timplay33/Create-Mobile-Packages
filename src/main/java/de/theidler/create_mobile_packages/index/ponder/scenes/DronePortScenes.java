package de.theidler.create_mobile_packages.index.ponder.scenes;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.infrastructure.ponder.scenes.highLogistics.PonderHilo;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class DronePortScenes {
    public static void dronePortScene(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("drone_port", "Drone Port");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();

        scene.world().showSection(util.select().everywhere(), Direction.UP);

        BlockPos dronePort = util.grid().at(3, 2, 0);
        BlockPos packager = util.grid()
                .at(3, 2, 5);
        Selection belt = util.select().fromTo(3, 1, 0, 3, 1, 7);

        // Belt
        scene.world().setKineticSpeed(belt, -24);

        scene.overlay()
                .showText(90)
                .text("The Drone Port can send a Package to a Player")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector()
                        .blockSurface(dronePort, Direction.SOUTH));

        scene.idle(100);

        // Package
        ItemStack box = PackageStyles.getDefaultBox()
                .copy();
        PackageItem.addAddress(box, "Dev");
        PonderHilo.packagerCreate(scene, packager, box);
        scene.idle(30);

        scene.overlay()
                .showText(90)
                .text("Set the Package address to the Player name")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector()
                        .blockSurface(packager, Direction.SOUTH));
        scene.idle(100);

        scene.world()
                .createItemOnBelt(util.grid()
                        .at(3, 1, 4), Direction.SOUTH, box);
        PonderHilo.packagerClear(scene, packager);

        scene.rotateCameraY(-90);

        scene.idle(70);

        scene.world()
                .removeItemsFromBelt(util.grid()
                        .at(3, 1, 1));
        scene.world()
                .flapFunnel(util.grid()
                        .at(3, 2, 1), false);

        scene.overlay()
                .showText(90)
                .text("The Package will now be send to the Player")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector()
                        .blockSurface(dronePort, Direction.SOUTH));
        scene.idle(100);

    }
}
