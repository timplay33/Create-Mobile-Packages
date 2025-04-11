package de.theidler.create_mobile_packages;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import de.theidler.create_mobile_packages.index.*;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

@Mod(CreateMobilePackages.MODID)
public class CreateMobilePackages
{
    public static final String MODID = "create_mobile_packages";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(CreateMobilePackages.MODID);

    public CreateMobilePackages(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Registering CMPCreativeModeTabs");
        CMPCreativeModeTabs.register(modEventBus);
        LOGGER.info("Registering REGISTRATE");
        REGISTRATE.registerEventListeners(modEventBus);
        LOGGER.info("Registering CMPBlocks");
        CMPBlocks.register();
        LOGGER.info("Registering CMPItems");
        CMPItems.register();
        LOGGER.info("Registering CMPBlockEntities");
        CMPBlockEntities.register();
        LOGGER.info("Registering CMPMenuTypes");
        CMPMenuTypes.register();
        LOGGER.info("Registering CMPPackets");
        CMPPackets.registerPackets();
        LOGGER.info("Registering CMPConfigs");
        CMPConfigs.register(context);
        LOGGER.info("Registering CMPEntities");
        CMPEntities.register();

        if (FMLEnvironment.dist.isClient()) {
            CreateMobilePackagesClient.registerClientEvents(modEventBus);
        }
    }

    public static ResourceLocation asResource(String path) {
        return fromNamespaceAndPath(MODID, path);
    }
}
