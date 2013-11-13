package com.mojang.minecraft.render;

import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.client.render.Renderer;

import com.mojang.minecraft.entity.player.LocalPlayer;
import com.mojang.minecraft.level.Level;

public final class Chunk {

	public static int chunkUpdates = 0;
	
	private Level level;
	private int baseListId = -1;
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

		for(int pass = 0; pass < 2; pass++) {
			boolean continuing = false;
			boolean cleaned = false;
			GL11.glNewList(this.baseListId + pass, GL11.GL_COMPILE);
			Renderer.get().begin();
			for(int x = this.x; x < this.x + this.width; x++) {
				for(int y = this.y; y < this.y + this.height; y++) {
					for(int z = this.z; z < this.z + this.depth; z++) {
						int type = this.level.getTile(x, y, z);
						if(type > 0) {
							BlockType block = Blocks.fromId(type);
							if(block == null) {
								block = VanillaBlock.STONE;
							}

							int requiredPass = block.isLiquid() ? 1 : 0;
							if(requiredPass != pass) {
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
			if(cleaned) {
				this.dirty[pass] = false;
			}

			if(!continuing) {
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
		for(int index = 0; index < 2; index++) {
			this.dirty[index] = true;
		}
	}

	public final void dispose() {
		this.setAllDirty();
		this.level = null;
	}

	public final void appendLists(IntBuffer buffer, int pass) {
		if(this.visible && !this.dirty[pass]) {
			buffer.put(this.baseListId + pass);
		}
	}

	public final void clip() {
		this.visible = Frustum.isBoxInFrustum(this.x, this.y, this.z, this.x + this.width, this.y + this.height, this.z + this.depth);
	}

}
