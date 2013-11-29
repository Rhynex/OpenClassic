package ch.spacebase.openclassic.client.level;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.client.util.BlockUtils;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.mob.Creeper;
import com.mojang.minecraft.entity.mob.Mob;
import com.mojang.minecraft.entity.mob.Pig;
import com.mojang.minecraft.entity.mob.Sheep;
import com.mojang.minecraft.entity.mob.Skeleton;
import com.mojang.minecraft.entity.mob.Spider;
import com.mojang.minecraft.entity.mob.Zombie;

public class MobSpawner {

	@SuppressWarnings("unchecked")
	private static final Class<? extends Mob> ANIMALS[] = new Class[] { Pig.class, Sheep.class };
	@SuppressWarnings("unchecked")
	private static final Class<? extends Mob> ALL[] = new Class[] { Zombie.class, Skeleton.class, Creeper.class, Spider.class, Pig.class, Sheep.class };

	public ClientLevel level;

	public MobSpawner(ClientLevel level) {
		this.level = level;
	}

	public int spawn(int max, Entity player, boolean progress) {
		if(progress) {
			OpenClassic.getClient().getProgressBar().setText("Spawning...");
		}
		
		int count = 0;
		for(int mob = 0; mob < max; mob++) {
			if(progress) {
				OpenClassic.getClient().getProgressBar().setProgress(mob * 100 / (max - 1));
				OpenClassic.getClient().getProgressBar().render();
			}

			Class<? extends Mob> spawnable[] = ANIMALS;
			if(OpenClassic.getClient().getSettings().getIntSetting("options.survival").getValue() > 1) {
				spawnable = ALL;
			}

			int type = this.level.getRandom().nextInt(spawnable.length);
			int rx = this.level.getRandom().nextInt(this.level.getWidth());
			int ry = (int) (Math.min(this.level.getRandom().nextFloat(), this.level.getRandom().nextFloat()) * this.level.getHeight());
			int rz = this.level.getRandom().nextInt(this.level.getDepth());
			BlockType block = this.level.getBlockTypeAt(rx, ry, rz);
			if(!BlockUtils.preventsRendering(this.level, rx, ry, rz) && !(block != null && block.isLiquid()) && (!this.level.isLit(rx, ry, rz) || this.level.getRandom().nextInt(5) == 0)) {
				for(int pass = 0; pass < 3; pass++) {
					int bx = rx;
					int by = ry;
					int bz = rz;
					for(int run = 0; run < 3; run++) {
						bx += this.level.getRandom().nextInt(6) - this.level.getRandom().nextInt(6);
						by += this.level.getRandom().nextInt(1) - this.level.getRandom().nextInt(1);
						bz += this.level.getRandom().nextInt(6) - this.level.getRandom().nextInt(6);
						if(bx >= 0 && bz >= 1 && by >= 0 && by < this.level.getHeight() - 2 && bx < this.level.getWidth() && bz < this.level.getDepth() && BlockUtils.preventsRendering(this.level, bx, by - 1, bz) && !BlockUtils.preventsRendering(this.level, bx, by, bz) && !BlockUtils.preventsRendering(this.level, bx, by + 1, bz)) {
							float sx = bx + 0.5f;
							float sy = by + 1;
							float sz = bz + 0.5f;
							if(player != null) {
								float dx = sx - player.x;
								float dy = sy - player.y;
								float dz = sz - player.z;
								if(dx * dx + dy * dy + dz * dz < 256) {
									continue;
								}
							} else {
								float dx = sx - this.level.getSpawn().getX();
								float dy = sy - this.level.getSpawn().getY();
								float dz = sz - this.level.getSpawn().getZ();
								if(dx * dx + dy * dy + dz * dz < 256) {
									continue;
								}
							}

							Mob spawning = null;
							try {
								spawning = spawnable[type].getConstructor(ClientLevel.class, float.class, float.class, float.class).newInstance(level, sx, sy, sz);
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
