package de.theidler.create_mobile_packages.index.ponder;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CMPPonderPlugin implements PonderPlugin {
    @Override
    public @NotNull String getModId() {
        return CreateMobilePackages.MODID;
    }

    @Override
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        CMPPonderScenes.register(helper);
    }
}
