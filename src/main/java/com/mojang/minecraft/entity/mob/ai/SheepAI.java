package com.mojang.minecraft.entity.mob.ai;

import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.math.MathHelper;

import com.mojang.minecraft.entity.mob.Sheep;

public class SheepAI extends BasicAI {

	private Sheep parent;

	public SheepAI(Sheep parent) {
		this.parent = parent;
	}

	public void update() {
		float xDiff = -0.7F * MathHelper.sin(this.parent.yaw * MathHelper.DEG_TO_RAD);
		float zDiff = 0.7F * MathHelper.cos(this.parent.yaw * MathHelper.DEG_TO_RAD);
		int x = (int) (this.mob.x + xDiff);
		int y = (int) (this.mob.y - 2);
		int z = (int) (this.mob.z + zDiff);
		if(this.parent.grazing) {
			if(this.level.getTile(x, y, z) != VanillaBlock.GRASS.getId()) {
				this.parent.grazing = false;
			} else {
				if(this.parent.grazingTime++ == 60) {
					this.level.setTile(x, y, z, VanillaBlock.DIRT.getId());
					if(this.random.nextInt(5) == 0) {
						this.parent.hasFur = true;
					}
				}

				this.xxa = 0;
				this.yya = 0;
				this.mob.pitch = 40 + this.parent.grazingTime / 2 % 2 * 10;
			}
		} else {
			if(this.level.getTile(x, y, z) == VanillaBlock.GRASS.getId()) {
				this.parent.grazing = true;
				this.parent.grazingTime = 0;
			}

			super.update();
		}
	}
	
}
