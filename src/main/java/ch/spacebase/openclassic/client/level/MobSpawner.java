package ch.spacebase.openclassic.client.level;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.client.util.BlockUtils;

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

	public int spawn(Position pos, int max, boolean showProgress) {
		if(showProgress) {
			OpenClassic.getClient().getProgressBar().setText("Spawning...");
		}
		
		ClientLevel level = (ClientLevel) pos.getLevel();
		int count = 0;
		for(int mob = 0; mob < max; mob++) {
			if(showProgress) {
				OpenClassic.getClient().getProgressBar().setProgress(mob * 100 / (max - 1));
				OpenClassic.getClient().getProgressBar().render();
			}

			Class<? extends Mob> spawnable[] = ANIMALS;
			if(OpenClassic.getClient().getSettings().getIntSetting("options.survival").getValue() > 1) {
				spawnable = ALL;
			}

			int type = level.getRandom().nextInt(spawnable.length);
			int rx = level.getRandom().nextInt(level.getWidth());
			int ry = (int) (Math.min(level.getRandom().nextFloat(), level.getRandom().nextFloat()) * level.getHeight());
			int rz = level.getRandom().nextInt(level.getDepth());
			BlockType block = level.getBlockTypeAt(rx, ry, rz);
			if(!BlockUtils.preventsRendering(level, rx, ry, rz) && !(block != null && block.isLiquid()) && (!level.isLit(rx, ry, rz) || level.getRandom().nextInt(5) == 0)) {
				for(int pass = 0; pass < 3; pass++) {
					int bx = rx;
					int by = ry;
					int bz = rz;
					for(int run = 0; run < 3; run++) {
						bx += level.getRandom().nextInt(6) - level.getRandom().nextInt(6);
						by += level.getRandom().nextInt(1) - level.getRandom().nextInt(1);
						bz += level.getRandom().nextInt(6) - level.getRandom().nextInt(6);
						if(bx >= 0 && bz >= 1 && by >= 0 && by < level.getHeight() - 2 && bx < level.getWidth() && bz < level.getDepth() && BlockUtils.preventsRendering(level, bx, by - 1, bz) && !BlockUtils.preventsRendering(level, bx, by, bz) && !BlockUtils.preventsRendering(level, bx, by + 1, bz)) {
							float sx = bx + 0.5f;
							float sy = by + 1;
							float sz = bz + 0.5f;
							float dx = sx - pos.getX();
							float dy = sy - pos.getY();
							float dz = sz - pos.getZ();
							if(dx * dx + dy * dy + dz * dz < 256) {
								continue;
							}

							Mob spawning = null;
							try {
								spawning = spawnable[type].getConstructor(ClientLevel.class, float.class, float.class, float.class).newInstance(level, sx, sy, sz);
							} catch(Exception e) {
								OpenClassic.getLogger().severe("Failed to spawn mob \"" + spawnable[type].getSimpleName() + "\"");
								e.printStackTrace();
							}

							if(spawning != null && level.isFree(spawning.bb)) {
								count++;
								level.addEntity(spawning);
							}
						}
					}
				}
			}
		}

		return count;
	}
	
}
