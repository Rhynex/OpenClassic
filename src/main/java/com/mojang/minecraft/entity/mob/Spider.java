package com.mojang.minecraft.entity.mob;

import com.mojang.minecraft.entity.mob.ai.JumpAttackAI;
import com.mojang.minecraft.level.Level;

public class Spider extends QuadrupedMob {

	public Spider(Level level, float x, float y, float z) {
		super(level, x, y, z);
		this.heightOffset = 0.72F;
		this.modelName = "spider";
		this.textureName = "/mob/spider.png";
		this.setSize(1.4F, 0.9F);
		this.setPos(x, y, z);
		this.deathScore = 105;
		this.bobStrength = 0.0F;
		this.ai = new JumpAttackAI();
	}
}
