package com.mojang.minecraft.render;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.render.Renderer;

import com.mojang.minecraft.entity.player.LocalPlayer;
import com.mojang.minecraft.level.Level;

public final class LevelRenderer {

	public Level level;
	public TextureManager textures;
	public int listId;
	public IntBuffer buffer = BufferUtils.createIntBuffer(65536);
	public List<Chunk> chunks = new ArrayList<Chunk>();
	private Chunk[] loadQueue;
	public Chunk[] chunkCache;
	private int xChunks;
	private int zChunks;
	private int yChunks;
	private int baseListId;
	private int[] chunkDataCache = new int['\uc350'];
	public int ticks = 0;
	private float lastLoadX = -9999.0F;
	private float lastLoadY = -9999.0F;
	private float lastLoadZ = -9999.0F;
	public float cracks;

	public LevelRenderer(TextureManager textures) {
		this.textures = textures;
		this.listId = GL11.glGenLists(2);
		this.baseListId = GL11.glGenLists(524288);
	}

	public final void refresh() {
		if(this.chunkCache != null) {
			for(int index = 0; index < this.chunkCache.length; index++) {
				this.chunkCache[index].dispose();
			}
		}

		this.xChunks = this.level.width / 16;
		this.zChunks = this.level.height / 16;
		this.yChunks = this.level.depth / 16;
		this.chunkCache = new Chunk[this.xChunks * this.zChunks * this.yChunks];
		this.loadQueue = new Chunk[this.xChunks * this.zChunks * this.yChunks];
		int listCount = 0;

		for(int x = 0; x < this.xChunks; x++) {
			for(int y = 0; y < this.zChunks; y++) {
				for(int z = 0; z < this.yChunks; z++) {
					this.chunkCache[(z * this.zChunks + y) * this.xChunks + x] = new Chunk(this.level, x << 4, y << 4, z << 4, 16, this.baseListId + listCount);
					this.loadQueue[(z * this.zChunks + y) * this.xChunks + x] = this.chunkCache[(z * this.zChunks + y) * this.xChunks + x];
					listCount += 2;
				}
			}
		}

		for(int x = 0; x < this.chunks.size(); x++) {
			this.chunks.get(x).loaded = false;
		}

		this.chunks.clear();
		GL11.glNewList(this.listId, 4864);
		GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
		float groundLevel = this.level.getGroundLevel();
		int length = 128;
		if(128 > this.level.width) {
			length = this.level.width;
		}

		if(length > this.level.depth) {
			length = this.level.depth;
		}

		int mult = 2048 / length;
		Renderer.get().begin();
		for(int x = -length * mult; x < this.level.width + length * mult; x += length) {
			for(int z = -length * mult; z < this.level.depth + length * mult; z += length) {
				float y = groundLevel;
				if(x >= 0 && z >= 0 && x < this.level.width && z < this.level.depth) {
					y = 0;
				}

				Renderer.get().vertexuv(x, y, (z + length), 0.0F, length);
				Renderer.get().vertexuv((x + length), y, (z + length), length, length);
				Renderer.get().vertexuv((x + length), y, z, length, 0.0F);
				Renderer.get().vertexuv(x, y, z, 0.0F, 0.0F);
			}
		}

		Renderer.get().end();
		GL11.glColor3f(0.8F, 0.8F, 0.8F);
		Renderer.get().begin();

		for(int x = 0; x < this.level.width; x += length) {
			Renderer.get().vertexuv(x, 0.0F, 0.0F, 0.0F, 0.0F);
			Renderer.get().vertexuv((x + length), 0.0F, 0.0F, length, 0.0F);
			Renderer.get().vertexuv((x + length), groundLevel, 0.0F, length, groundLevel);
			Renderer.get().vertexuv(x, groundLevel, 0.0F, 0.0F, groundLevel);
			Renderer.get().vertexuv(x, groundLevel, this.level.depth, 0.0F, groundLevel);
			Renderer.get().vertexuv((x + length), groundLevel, this.level.depth, length, groundLevel);
			Renderer.get().vertexuv((x + length), 0.0F, this.level.depth, length, 0.0F);
			Renderer.get().vertexuv(x, 0.0F, this.level.depth, 0.0F, 0.0F);
		}

		GL11.glColor3f(0.6F, 0.6F, 0.6F);
		for(int z = 0; z < this.level.depth; z += length) {
			Renderer.get().vertexuv(0.0F, groundLevel, z, 0.0F, 0.0F);
			Renderer.get().vertexuv(0.0F, groundLevel, (z + length), length, 0.0F);
			Renderer.get().vertexuv(0.0F, 0.0F, (z + length), length, groundLevel);
			Renderer.get().vertexuv(0.0F, 0.0F, z, 0.0F, groundLevel);
			Renderer.get().vertexuv(this.level.width, 0.0F, z, 0.0F, groundLevel);
			Renderer.get().vertexuv(this.level.width, 0.0F, (z + length), length, groundLevel);
			Renderer.get().vertexuv(this.level.width, groundLevel, (z + length), length, 0.0F);
			Renderer.get().vertexuv(this.level.width, groundLevel, z, 0.0F, 0.0F);
		}

		Renderer.get().end();
		GL11.glEndList();
		GL11.glNewList(this.listId + 1, 4864);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		float waterLevel = this.level.getWaterLevel();
		GL11.glBlendFunc(770, 771);
		int len = 128;
		if(len > this.level.width) {
			len = this.level.width;
		}

		if(len > this.level.depth) {
			len = this.level.depth;
		}

		int mul = 2048 / len;
		Renderer.get().begin();
		for(int x = -len * mul; x < this.level.width + len * mul; x += len) {
			for(int z = -len * mul; z < this.level.depth + len * mul; z += len) {
				float y = waterLevel - 0.05F;
				if(x < 0 || z < 0 || x >= this.level.width || z >= this.level.depth) {
					Renderer.get().vertexuv(x, y, (z + len), 0.0F, len);
					Renderer.get().vertexuv((x + len), y, (z + len), len, len);
					Renderer.get().vertexuv((x + len), y, z, len, 0.0F);
					Renderer.get().vertexuv(x, y, z, 0.0F, 0.0F);
					Renderer.get().vertexuv(x, y, z, 0.0F, 0.0F);
					Renderer.get().vertexuv((x + len), y, z, len, 0.0F);
					Renderer.get().vertexuv((x + len), y, (z + len), len, len);
					Renderer.get().vertexuv(x, y, (z + len), 0.0F, len);
				}
			}
		}

		Renderer.get().end();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEndList();
		this.queueChunks(0, 0, 0, this.level.width, this.level.height, this.level.depth);
	}

