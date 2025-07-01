package de.theidler.create_mobile_packages.entities.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import de.theidler.create_mobile_packages.entities.models.RoboBeeModel;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

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
    public void render(RoboBeeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        ItemStack stack = entity.getItemStack();

        if (!PackageItem.isPackage(stack))
            return;

        ResourceLocation modelKey = BuiltInRegistries.ITEM.getKey(stack.getItem());

        float riggingOffset = PackageItem.getHookDistance(stack);
        float heightScale = entity.getPackageHeightScale();

        poseStack.pushPose();
        poseStack.translate(-0.5D, 0 - (riggingOffset - 5 / 16f) * heightScale, -0.5D);
        poseStack.scale(1F, heightScale * 1F, 1F);

        PartialModel partialModel = AllPartialModels.PACKAGE_RIGGING.get(modelKey);
        if (partialModel != null) {
            BakedModel rig = partialModel.get();


            Minecraft.getInstance().getItemRenderer().renderModelLists(
                    rig,
                    ItemStack.EMPTY,
                    packedLight,
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer.getBuffer(ItemBlockRenderTypes.getRenderType(ItemStack.EMPTY, true))
            );
            poseStack.popPose();
        }

        if (!stack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0 - (riggingOffset - 1 + 3 / 16f) * heightScale, 0.0D);
            poseStack.scale(2F, heightScale * 2F, 2F);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
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
