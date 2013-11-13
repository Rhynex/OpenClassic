package com.mojang.minecraft.entity.mob.ai;

import ch.spacebase.openclassic.api.block.VanillaBlock;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.particle.TerrainParticle;

public class CreeperAI extends BasicAttackAI {

	public boolean attack(Entity entity) {
		if(super.attack(entity)) {
			this.mob.hurt(entity, 6);
			return true;
		}

		return false;
	}

	public void beforeRemove() {
		this.level.explode(this.mob, this.mob.x, this.mob.y, this.mob.z, 4);
		for(int count = 0; count < 500; count++) {
			float particleX = (float) this.random.nextGaussian();
			float particleY = (float) this.random.nextGaussian();
			float particleZ = (float) this.random.nextGaussian();
			float len = (float) Math.sqrt(particleX * particleX + particleY * particleY + particleZ * particleZ);
			float xd = particleX / len / len;
			float yd = particleY / len / len;
			float zd = particleZ / len / len;
			this.level.minecraft.particleManager.spawnParticle(new TerrainParticle(this.level, this.mob.x + particleX, this.mob.y + particleY, this.mob.z + particleZ, xd, yd, zd, VanillaBlock.LEAVES));
		}
	}
	
}
