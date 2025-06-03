package de.theidler.create_mobile_packages.entities.render;

import com.simibubi.create.content.logistics.box.PackageStyles;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import de.theidler.create_mobile_packages.entities.models.RoboBeeModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
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

        PackageStyles.PackageStyle style = getStyleFromStack(stack);
        if (style != null) {
            ResourceLocation riggingModel = style.getRiggingModel();
            float riggingOffset = style.riggingOffset();
            float heightScale = entity.getPackageHeightScale();

            poseStack.pushPose();
            poseStack.translate(-0.5D, 0 - (riggingOffset - 5) / 16 * heightScale, -0.5D);
            poseStack.scale(1F, heightScale * 1F, 1F);
            var modelManager = Minecraft.getInstance().getModelManager();
            var bakedModel = modelManager.getModel(ModelResourceLocation.standalone(riggingModel));
            Minecraft.getInstance().getItemRenderer().renderModelLists(
                bakedModel,
                ItemStack.EMPTY,
                packedLight,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer.getBuffer(ItemBlockRenderTypes.getRenderType(ItemStack.EMPTY, true))
            );
            poseStack.popPose();

            if (!stack.isEmpty() && PackageItem.isPackage(stack)) {
                poseStack.pushPose();
                poseStack.translate(0.0D, 0-(riggingOffset-1) / 16 * heightScale, 0.0D);
                poseStack.scale(4F, heightScale * 4F, 4F);
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

    public static PackageStyles.PackageStyle getStyleFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (PackageStyles.PackageStyle style : PackageStyles.STYLES) {
            if (style.getItemId().equals(itemId)) {
                return style;
            }
        }
        return null;
    }
}
