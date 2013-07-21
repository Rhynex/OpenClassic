package com.mojang.minecraft.model;

import ch.spacebase.openclassic.api.math.MathHelper;

public class AnimalModel extends Model {

	public ModelPart head = new ModelPart(0, 0);
	public ModelPart body;
	public ModelPart leg1;
	public ModelPart leg2;
	public ModelPart leg3;
	public ModelPart leg4;

	public AnimalModel(int baseY) {
		this.head.setBounds(-4.0F, -4.0F, -8.0F, 8, 8, 8, 0.0F);
		this.head.setPosition(0.0F, (18 - baseY), -6.0F);
		this.body = new ModelPart(28, 8);
		this.body.setBounds(-5.0F, -10.0F, -7.0F, 10, 16, 8, 0.0F);
		this.body.setPosition(0.0F, (17 - baseY), 2.0F);
		this.leg1 = new ModelPart(0, 16);
		this.leg1.setBounds(-2.0F, 0.0F, -2.0F, 4, baseY, 4, 0.0F);
		this.leg1.setPosition(-3.0F, (24 - baseY), 7.0F);
		this.leg2 = new ModelPart(0, 16);
		this.leg2.setBounds(-2.0F, 0.0F, -2.0F, 4, baseY, 4, 0.0F);
		this.leg2.setPosition(3.0F, (24 - baseY), 7.0F);
		this.leg3 = new ModelPart(0, 16);
		this.leg3.setBounds(-2.0F, 0.0F, -2.0F, 4, baseY, 4, 0.0F);
		this.leg3.setPosition(-3.0F, (24 - baseY), -5.0F);
		this.leg4 = new ModelPart(0, 16);
		this.leg4.setBounds(-2.0F, 0.0F, -2.0F, 4, baseY, 4, 0.0F);
		this.leg4.setPosition(3.0F, (24 - baseY), -5.0F);
	}

	public final void render(float animStep, float runProgress, float dt, float yaw, float pitch, float scale) {
		this.head.yaw = yaw / 57.295776F;
		this.head.pitch = pitch / 57.295776F;
		this.body.pitch = MathHelper.HALF_PI;
		this.leg1.pitch = MathHelper.cos(animStep * 0.6662F) * 1.4F * runProgress;
		this.leg2.pitch = MathHelper.cos(animStep * 0.6662F + MathHelper.PI) * 1.4F * runProgress;
		this.leg3.pitch = MathHelper.cos(animStep * 0.6662F + MathHelper.PI) * 1.4F * runProgress;
		this.leg4.pitch = MathHelper.cos(animStep * 0.6662F) * 1.4F * runProgress;
		this.head.render(scale);
		this.body.render(scale);
		this.leg1.render(scale);
		this.leg2.render(scale);
		this.leg3.render(scale);
		this.leg4.render(scale);
	}
}
