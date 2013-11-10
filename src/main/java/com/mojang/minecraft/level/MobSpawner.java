package com.mojang.minecraft.level;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.client.ClientProgressBar;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.mob.Creeper;
import com.mojang.minecraft.entity.mob.Mob;
import com.mojang.minecraft.entity.mob.Pig;
import com.mojang.minecraft.entity.mob.Sheep;
import com.mojang.minecraft.entity.mob.Skeleton;
import com.mojang.minecraft.entity.mob.Spider;
import com.mojang.minecraft.entity.mob.Zombie;

public final class MobSpawner {

	@SuppressWarnings("unchecked")
	private static final Class<? extends Mob> ANIMALS[] = new Class[] { Pig.class, Sheep.class };
	@SuppressWarnings("unchecked")
	private static final Class<? extends Mob> ALL[] = new Class[] { Zombie.class, Skeleton.class, Creeper.class, Spider.class, Pig.class, Sheep.class };

	public Level level;

	public MobSpawner(Level level) {
		this.level = level;
	}

	public final int spawn(int max, Entity player, ClientProgressBar progress) {
		int count = 0;
		for(int mob = 0; mob < max; mob++) {
			if(progress != null) {
				progress.setProgress(mob * 100 / (max - 1));
				progress.render();
			}

			Class<? extends Mob> spawnable[] = ANIMALS;
			if(OpenClassic.getClient().getSettings().getIntSetting("options.survival").getValue() > 1) {
				spawnable = ALL;
			}

			int type = this.level.random.nextInt(spawnable.length);
			int rx = this.level.random.nextInt(this.level.width);
			int ry = (int) (Math.min(this.level.random.nextFloat(), this.level.random.nextFloat()) * this.level.height);
			int rz = this.level.random.nextInt(this.level.depth);
			if(!this.level.getPreventsRendering(rx, ry, rz) && !(Blocks.fromId(this.level.getTile(rx, ry, rz)) != null && Blocks.fromId(this.level.getTile(rx, ry, rz)).isLiquid()) && (!this.level.isLit(rx, ry, rz) || this.level.random.nextInt(5) == 0)) {
				for(int pass = 0; pass < 3; pass++) {
					int bx = rx;
					int by = ry;
					int bz = rz;
					for(int run = 0; run < 3; run++) {
						bx += this.level.random.nextInt(6) - this.level.random.nextInt(6);
						by += this.level.random.nextInt(1) - this.level.random.nextInt(1);
						bz += this.level.random.nextInt(6) - this.level.random.nextInt(6);
						if(bx >= 0 && bz >= 1 && by >= 0 && by < this.level.height - 2 && bx < this.level.width && bz < this.level.depth && this.level.getPreventsRendering(bx, by - 1, bz) && !this.level.getPreventsRendering(bx, by, bz) && !this.level.getPreventsRendering(bx, by + 1, bz)) {
							float sx = bx + 0.5F;
							float sy = by + 1.0F;
							float sz = bz + 0.5F;
							if(player != null) {
								float dx = sx - player.x;
								float dy = sy - player.y;
								float dz = sz - player.z;
								if(dx * dx + dy * dy + dz * dz < 256.0F) {
									continue;
								}
							} else {
								float dx = sx - this.level.xSpawn;
								float dy = sy - this.level.ySpawn;
								float dz = sz - this.level.zSpawn;
								if(dx * dx + dy * dy + dz * dz < 256.0F) {
									continue;
								}
							}

							Mob spawning = null;
							try {
								spawning = spawnable[type].getConstructor(Level.class, float.class, float.class, float.class).newInstance(level, sx, sy, sz);
							} catch(Exception e) {
								OpenClassic.getLogger().severe("Failed to spawn mob \"" + spawnable[type].getSimpleName() + "\"");
								e.printStackTrace();
							}

							if(spawning != null && this.level.isFree(spawning.bb)) {
								count++;
								this.level.addEntity(spawning);
							}
						}
					}
				}
			}
		}

		return count;
	}
}
