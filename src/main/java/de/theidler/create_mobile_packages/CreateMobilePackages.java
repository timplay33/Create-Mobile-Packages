package de.theidler.create_mobile_packages;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import de.theidler.create_mobile_packages.index.*;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CreateMobilePackages.MODID)
public class CreateMobilePackages
{
    public static final String MODID = "create_mobile_packages";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(CreateMobilePackages.MODID);

    public CreateMobilePackages() {
        onCtor();
    }

    public static void onCtor() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        REGISTRATE.registerEventListeners(modEventBus);

        CMPCreativeModeTabs.register(modEventBus);
        CMPBlocks.register();
        CMPItems.register();
        CMPBlockEntities.register();
        CMPMenuTypes.register();
        CMPPackets.registerPackets();
        CMPConfigs.register(modLoadingContext);
        CMPEntities.register();
        CMPDisplaySources.register();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> {
            return () -> {
                CreateMobilePackagesClient.onCtorClient(modEventBus, forgeEventBus);
            };
        });

    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MODID, path);
    }
}
