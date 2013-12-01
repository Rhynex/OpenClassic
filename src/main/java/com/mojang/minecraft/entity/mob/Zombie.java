package com.mojang.minecraft.entity.mob;

import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.render.GuiTextures;

import com.mojang.minecraft.entity.mob.ai.BasicAttackAI;

public class Zombie extends HumanoidMob {

	public Zombie(ClientLevel level, float x, float y, float z) {
		this(level, x, y, z, "/textures/entity/mob/zombie.png");
	}
	
	public Zombie(ClientLevel level, float x, float y, float z, String texture) {
		super(level, x, y, z, GuiTextures.ZOMBIE);
		this.modelName = "zombie";
		this.heightOffset = 1.62F;
		this.deathScore = 80;
		this.ai = new BasicAttackAI();
		this.ai.defaultLookAngle = 30;
		((BasicAttackAI) this.ai).runSpeed = 1;
	}
	
}
