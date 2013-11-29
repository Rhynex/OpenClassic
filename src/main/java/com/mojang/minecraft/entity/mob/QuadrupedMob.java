package com.mojang.minecraft.entity.mob;

import ch.spacebase.openclassic.client.level.ClientLevel;

public class QuadrupedMob extends Mob {

	public QuadrupedMob(ClientLevel level, float x, float y, float z) {
		super(level);
		this.setSize(1.4F, 1.2F);
		this.setPos(x, y, z);
		this.modelName = "pig";
	}
	
}
