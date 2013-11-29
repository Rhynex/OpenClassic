package com.mojang.minecraft.entity.mob;

import ch.spacebase.openclassic.client.level.ClientLevel;

import com.mojang.minecraft.entity.mob.ai.BasicAttackAI;

public class Zombie extends HumanoidMob {

	public Zombie(ClientLevel level, float x, float y, float z) {
		super(level, x, y, z);
		this.modelName = "zombie";
		this.textureName = "/textures/entity/mob/zombie.png";
		this.heightOffset = 1.62F;
		this.deathScore = 80;
		this.ai = new BasicAttackAI();
		this.ai.defaultLookAngle = 30;
		((BasicAttackAI) this.ai).runSpeed = 1;
	}
}
