package com.mojang.minecraft.gamemode;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.level.MobSpawner;
import ch.spacebase.openclassic.client.render.RenderHelper;
import ch.spacebase.openclassic.client.util.BlockUtils;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.entity.mob.Mob;
import com.mojang.minecraft.entity.player.LocalPlayer;

public class SurvivalGameMode extends GameMode {

	private int hitX;
	private int hitY;
	private int hitZ;
	private int hits;
	private int blockHardness;
	private int hitDelay;
	private int soundCounter;
	private MobSpawner spawner;

	public SurvivalGameMode(Minecraft mc) {
		super(mc);
	}

	public void preparePlayer(LocalPlayer player) {
		player.inventory.slots[8] = VanillaBlock.TNT.getId();
		player.inventory.count[8] = 10;
	}

	public void breakBlock(int x, int y, int z) {
		BlockType block = this.mc.level.getBlockTypeAt(x, y, z);
		BlockUtils.dropItems(block, this.mc.level, x, y, z);
		super.breakBlock(x, y, z);
	}

	public boolean canPlace(int block) {
		return this.mc.player.inventory.removeSelected(block);
	}

	public void hitBlock(int x, int y, int z) {
		BlockType block = this.mc.level.getBlockTypeAt(x, y, z);
		if(block != null && BlockUtils.getHardness(block) == 0) {
			this.breakBlock(x, y, z);
		}
	}

	public void resetHits() {
		this.hits = 0;
		this.soundCounter = 0;
		this.hitDelay = 0;
	}

	public void hitBlock(int x, int y, int z, int side) {
		if(this.hitDelay > 0) {
			this.hitDelay--;
		} else if(x == this.hitX && y == this.hitY && z == this.hitZ) {
			BlockType type = this.mc.level.getBlockTypeAt(x, y, z);
			if(type != null) {
				this.blockHardness = BlockUtils.getHardness(type);
				RenderHelper.getHelper().spawnBlockParticles(this.mc.level, x, y, z, side, this.mc.particleManager);
				this.hits++;
				if(this.soundCounter % 4 == 0) {
					StepSound sound = type.getStepSound();
					OpenClassic.getClient().getAudioManager().playSound(sound.getSound(), x, y, z, (sound.getVolume() + 1.0F) / 8F, sound.getPitch() * 0.5F);
				}

				this.soundCounter++;
				if(this.hits == this.blockHardness + 1) {
					this.breakBlock(x, y, z);
					this.hits = 0;
					this.hitDelay = 5;
				}

			}
		} else {
			this.hits = 0;
			this.soundCounter = 0;
			this.hitX = x;
			this.hitY = y;
			this.hitZ = z;
		}
	}

	public void applyBlockCracks(float time) {
		if(this.hits <= 0) {
			this.mc.levelRenderer.cracks = 0;
		} else {
			this.mc.levelRenderer.cracks = (this.hits + time - 1) / this.blockHardness;
		}
	}

	public float getReachDistance() {
		return 4;
	}

	public boolean useItem(LocalPlayer player, int type) {
		BlockType block = Blocks.fromId(type);
		if(block == VanillaBlock.RED_MUSHROOM && this.mc.player.inventory.removeSelected(type)) {
			player.hurt(null, 3);
			return true;
		} else if(block == VanillaBlock.BROWN_MUSHROOM && this.mc.player.inventory.removeSelected(type)) {
			player.heal(5);
			return true;
		}

		return false;
	}

	public void apply(ClientLevel level) {
		super.apply(level);
		this.spawner = new MobSpawner();
	}

	public void apply(LocalPlayer player) {
		for(int slot = 0; slot < 9; slot++) {
			player.inventory.slots[slot] = -1;
			player.inventory.count[slot] = 0;
		}

		player.inventory.slots[8] = VanillaBlock.TNT.getId();
		player.inventory.count[8] = 10;
	}

	public void spawnMobs() {
		ClientLevel level = (ClientLevel) OpenClassic.getClient().getLevel();
		int area = level.getWidth() * level.getHeight() * level.getDepth() / 64 / 64 / 64;
		if(level.getRandom().nextInt(100) < area && level.countInstanceOf(Mob.class) < area * 20) {
			this.spawner.spawn(OpenClassic.getClient().getPlayer().getPosition(), area, false);
		}
	}

	public void prepareLevel(ClientLevel level) {
		this.spawner = new MobSpawner();
		int area = level.getWidth() * level.getHeight() * level.getDepth() / 800;
		this.spawner.spawn(level.getSpawn(), area, true);
	}
}
