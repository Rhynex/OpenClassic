package com.mojang.minecraft.entity.mob;

import ch.spacebase.openclassic.client.level.ClientLevel;

import com.mojang.minecraft.entity.mob.ai.JumpAttackAI;

public class Spider extends QuadrupedMob {

	public Spider(ClientLevel level, float x, float y, float z) {
		super(level, x, y, z);
		this.heightOffset = 0.72F;
		this.modelName = "spider";
		this.textureName = "/textures/entity/mob/spider.png";
		this.setSize(1.4F, 0.9F);
		this.setPos(x, y, z);
		this.deathScore = 105;
		this.bobStrength = 0;
		this.ai = new JumpAttackAI();
	}
	
}
