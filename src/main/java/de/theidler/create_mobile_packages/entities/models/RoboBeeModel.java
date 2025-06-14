package de.theidler.create_mobile_packages.entities.models;// Made with Blockbench 4.12.4

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.robo_bee_entity.RoboBeeEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

//Thanks to @AzulConspirator & @SNRTom for the model
public class RoboBeeModel<T extends RoboBeeEntity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(CreateMobilePackages.asResource("robobeemodel"), "main");
	private final ModelPart stinger;
	private final ModelPart wing1;
	private final ModelPart rotor1;
	private final ModelPart wing2;
	private final ModelPart rotor2;
	private final ModelPart bb_main;

	public RoboBeeModel(ModelPart root) {
		this.stinger = root.getChild("stinger");
		this.wing1 = root.getChild("wing1");
		this.rotor1 = this.wing1.getChild("rotor1");
		this.wing2 = root.getChild("wing2");
		this.rotor2 = this.wing2.getChild("rotor2");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition stinger = partdefinition.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(30, 7).addBox(0.0F, 0.0F, 4.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 18.0F, 1.0F));

		PartDefinition wing1 = partdefinition.addOrReplaceChild("wing1", CubeListBuilder.create().texOffs(37, 3).addBox(2.0F, -1.0F, -2.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(37, 0).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(37, 3).addBox(-3.0F, -1.0F, -2.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(37, 0).addBox(-3.0F, -1.0F, 2.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6F, 14.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

		PartDefinition rotor1 = wing1.addOrReplaceChild("rotor1", CubeListBuilder.create().texOffs(-4, 18).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition wing2 = partdefinition.addOrReplaceChild("wing2", CubeListBuilder.create().texOffs(37, 3).addBox(2.0F, -1.0F, -2.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(37, 0).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(37, 3).addBox(-3.0F, -1.0F, -2.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(37, 0).addBox(-3.0F, -1.0F, 2.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 14.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

		PartDefinition rotor2 = wing2.addOrReplaceChild("rotor2", CubeListBuilder.create().texOffs(-4, 18).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -9.0F, -5.0F, 7.0F, 7.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(2, 3).addBox(-2.0F, -9.0F, -8.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(2, 0).addBox(2.0F, -9.0F, -8.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
		float rotationSpeed = 20.0F;
		float rotation = pAgeInTicks * rotationSpeed;

		this.rotor1.yRot = rotation;
		this.rotor2.yRot = -rotation;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		poseStack.pushPose();
		poseStack.scale(0.8F,0.8F,0.8F);
		poseStack.translate(0.0F, 0.4F, 0.0F);
		stinger.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		wing1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		wing2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.popPose();
	}
}