package com.mojang.minecraft.entity.mob.ai;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.mob.Mob;
import com.mojang.minecraft.level.Level;

public abstract class AI {

	public int defaultLookAngle = 0;

	public abstract void tick(Level level, Mob mob);

	public abstract void beforeRemove();

	public abstract void hurt(Entity cause, int damage);
}
