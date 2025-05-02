package de.theidler.create_mobile_packages.compat;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class CMPJEI implements IModPlugin{
    private static final ResourceLocation ID = CreateMobilePackages.asResource("jei_plugin");

    public static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }
}
