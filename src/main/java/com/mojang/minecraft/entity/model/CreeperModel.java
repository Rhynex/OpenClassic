package com.mojang.minecraft.entity.model;

import ch.spacebase.openclassic.api.math.MathHelper;

public final class CreeperModel extends Model {

	private ModelPart head = new ModelPart(0, 0);
	private ModelPart body;
	private ModelPart leg1;
	private ModelPart leg2;
	private ModelPart leg3;
	private ModelPart leg4;

	public CreeperModel() {
		this.head.setBounds(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
		this.body = new ModelPart(16, 16);
		this.body.setBounds(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
		this.leg1 = new ModelPart(0, 16);
		this.leg1.setBounds(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
		this.leg1.setPosition(-2.0F, 12.0F, 4.0F);
		this.leg2 = new ModelPart(0, 16);
		this.leg2.setBounds(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
		this.leg2.setPosition(2.0F, 12.0F, 4.0F);
		this.leg3 = new ModelPart(0, 16);
		this.leg3.setBounds(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
		this.leg3.setPosition(-2.0F, 12.0F, -4.0F);
		this.leg4 = new ModelPart(0, 16);
		this.leg4.setBounds(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
		this.leg4.setPosition(2.0F, 12.0F, -4.0F);
	}

	public final void render(float animStep, float runProgress, float dt, float yaw, float pitch, float scale) {
		this.head.yaw = yaw / 57.295776F;
		this.head.pitch = pitch / 57.295776F;
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
