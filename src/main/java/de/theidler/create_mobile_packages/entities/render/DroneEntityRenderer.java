package de.theidler.create_mobile_packages.entities.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.DroneEntity;
import de.theidler.create_mobile_packages.entities.models.DroneEntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DroneEntityRenderer extends MobRenderer<DroneEntity, DroneEntityModel> {
    private static final ResourceLocation TEXTURE = CreateMobilePackages.asResource("textures/item/drone_controller.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(CreateMobilePackages.asResource("drone"), "main");

    public DroneEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new DroneEntityModel(pContext.bakeLayer(LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(DroneEntity pEntity) {
        return TEXTURE;
    }

    @Override
    public void render(DroneEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
    }
}
