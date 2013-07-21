package com.mojang.minecraft.render;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.client.render.Renderer;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.player.LocalPlayer;

public final class Chunk {

	private Level level;
	private int baseListId = -1;
	public static int chunkUpdates = 0;
	private int x;
	private int y;
	private int z;
	private int width;
	private int height;
	private int depth;
	public boolean visible = false;
	private boolean[] dirty = new boolean[2];
	public boolean loaded;

	public Chunk(Level level, int x, int y, int z, int size, int baseId) {
		this.level = level;
		this.x = x;
		this.y = y;
		this.z = z;
		this.width = size;
		this.height = size;
		this.depth = size;
		this.baseListId = baseId;
		this.setAllDirty();
	}

	public final void update() {
		chunkUpdates++;
		this.setAllDirty();

		for (int pass = 0; pass < 2; pass++) {
			boolean continuing = false;
			boolean cleaned = false;
			GL11.glNewList(this.baseListId + pass, GL11.GL_COMPILE);
			Renderer.get().begin();
			for (int x = this.x; x < this.x + this.width; x++) {
				for (int y = this.y; y < this.y + this.height; y++) {
					for (int z = this.z; z < this.z + this.depth; z++) {
						int type = this.level.getTile(x, y, z);
						if (type > 0) {
							BlockType block = Blocks.fromId(type);
							if(block == null) block = VanillaBlock.STONE;
							
							int requiredPass = block.getId() == VanillaBlock.WATER.getId() || block.getId() == VanillaBlock.STATIONARY_WATER.getId() ? 1 : 0;
							if (requiredPass != pass) {
								continuing = true;
							} else {
								cleaned |= block.getModel().render(x, y, z, this.level.getBrightness(x, y, z), true);
							}
						}
					}
				}
			}

			Renderer.get().end();
			GL11.glEndList();
			if (cleaned) {
				this.dirty[pass] = false;
			}

			if (!continuing) {
				break;
			}
		}

	}

	public final float distanceSquared(LocalPlayer player) {
		float xDistance = player.x - this.x;
		float yDistance = player.y - this.y;
		float zDistance = player.z - this.z;
		return xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
	}

	private void setAllDirty() {
		for (int index = 0; index < 2; ++index) {
			this.dirty[index] = true;
		}
	}

	public final void dispose() {
		this.setAllDirty();
		this.level = null;
	}

	public final int appendData(int[] data, int start, int var3) {
		if (!this.visible) {
			return start;
		} else {
			if (!this.dirty[var3]) {
				data[start++] = this.baseListId + var3;
			}

			return start;
		}
	}

	public final void clip(Frustum check) {
		this.visible = check.isBoxInFrustum(this.x, this.y, this.z, this.x + this.width, this.y + this.height, this.z + this.depth);
	}

}
