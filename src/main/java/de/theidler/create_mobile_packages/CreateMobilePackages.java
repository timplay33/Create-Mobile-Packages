package de.theidler.create_mobile_packages;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import de.theidler.create_mobile_packages.index.*;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CreateMobilePackages.MODID)
public class CreateMobilePackages
{
    public static final String MODID = "create_mobile_packages";
    public static final String NAME = "Create: Mobile Packages";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(CreateMobilePackages.MODID)
            .defaultCreativeTab("create_mobile_packages_tab",
                    t -> {
                        t.icon(() -> CMPItems.ROBO_BEE.get().getDefaultInstance());
                        t.title(Component.translatable("itemGroup.create_mobile_packages"));
                    }).build()
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );
    public static final RoboManager ROBO_MANAGER = new RoboManager();

    public CreateMobilePackages(FMLJavaModLoadingContext context) {
        onCtor(context);
    }

    public static void onCtor(FMLJavaModLoadingContext modLoadingContext) {
        IEventBus modEventBus = modLoadingContext.getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        REGISTRATE.registerEventListeners(modEventBus);

        CMPBlocks.register();
        CMPItems.register();
        CMPBlockEntities.register();
        CMPMenuTypes.register();
        CMPPackets.registerPackets();
        CMPConfigs.register(modLoadingContext);
        CMPEntities.register();
        CMPDisplaySources.register();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateMobilePackagesClient.onCtorClient(modEventBus, forgeEventBus));

    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
