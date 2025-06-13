package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.entities.models.RoboBeeModel;
import de.theidler.create_mobile_packages.entities.render.RoboBeeEntityRenderer;
import de.theidler.create_mobile_packages.index.CMPEntities;
import de.theidler.create_mobile_packages.index.ponder.CMPPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateMobilePackagesClient {

    public CreateMobilePackagesClient() {
    }

    public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(CreateMobilePackagesClient::clientInit);
        modEventBus.addListener(CreateMobilePackagesClient::registerEntityRenderers);
        modEventBus.addListener(CreateMobilePackagesClient::registerLayerDefinitions);
    }

    private static void clientInit(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CMPPonderPlugin());
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CMPEntities.ROBO_BEE_ENTITY.get(), RoboBeeEntityRenderer::new);
    }
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RoboBeeModel.LAYER_LOCATION, RoboBeeModel::createBodyLayer);
    }
}
