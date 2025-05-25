package de.theidler.create_mobile_packages.index.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import de.theidler.create_mobile_packages.index.CMPBlocks;
import de.theidler.create_mobile_packages.index.ponder.scenes.RoboBeePortScenes;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CMPPonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        HELPER.forComponents(CMPBlocks.BEE_PORT)
                .addStoryBoard("robobeeport", RoboBeePortScenes::roboBeePort);
        HELPER.forComponents(CMPBlocks.BEE_PORT)
                .addStoryBoard("pullfromchest", RoboBeePortScenes::pullFromChest);
        HELPER.forComponents(CMPBlocks.BEE_PORT)
                .addStoryBoard("simplesetup", RoboBeePortScenes::simpleSetup);
        HELPER.forComponents(CMPBlocks.BEE_PORT)
                .addStoryBoard("pushtochest", RoboBeePortScenes::pushToChest);
        HELPER.forComponents(CMPBlocks.BEE_PORT)
                .addStoryBoard("craftersetup", RoboBeePortScenes::crafterSetup);
        HELPER.forComponents(CMPBlocks.BEE_PORT)
                .addStoryBoard("porttoport", RoboBeePortScenes::portToPort);

    }
}
