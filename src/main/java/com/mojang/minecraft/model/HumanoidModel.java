package com.mojang.minecraft.model;

import ch.spacebase.openclassic.api.math.MathHelper;

public class HumanoidModel extends Model {

	public ModelPart head;
	public ModelPart hair;
	public ModelPart body;
	public ModelPart rightArm;
	public ModelPart leftArm;
	public ModelPart rightLeg;
	public ModelPart leftLeg;

	public HumanoidModel() {
		this(0);
	}

	public HumanoidModel(float offset) {
		this.head = new ModelPart(0, 0);
		this.head.setBounds(-4.0F, -8.0F, -4.0F, 8, 8, 8, offset);
		this.hair = new ModelPart(32, 0);
		this.hair.setBounds(-4.0F, -8.0F, -4.0F, 8, 8, 8, offset + 0.5F);
		this.body = new ModelPart(16, 16);
		this.body.setBounds(-4.0F, 0.0F, -2.0F, 8, 12, 4, offset);
		this.rightArm = new ModelPart(40, 16);
		this.rightArm.setBounds(-3.0F, -2.0F, -2.0F, 4, 12, 4, offset);
		this.rightArm.setPosition(-5.0F, 2.0F, 0.0F);
		this.leftArm = new ModelPart(40, 16);
		this.leftArm.mirror = true;
		this.leftArm.setBounds(-1.0F, -2.0F, -2.0F, 4, 12, 4, offset);
		this.leftArm.setPosition(5.0F, 2.0F, 0.0F);
		this.rightLeg = new ModelPart(0, 16);
		this.rightLeg.setBounds(-2.0F, 0.0F, -2.0F, 4, 12, 4, offset);
		this.rightLeg.setPosition(-2.0F, 12.0F, 0.0F);
		this.leftLeg = new ModelPart(0, 16);
		this.leftLeg.mirror = true;
		this.leftLeg.setBounds(-2.0F, 0.0F, -2.0F, 4, 12, 4, offset);
		this.leftLeg.setPosition(2.0F, 12.0F, 0.0F);
	}

	public final void render(float animStep, float runProgress, float dt, float yaw, float pitch, float scale) {
		this.setRotationAngles(animStep, runProgress, dt, yaw, pitch, scale);
		this.head.render(scale);
		this.body.render(scale);
		this.rightArm.render(scale);
		this.leftArm.render(scale);
		this.rightLeg.render(scale);
		this.leftLeg.render(scale);
	}

	public void setRotationAngles(float animStep, float runProgress, float dt, float yaw, float pitch, float scale) {
		this.head.yaw = yaw / 57.295776F;
		this.head.pitch = pitch / 57.295776F;
		this.rightArm.pitch = MathHelper.cos(animStep * 0.6662F + MathHelper.PI) * 2.0F * runProgress;
		this.rightArm.roll = (MathHelper.cos(animStep * 0.2312F) + 1.0F) * runProgress;
		this.leftArm.pitch = MathHelper.cos(animStep * 0.6662F) * 2.0F * runProgress;
		this.leftArm.roll = (MathHelper.cos(animStep * 0.2812F) - 1.0F) * runProgress;
		this.rightLeg.pitch = MathHelper.cos(animStep * 0.6662F) * 1.4F * runProgress;
		this.leftLeg.pitch = MathHelper.cos(animStep * 0.6662F + MathHelper.PI) * 1.4F * runProgress;
		this.rightArm.roll += MathHelper.cos(dt * 0.09F) * 0.05F + 0.05F;
		this.leftArm.roll -= MathHelper.cos(dt * 0.09F) * 0.05F + 0.05F;
		this.rightArm.pitch += MathHelper.sin(dt * 0.067F) * 0.05F;
		this.leftArm.pitch -= MathHelper.sin(dt * 0.067F) * 0.05F;
	}
}
