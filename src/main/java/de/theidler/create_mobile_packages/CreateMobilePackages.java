package de.theidler.create_mobile_packages;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import de.theidler.create_mobile_packages.index.CMPBlockEntities;
import de.theidler.create_mobile_packages.index.CMPBlocks;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.CMPCreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CreateMobilePackages.MODID)
public class CreateMobilePackages
{
    public static final String MODID = "create_mobile_packages";

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(CreateMobilePackages.MODID);

    public CreateMobilePackages(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);

        CMPCreativeModeTabs.register(modEventBus);
        REGISTRATE.registerEventListeners(modEventBus);
        CMPBlocks.register();
        CMPItems.register();
        CMPBlockEntities.register();
    }
}
