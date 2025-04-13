package de.theidler.create_mobile_packages.entities.render;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.DroneEntity;
import de.theidler.create_mobile_packages.entities.models.DroneEntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DroneEntityRenderer extends MobRenderer<DroneEntity, DroneEntityModel<DroneEntity>> {
    private static final ResourceLocation TEXTURE = CreateMobilePackages.asResource("textures/entity/drone.png");

    public DroneEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new DroneEntityModel<>(pContext.bakeLayer(DroneEntityModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(DroneEntity pEntity) {
        return TEXTURE;
    }

}
