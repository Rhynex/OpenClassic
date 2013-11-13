package com.mojang.minecraft.entity.mob;

import ch.spacebase.openclassic.api.math.MathHelper;

import com.mojang.minecraft.entity.mob.ai.CreeperAI;
import com.mojang.minecraft.level.Level;

public class Creeper extends Mob {

	public Creeper(Level level, float x, float y, float z) {
		super(level);
		this.heightOffset = 1.62F;
		this.modelName = "creeper";
		this.textureName = "/mob/creeper.png";
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
