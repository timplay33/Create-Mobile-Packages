package de.theidler.create_mobile_packages.index.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import de.theidler.create_mobile_packages.index.CMPBlocks;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class CMPPonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        HELPER.forComponents(CMPBlocks.DRONE_PORT)
                .addStoryBoard("drone_port", CMPPonderScenes::dronePortScene);
    }

    public static void dronePortScene(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("drone_port", "Drone Port");
        scene.configureBasePlate(-2, 0, 5);
        scene.world().showSection(util.select().everywhere(), Direction.UP);

        BlockPos dronePort = util.grid().at(1, 1, 2);

        scene.overlay().showText(30).text("The Drone Port can send Packages to Player").placeNearTarget().pointAt(util.vector().centerOf(dronePort));

    }
}
