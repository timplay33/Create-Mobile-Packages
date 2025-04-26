package de.theidler.create_mobile_packages.entities.render;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import de.theidler.create_mobile_packages.entities.models.RoboBeeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DroneEntityRenderer extends MobRenderer<RoboBeeEntity, RoboBeeModel<RoboBeeEntity>> {
    private static final ResourceLocation TEXTURE = CreateMobilePackages.asResource("textures/entity/robo_bee.png");

    public DroneEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new RoboBeeModel<>(pContext.bakeLayer(RoboBeeModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(RoboBeeEntity pEntity) {
        return TEXTURE;
    }

}
