package com.mojang.minecraft.entity.mob;

import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.math.MathHelper;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.mob.ai.BasicAttackAI;
import com.mojang.minecraft.entity.particle.TerrainParticle;
import com.mojang.minecraft.level.Level;

public class Creeper extends Mob {

	public Creeper(Level level, float x, float y, float z) {
		super(level);
		this.heightOffset = 1.62F;
		this.modelName = "creeper";
		this.textureName = "/mob/creeper.png";
		this.ai = new CreeperAI();
		this.ai.defaultLookAngle = 45;
		this.deathScore = 200;
		this.setPos(x, y, z);
	}

	public float getBrightness(float dt) {
		float brightness = (20 - this.health) / 20.0F;
		return ((MathHelper.sin(this.tickCount + dt) * 0.5F + 0.5F) * brightness * 0.5F + 0.25F + brightness * 0.25F) * super.getBrightness(dt);
	}

	public static class CreeperAI extends BasicAttackAI {

		public final boolean attack(Entity entity) {
			if(super.attack(entity)) {
				this.mob.hurt(entity, 6);
				return true;
			}

			return false;
		}

		public final void beforeRemove() {
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
}
