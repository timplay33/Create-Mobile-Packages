package de.theidler.create_mobile_packages;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import de.theidler.create_mobile_packages.index.*;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(CreateMobilePackages.MODID)
public class CreateMobilePackages
{
    public static final String MODID = "create_mobile_packages";
    public static final String NAME = "Create: Mobile Packages";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(CreateMobilePackages.MODID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
            .setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                    .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> CMP_FREQ =
            DATA_COMPONENT_TYPES.register("cmp_freq", () -> DataComponentType.<CustomData>builder()
                    .persistent(CustomData.CODEC)
                    .networkSynchronized(CustomData.STREAM_CODEC)
                    .build());
    public static final RoboManager ROBO_MANAGER = new RoboManager();

    public CreateMobilePackages(IEventBus eventBus, ModContainer modContainer) {
        onCtor(eventBus, modContainer);
    }

    private void onCtor(IEventBus modEventBus, ModContainer modContainer) {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        REGISTRATE.registerEventListeners(modEventBus);

        DATA_COMPONENT_TYPES.register(modEventBus);
        CMPCreativeModeTabs.register(modEventBus);
        CMPBlocks.register();
        CMPItems.register();
        CMPBlockEntities.register();
        CMPMenuTypes.register();
        CMPPackets.register();
        CMPConfigs.register(modLoadingContext, modContainer);
        CMPEntities.register();
        CMPDisplaySources.register();
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
