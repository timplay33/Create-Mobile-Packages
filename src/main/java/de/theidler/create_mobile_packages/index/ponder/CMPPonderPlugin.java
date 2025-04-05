package de.theidler.create_mobile_packages.index.ponder;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CMPPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return CreateMobilePackages.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        CMPPonderScenes.register(helper);
    }
}