	public final int sortChunks(LocalPlayer player, int pass) {
		float xDiff = player.x - this.lastLoadX;
		float yDiff = player.y - this.lastLoadY;
		float zDiff = player.z - this.lastLoadZ;
		float sqDistance = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
		if(sqDistance > 64) {
			this.lastLoadX = player.x;
			this.lastLoadY = player.y;
			this.lastLoadZ = player.z;

			try {
				Arrays.sort(this.loadQueue, new ChunkDistanceComparator(player));
			} catch(Exception e) {
			}
		}

		int length = 0;

		for(int index = 0; index < this.loadQueue.length; index++) {
			length = this.loadQueue[index].appendData(this.chunkDataCache, length, pass);
		}

		this.buffer.clear();
		this.buffer.put(this.chunkDataCache, 0, length);
		this.buffer.flip();
		if(this.buffer.remaining() > 0) {
			RenderHelper.getHelper().bindTexture("/terrain.png", true);
			GL11.glCallLists(this.buffer);
		}

		return this.buffer.remaining();
	}

	public final void queueChunks(int x1, int z1, int y1, int x2, int z2, int y2) {
		x1 /= 16;
		z1 /= 16;
		y1 /= 16;
		x2 /= 16;
		z2 /= 16;
		y2 /= 16;
		if(x1 < 0) {
			x1 = 0;
		}

		if(z1 < 0) {
			z1 = 0;
		}

		if(y1 < 0) {
			y1 = 0;
		}

		if(x2 > this.xChunks - 1) {
			x2 = this.xChunks - 1;
		}

		if(z2 > this.zChunks - 1) {
			z2 = this.zChunks - 1;
		}

		if(y2 > this.yChunks - 1) {
			y2 = this.yChunks - 1;
		}

		for(int x = x1; x <= x2; x++) {
			for(int z = z1; z <= z2; z++) {
				for(int y = y1; y <= y2; y++) {
					Chunk chunk = this.chunkCache[(y * this.zChunks + z) * this.xChunks + x];
					if(!chunk.loaded) {
						chunk.loaded = true;
						this.chunks.add(this.chunkCache[(y * this.zChunks + z) * this.xChunks + x]);
					}
				}
			}
		}
	}
}
