package com.mojang.minecraft.entity.mob;

import com.mojang.minecraft.entity.item.Arrow;
import com.mojang.minecraft.entity.mob.ai.BasicAttackAI;
import com.mojang.minecraft.entity.mob.ai.SkeletonAI;
import com.mojang.minecraft.level.Level;

public class Skeleton extends Zombie {

	public Skeleton(Level level, float x, float y, float z) {
		super(level, x, y, z);
		this.modelName = "skeleton";
		this.textureName = "/mob/skeleton.png";
		this.deathScore = 120;
		BasicAttackAI ai = new SkeletonAI(this);
		ai.runSpeed = 0.3F;
		ai.damage = 8;
		this.ai = ai;
	}

	public void shootArrow(Level level) {
		level.addEntity(new Arrow(level, this, this.x, this.y, this.z, this.yaw + 180 + (float) (Math.random() * 45 - 22.5), this.pitch - (float) (Math.random() * 45 - 10), 1));
	}

}
