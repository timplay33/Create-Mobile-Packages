package de.theidler.create_mobile_packages.entities.render;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import de.theidler.create_mobile_packages.entities.models.RoboBeeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;

public class DroneEntityRenderer extends MobRenderer<RoboBeeEntity, RoboBeeModel<RoboBeeEntity>> {
    private static final ResourceLocation TEXTURE = CreateMobilePackages.asResource("textures/entity/robo_bee.png");

    public DroneEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new RoboBeeModel<>(pContext.bakeLayer(RoboBeeModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(RoboBeeEntity pEntity) {
        return TEXTURE;
    }

    @Override
    public void render(RoboBeeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        ItemStack stack = entity.getItemStack();
        if (stack != null && !stack.isEmpty() && PackageItem.isPackage(stack)) {
            poseStack.pushPose();
            poseStack.translate(0.0D, -1D, 0.0D);
            poseStack.scale(5F, 5F, 5F);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                net.minecraft.world.item.ItemDisplayContext.GROUND,
                packedLight,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
            );
            poseStack.popPose();
        }
    }
}
