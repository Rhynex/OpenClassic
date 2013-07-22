package com.mojang.minecraft.entity.mob;

import com.mojang.minecraft.level.Level;

public class QuadrupedMob extends Mob {

	public QuadrupedMob(Level level, float x, float y, float z) {
		super(level);
		this.setSize(1.4F, 1.2F);
		this.setPos(x, y, z);
		this.modelName = "pig";
	}
}
