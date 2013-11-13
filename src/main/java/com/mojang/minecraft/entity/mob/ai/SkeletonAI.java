package com.mojang.minecraft.entity.mob.ai;

import com.mojang.minecraft.entity.item.Arrow;
import com.mojang.minecraft.entity.mob.Mob;
import com.mojang.minecraft.entity.mob.Skeleton;
import com.mojang.minecraft.level.Level;

public class SkeletonAI extends BasicAttackAI {

	private Skeleton parent;

	public SkeletonAI(Skeleton parent) {
		this.parent = parent;
	}

	public void tick(Level level, Mob mob) {
		super.tick(level, mob);
		if(mob.health > 0 && this.random.nextInt(30) == 0 && this.attackTarget != null) {
			this.parent.shootArrow(level);
		}
	}

	public void beforeRemove() {
		int arrows = (int) ((Math.random() + Math.random()) * 3 + 4);

		for(int count = 0; count < arrows; count++) {
			this.parent.level.addEntity(new Arrow(this.parent.level, this.parent.level.minecraft.player, this.parent.x, this.parent.y - 0.2F, this.parent.z, (float) Math.random() * 360, -((float) Math.random()) * 60, 0.4F));
		}
	}
	
}
