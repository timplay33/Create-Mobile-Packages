package de.theidler.create_mobile_packages;

import com.simibubi.create.Create;
import de.theidler.create_mobile_packages.entities.models.RoboBeeModel;
import de.theidler.create_mobile_packages.entities.render.DroneEntityRenderer;
import de.theidler.create_mobile_packages.index.CMPEntities;
import de.theidler.create_mobile_packages.index.ponder.CMPPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = CreateMobilePackages.MODID, dist = Dist.CLIENT)
public class CreateMobilePackagesClient {

    public CreateMobilePackagesClient(IEventBus modEventBus) {
        onCtorClient(modEventBus);
    }

    public static void onCtorClient(IEventBus modEventBus) {
        modEventBus.addListener(CreateMobilePackagesClient::clientInit);
        modEventBus.addListener(CreateMobilePackagesClient::registerEntityRenderers);
        modEventBus.addListener(CreateMobilePackagesClient::registerLayerDefinitions);
    }

    private static void clientInit(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CMPPonderPlugin());
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CMPEntities.ROBO_BEE_ENTITY.get(), DroneEntityRenderer::new);
    }
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RoboBeeModel.LAYER_LOCATION, RoboBeeModel::createBodyLayer);
    }
}
