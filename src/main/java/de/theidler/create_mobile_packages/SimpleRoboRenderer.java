package de.theidler.create_mobile_packages;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class SimpleRoboRenderer {
    public static void renderRobo(SimpleRobo robo, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, Frustum frustum) {
        if (robo.getLevel().dimension() != Minecraft.getInstance().level.dimension()) return;

        Vec3 pos = robo.getPosition();
        Vec3 oldPos = robo.oldPosition != null ? robo.oldPosition : pos;

        double lerpX = Mth.lerp(partialTicks, oldPos.x, pos.x);
        double lerpY = Mth.lerp(partialTicks, oldPos.y, pos.y);
        double lerpZ = Mth.lerp(partialTicks, oldPos.z, pos.z);

        Vec3 renderPos = new Vec3(lerpX, lerpY, lerpZ);

        AABB boundingBox = new AABB(renderPos.add(-0.5, -0.5, -0.5), renderPos.add(0.5, 0.5, 0.5));
        if (!frustum.isVisible(boundingBox)) return;

        poseStack.pushPose();
        poseStack.translate(lerpX, lerpY, lerpZ);

        renderSimpleRobo(robo, poseStack, buffer);

        poseStack.popPose();
    }

    private static void renderSimpleRobo(SimpleRobo robo, PoseStack poseStack, MultiBufferSource buffer) {
        float size = 0.25f;
        Color color = new Color(0x3366FF);
        float r = color.getRed()/255f;
        float g = color.getGreen()/255f;
        float b = color.getBlue()/255f;
        float a = 1.0f;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, vertexConsumer,
                -size, -size, -size,
                size, size, size,
                r, g, b, a);
    }
}
