package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.index.CMPBlocks;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.CMPCreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CreateMobilePackages.MODID)
public class CreateMobilePackages
{
    public static final String MODID = "create_mobile_packages";

    public CreateMobilePackages(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        CMPBlocks.BLOCKS.register(modEventBus);
        CMPItems.ITEMS.register(modEventBus);
        CMPCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }
}
