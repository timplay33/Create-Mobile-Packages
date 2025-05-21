package de.theidler.create_mobile_packages;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = CreateMobilePackages.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null)
            return;

        PoseStack poseStack = event.getPoseStack();
        float partialTicks = event.getPartialTick();

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        Frustum frustum = new Frustum(poseStack.last().pose(),
                event.getProjectionMatrix());
        frustum.prepare(cameraPos.x, cameraPos.y, cameraPos.z);

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        for (SimpleRobo robo : CreateMobilePackages.ROBO_MANAGER.robos.values()) {
            SimpleRoboRenderer.renderRobo(robo, partialTicks, poseStack, buffer, frustum);
        }

        buffer.endBatch();
        poseStack.popPose();
    }
}
