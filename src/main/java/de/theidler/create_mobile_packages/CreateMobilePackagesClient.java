package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.entities.models.DroneEntityModel;
import de.theidler.create_mobile_packages.entities.render.DroneEntityRenderer;
import de.theidler.create_mobile_packages.index.CMPEntities;
import de.theidler.create_mobile_packages.index.ponder.CMPPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class CreateMobilePackagesClient {
    public static void registerClientEvents(IEventBus modEventBus) {
        registerPonder();
        modEventBus.addListener(CreateMobilePackagesClient::registerEntityRenderers);
        modEventBus.addListener(CreateMobilePackagesClient::registerLayerDefinitions);
    }

    private static void registerPonder() {
        CreateMobilePackages.LOGGER.info("Registering PonderIndex");
        PonderIndex.addPlugin(new CMPPonderPlugin());
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CMPEntities.DRONE_ENTITY.get(), DroneEntityRenderer::new);
    }
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DroneEntityRenderer.LAYER_LOCATION, DroneEntityModel::createBodyLayer);
    }


}
