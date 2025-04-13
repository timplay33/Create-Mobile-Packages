package de.theidler.create_mobile_packages.entities.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.DroneEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class DroneEntityModel<T extends DroneEntity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(CreateMobilePackages.asResource("droneentitymodel"), "main");
	private final ModelPart drone;
	private final ModelPart blate4;
	private final ModelPart blate3;
	private final ModelPart blate2;
	private final ModelPart blate;

	public DroneEntityModel(ModelPart root) {
		this.drone = root.getChild("drone");
		this.blate4 = this.drone.getChild("blate4");
		this.blate3 = this.drone.getChild("blate3");
		this.blate2 = this.drone.getChild("blate2");
		this.blate = this.drone.getChild("blate");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition drone = partdefinition.addOrReplaceChild("drone", CubeListBuilder.create().texOffs(0, 27).addBox(-4.0F, -5.0F, -11.0F, 8.0F, 1.0F, 22.0F, new CubeDeformation(0.0F))
		.texOffs(0, 50).addBox(-4.0F, -6.0F, -11.0F, 8.0F, 1.0F, 20.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-4.0F, -4.0F, -11.0F, 8.0F, 4.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = drone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(56, 56).addBox(-22.0F, -1.0F, -1.0F, 24.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.0F, -2.0F, -15.0F, 0.0F, 0.6545F, 0.0F));

		PartDefinition cube_r2 = drone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(56, 52).addBox(-22.0F, -1.0F, -1.0F, 24.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -2.0F, 7.0F, 0.0F, 0.6545F, 0.0F));

		PartDefinition cube_r3 = drone.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(56, 54).addBox(-22.0F, -1.0F, -1.0F, 24.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.0F, -2.0F, 18.0F, 0.0F, -0.6545F, 0.0F));

		PartDefinition cube_r4 = drone.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(56, 50).addBox(-22.0F, -1.0F, -1.0F, 24.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -2.0F, -3.0F, 0.0F, -0.6545F, 0.0F));

		PartDefinition blate4 = drone.addOrReplaceChild("blate4", CubeListBuilder.create().texOffs(56, 62).addBox(-1.5F, 1.5F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(62, 0).addBox(0.5F, -0.5F, -1.5F, 13.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(66, 23).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(62, 4).addBox(-13.5F, -0.5F, -1.5F, 13.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(20.5F, -5.5F, 19.5F));

		PartDefinition blate3 = drone.addOrReplaceChild("blate3", CubeListBuilder.create().texOffs(62, 18).addBox(-1.5F, 1.5F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(60, 39).addBox(0.5F, -0.5F, -1.5F, 13.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(64, 47).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(60, 43).addBox(-13.5F, -0.5F, -1.5F, 13.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-20.5F, -5.5F, 19.5F));

		PartDefinition blate2 = drone.addOrReplaceChild("blate2", CubeListBuilder.create().texOffs(62, 13).addBox(-1.5F, 1.5F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(60, 31).addBox(0.5F, -0.5F, -1.5F, 13.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(62, 23).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(60, 35).addBox(-13.5F, -0.5F, -1.5F, 13.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(20.5F, -5.5F, -16.5F));

		PartDefinition blate = drone.addOrReplaceChild("blate", CubeListBuilder.create().texOffs(62, 8).addBox(-1.5F, 1.5F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(56, 58).addBox(0.5F, -0.5F, -1.5F, 13.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(60, 47).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(60, 27).addBox(-13.5F, -0.5F, -1.5F, 13.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-20.5F, -5.5F, -16.5F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(DroneEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		poseStack.pushPose();
		poseStack.scale(0.2F,0.2F,0.2F);
		poseStack.translate(0.0F, 4.0F, 0.0F);
		drone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.popPose();
	}
}