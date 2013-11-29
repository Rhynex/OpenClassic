package com.mojang.minecraft.render;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.render.Renderer;

import com.mojang.minecraft.entity.player.LocalPlayer;

public class LevelRenderer {

	public ClientLevel level;
	public int boundaryList;
	public IntBuffer listBuffer = BufferUtils.createIntBuffer(65536);
	public List<Chunk> chunks = new ArrayList<Chunk>();
	private Chunk[] loadQueue;
	public Chunk[] chunkCache;
	private int xChunks;
	private int zChunks;
	private int yChunks;
	private int baseListId;
	public int ticks = 0;
	private float lastLoadX = -9999;
	private float lastLoadY = -9999;
	private float lastLoadZ = -9999;
	public float cracks;

	public LevelRenderer() {
		this.boundaryList = GL11.glGenLists(2);
		this.baseListId = GL11.glGenLists(524288);
	}

	public void refresh() {
		if(this.level == null) {
			return;
		}

		if(this.chunkCache != null) {
			for(int index = 0; index < this.chunkCache.length; index++) {
				this.chunkCache[index].dispose();
			}
		}

		this.xChunks = this.level.getWidth() / 16;
		this.zChunks = this.level.getHeight() / 16;
		this.yChunks = this.level.getDepth() / 16;
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
		GL11.glNewList(this.boundaryList, GL11.GL_COMPILE);
		GL11.glColor4f(0.5F, 0.5F, 0.5F, 1);
		float groundLevel = this.level.getGroundLevel();
		int length = 128;
		if(length > this.level.getWidth()) {
			length = this.level.getWidth();
		}

		if(length > this.level.getDepth()) {
			length = this.level.getDepth();
		}

		int scale = 2048 / length;
		Renderer.get().begin();
		for(int x = -length * scale; x < this.level.getWidth() + length * scale; x += length) {
			for(int z = -length * scale; z < this.level.getDepth() + length * scale; z += length) {
				float y = groundLevel;
				if(x >= 0 && z >= 0 && x < this.level.getWidth() && z < this.level.getDepth()) {
					y = 0;
				}

				Renderer.get().vertexuv(x, y, (z + length), 0, length);
				Renderer.get().vertexuv((x + length), y, (z + length), length, length);
				Renderer.get().vertexuv((x + length), y, z, length, 0);
				Renderer.get().vertexuv(x, y, z, 0, 0);
			}
		}

		Renderer.get().end();
		GL11.glColor3f(0.8F, 0.8F, 0.8F);
		Renderer.get().begin();

		for(int x = 0; x < this.level.getWidth(); x += length) {
			Renderer.get().vertexuv(x, 0, 0, 0, 0);
			Renderer.get().vertexuv((x + length), 0, 0, length, 0);
			Renderer.get().vertexuv((x + length), groundLevel, 0, length, groundLevel);
			Renderer.get().vertexuv(x, groundLevel, 0, 0, groundLevel);
			Renderer.get().vertexuv(x, groundLevel, this.level.getDepth(), 0, groundLevel);
			Renderer.get().vertexuv((x + length), groundLevel, this.level.getDepth(), length, groundLevel);
			Renderer.get().vertexuv((x + length), 0, this.level.getDepth(), length, 0);
			Renderer.get().vertexuv(x, 0, this.level.getDepth(), 0, 0);
		}

		GL11.glColor3f(0.6F, 0.6F, 0.6F);
		for(int z = 0; z < this.level.getDepth(); z += length) {
			Renderer.get().vertexuv(0, groundLevel, z, 0, 0);
			Renderer.get().vertexuv(0, groundLevel, (z + length), length, 0);
			Renderer.get().vertexuv(0, 0, (z + length), length, groundLevel);
			Renderer.get().vertexuv(0, 0, z, 0, groundLevel);
			Renderer.get().vertexuv(this.level.getWidth(), 0, z, 0, groundLevel);
			Renderer.get().vertexuv(this.level.getWidth(), 0, (z + length), length, groundLevel);
			Renderer.get().vertexuv(this.level.getWidth(), groundLevel, (z + length), length, 0);
			Renderer.get().vertexuv(this.level.getWidth(), groundLevel, z, 0, 0);
		}

		Renderer.get().end();
		GL11.glEndList();
		GL11.glNewList(this.boundaryList + 1, GL11.GL_COMPILE);
		GL11.glColor3f(1, 1, 1);
		float waterLevel = this.level.getWaterLevel();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		int len = 128;
		if(len > this.level.getWidth()) {
			len = this.level.getWidth();
		}

		if(len > this.level.getDepth()) {
			len = this.level.getDepth();
		}

		Renderer.get().begin();
		for(int x = -2048; x < this.level.getWidth() + 2048; x += len) {
			for(int z = -2048; z < this.level.getDepth() + 2048; z += len) {
				float y = waterLevel - 0.05F;
				if(x < 0 || z < 0 || x >= this.level.getWidth() || z >= this.level.getDepth()) {
					Renderer.get().vertexuv(x, y, (z + len), 0, len);
					Renderer.get().vertexuv((x + len), y, (z + len), 0 + len, len);
					Renderer.get().vertexuv((x + len), y, z, 0 + len, 0);
					Renderer.get().vertexuv(x, y, z, 0, 0);
					Renderer.get().vertexuv(x, y, z, 0, 0);
					Renderer.get().vertexuv((x + len), y, z, 0 + len, 0);
					Renderer.get().vertexuv((x + len), y, (z + len), 0 + len, len);
					Renderer.get().vertexuv(x, y, (z + len), 0, len);
				}
			}
		}

		Renderer.get().end();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEndList();
		this.queueChunks(0, 0, 0, this.level.getWidth(), this.level.getHeight(), this.level.getDepth());
	}

	public int sortAndRender(LocalPlayer player, int pass) {
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

		this.listBuffer.clear();
		for(int index = 0; index < this.loadQueue.length; index++) {
			this.loadQueue[index].appendLists(this.listBuffer, pass);
		}

		this.listBuffer.flip();
		if(this.listBuffer.remaining() > 0) {
			GL11.glCallLists(this.listBuffer);
		}

		return this.listBuffer.remaining();
	}

	public void queueChunks(int x1, int z1, int y1, int x2, int z2, int y2) {
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
