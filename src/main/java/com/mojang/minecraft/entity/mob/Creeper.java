package com.mojang.minecraft.entity.mob;

import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.render.GuiTextures;

import com.mojang.minecraft.entity.mob.ai.CreeperAI;

public class Creeper extends Mob {

	public Creeper(ClientLevel level, float x, float y, float z) {
		super(level, GuiTextures.CREEPER);
		this.heightOffset = 1.62F;
		this.modelName = "creeper";
		this.ai = new CreeperAI();
		this.ai.defaultLookAngle = 45;
		this.deathScore = 200;
		this.setPos(x, y, z);
	}

	public float getBrightness(float dt) {
		float brightness = (20 - this.health) / 20F;
		return ((MathHelper.sin(this.tickCount + dt) * 0.5F + 0.5F) * brightness * 0.5F + 0.25F + brightness * 0.25F) * super.getBrightness(dt);
	}
	
}
