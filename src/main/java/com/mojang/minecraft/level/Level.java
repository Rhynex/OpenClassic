package com.mojang.minecraft.level;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.event.block.BlockPhysicsEvent;
import ch.spacebase.openclassic.api.event.level.SpawnChangeEvent;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.util.BlockUtils;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.item.PrimedTnt;
import com.mojang.minecraft.entity.model.Vector;
import com.mojang.minecraft.entity.particle.ParticleManager;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.phys.Intersection;
import com.zachsthings.onevent.EventManager;

public class Level {

	private static final Random rand = new Random();

	public int width;
	public int height;
	public int depth;
	public byte[] blocks;
	public String name;
	public String creator;
	public long createTime;
	public float xSpawn;
	public float ySpawn;
	public float zSpawn;
	public float yawSpawn;
	public float pitchSpawn;
	private transient int[] highest;
	public transient Random random = new Random();
	private transient int id;
	private transient ArrayList<TickNextTick> tickNextTicks;
	public BlockMap blockMap;
	private boolean networkMode;
	public transient Minecraft rendererContext;
	public boolean creativeMode;
	public int waterLevel;
	public int skyColor;
	public int fogColor;
	public int cloudColor;
	private int unprocessed;
	public Entity player;
	public transient ParticleManager particleEngine;
	public transient Object font;
	public boolean growTrees;

	public transient ClientLevel openclassic;

	public Level() {
		this.id = this.random.nextInt();
		this.tickNextTicks = new ArrayList<TickNextTick>();
		this.networkMode = false;
		this.unprocessed = 0;
		this.growTrees = false;

		this.openclassic = new ClientLevel(this);
	}

	public void initTransient() {
		if(this.blocks == null) {
			throw new RuntimeException("The level is corrupt!");
		} else {
			this.highest = new int[this.width * this.depth];
			Arrays.fill(this.highest, this.height);
			this.calcLightDepths(0, 0, this.width, this.depth);
			this.random = new Random();
			this.id = this.random.nextInt();
			this.tickNextTicks = new ArrayList<TickNextTick>();
			if(this.waterLevel == 0) {
				this.waterLevel = this.height / 2;
			}

			Minecraft mc = GeneralUtils.getMinecraft();
			if(mc.settings.night) {
				this.skyColor = 0;
				this.fogColor = new Color(30, 30, 30, 70).getRGB();
				this.cloudColor = new Color(30, 30, 30, 70).getRGB();
			} else {
				this.skyColor = 10079487;
				this.fogColor = 16777215;
				this.cloudColor = 16777215;
			}

			if(this.xSpawn == 0 && this.ySpawn == 0 && this.zSpawn == 0) {
				this.findSpawn();
			}

			if(this.blockMap == null) {
				this.blockMap = new BlockMap(this.width, this.height, this.depth);
			}
		}
	}

	public void setData(int width, int height, int depth, byte[] blocks) {
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.waterLevel = height / 2;
		this.blocks = blocks;
		this.highest = new int[width * height];
		Arrays.fill(this.highest, this.height);
		this.calcLightDepths(0, 0, width, height);
		if(this.rendererContext != null) {
			this.rendererContext.levelRenderer.refresh();
		}

		this.tickNextTicks.clear();
		this.initTransient();
	}

	public void findSpawn() {
		Random rand = new Random();
		int attempts = 0;

		int x = 0;
		int z = 0;
		int y = 0;
		while(y <= this.getWaterLevel()) {
			attempts++;
			x = rand.nextInt(this.width / 2) + this.width / 4;
			y = this.getHighestTile(x, z) + 1;
			z = rand.nextInt(this.depth / 2) + this.depth / 4;
			if(attempts == 10000) {
				this.xSpawn = x;
				this.ySpawn = -100;
				this.zSpawn = z;
				return;
			}
		}

		this.xSpawn = x;
		this.ySpawn = y;
		this.zSpawn = z;
	}

	public void calcLightDepths(int x1, int z1, int x2, int z2) {
		for(int x = x1; x < x1 + x2; x++) {
			for(int z = z1; z < z1 + z2; z++) {
				int highest = this.highest[x + z * this.width];

				int blocker = this.height - 1;
				while(blocker > 0 && !this.isLightBlocker(x, blocker, z)) {
					blocker--;
				}

				this.highest[x + z * this.width] = blocker;
				if(highest != blocker) {
					int lower = highest < blocker ? highest : blocker;
					highest = highest > blocker ? highest : blocker;
					if(this.rendererContext != null) {
						this.rendererContext.levelRenderer.queueChunks(x - 1, lower - 1, z - 1, x + 1, highest + 1, z + 1);
					}
				}
			}
		}

	}

	public boolean isLightBlocker(int x, int y, int z) {
		BlockType block = Blocks.fromId(this.getTile(x, y, z));
		return block != null && block.isOpaque();
	}

	public ArrayList<AABB> getCubes(AABB aabb) {
		ArrayList<AABB> ret = new ArrayList<AABB>();
		int x0 = (int) aabb.x0;
		int x1 = (int) aabb.x1 + 1;
		int y0 = (int) aabb.y0;
		int y1 = (int) aabb.y1 + 1;
		int z0 = (int) aabb.z0;
		int z1 = (int) aabb.z1 + 1;
		if(aabb.x0 < 0.0F) {
			x0--;
		}

		if(aabb.y0 < 0.0F) {
			y0--;
		}

		if(aabb.z0 < 0.0F) {
			z0--;
		}

		for(int x = x0; x < x1; x++) {
			for(int y = y0; y < y1; y++) {
				for(int z = z0; z < z1; z++) {
					if(x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.height && z < this.depth) {
						BlockType type = Blocks.fromId(this.getTile(x, y, z));
						AABB bb = BlockUtils.getCollisionBox(type.getId(), x, y, z);
						if(type != null && bb != null && aabb.intersectsInner(bb)) {
							ret.add(bb);
						}
					} else if(x < 0 || y < 0 || z < 0 || x >= this.width || z >= this.depth) {
						AABB bb = BlockUtils.getCollisionBox(VanillaBlock.BEDROCK.getId(), x, y, z);
						if(bb != null && aabb.intersectsInner(bb)) {
							ret.add(bb);
						}
					}
				}
			}
		}

		return ret;
	}

	public void swap(int x1, int y1, int z1, int x2, int y2, int z2) {
		if(!this.networkMode) {
			int b1 = this.getTile(x1, y1, z1);
			int b2 = this.getTile(x2, y2, z2);
			this.setTileNoNeighborChange(x1, y1, z1, b2);
			this.setTileNoNeighborChange(x2, y2, z2, b1);
			this.updateNeighborsAt(x1, y1, z1);
			this.updateNeighborsAt(x2, y2, z2);
		}
	}

	public boolean setTileNoNeighborChange(int x, int y, int z, int type) {
		return this.networkMode ? false : this.netSetTileNoNeighborChange(x, y, z, type);
	}

	public boolean netSetTileNoNeighborChange(int x, int y, int z, int type) {
		if(x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.height && z < this.depth) {
			if(type == this.blocks[(y * this.depth + z) * this.width + x]) {
				return false;
			} else {
				if(type == 0 && (x == 0 || z == 0 || x == this.width - 1 || z == this.depth - 1) && y >= this.getGroundLevel() && y < this.getWaterLevel()) {
					type = VanillaBlock.WATER.getId();
				}

				this.blocks[(y * this.depth + z) * this.width + x] = (byte) type;
				this.calcLightDepths(x, z, 1, 1);
				if(this.rendererContext != null) {
					this.rendererContext.levelRenderer.queueChunks(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
				}

				return true;
			}
		} else {
			return false;
		}
	}

	public boolean setTile(int x, int y, int z, int type) {
		if(this.networkMode) {
			return false;
		} else if(this.setTileNoNeighborChange(x, y, z, type)) {
			this.updateNeighborsAt(x, y, z);
			return true;
		} else {
			return false;
		}
	}

	public boolean netSetTile(int x, int y, int z, int type) {
		if(this.netSetTileNoNeighborChange(x, y, z, type)) {
			this.updateNeighborsAt(x, y, z);
			return true;
		} else {
			return false;
		}
	}

	public void updateNeighborsAt(int x, int y, int z) {
		if(this.openclassic.getPhysicsEnabled()) {
			this.updateNeighbor(x - 1, y, z, x, y, z);
			this.updateNeighbor(x + 1, y, z, x, y, z);
			this.updateNeighbor(x, y - 1, z, x, y, z);
			this.updateNeighbor(x, y + 1, z, x, y, z);
			this.updateNeighbor(x, y, z - 1, x, y, z);
			this.updateNeighbor(x, y, z + 1, x, y, z);
		}
	}

	public boolean setTileNoUpdate(int x, int y, int z, int type) {
		if(x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.height && z < this.depth) {
			if(type == this.blocks[(y * this.depth + z) * this.width + x]) {
				return false;
			} else {
				this.blocks[(y * this.depth + z) * this.width + x] = (byte) type;
				return true;
			}
		} else {
			return false;
		}
	}

	private void updateNeighbor(int x, int y, int z, int nx, int nz, int ny) {
		if(x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.height && z < this.depth) {
			BlockType type = Blocks.fromId(this.getTile(x, y, z));
			if(type != null) {
				if(type.getPhysics() != null) {
					type.getPhysics().onNeighborChange(this.openclassic.getBlockAt(x, y, z), this.openclassic.getBlockAt(nx, ny, nz));
				}
			}

		}
	}

	public boolean isLit(int x, int y, int z) {
		return x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.height && z < this.depth ? y >= this.highest[x + z * this.width] : true;
	}

	public int getTile(int x, int y, int z) {
		return x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.height && z < this.depth ? this.blocks[(y * this.depth + z) * this.width + x] & 255 : 0;
	}

	public boolean getPreventsRendering(int x, int y, int z) {
		BlockType type = Blocks.fromId(this.getTile(x, y, z));
		return type != null && type.getPreventsRendering();
	}

	public void tickEntities() {
		this.blockMap.tickAll();
	}

	public void tick() {
		int wshift = 1;
		int dshift = 1;
		while(1 << wshift < this.width) {
			wshift++;
		}

		while(1 << dshift < this.depth) {
			dshift++;
		}

		int size = this.tickNextTicks.size();
		for(int ct = 0; ct < size; ct++) {
			TickNextTick next = this.tickNextTicks.remove(0);
			if(next.ticks > 0) {
				next.ticks--;
				this.tickNextTicks.add(next);
			} else {
				if(this.isInBounds(next.x, next.y, next.z)) {
					byte block = this.blocks[(next.y * this.depth + next.z) * this.width + next.x];
					if(block == next.block && block > 0) {
						if(Blocks.fromId(block) != null && Blocks.fromId(block).getPhysics() != null && this.openclassic.getPhysicsEnabled()) {
							Blocks.fromId(block).getPhysics().update(this.openclassic.getBlockAt(next.x, next.y, next.z));
						}
					}
				}
			}
		}

		this.unprocessed += this.width * this.height * this.depth;
		int ticks = this.unprocessed / 200;
		this.unprocessed = 0;

		for(int count = 0; count < ticks; count++) {
			this.id = this.id * 3 + 1013904223;
			int y = this.id >> 2;
			int x = (y) & (this.width - 1);
			int z = y >> wshift & (this.depth - 1);
			y = y >> wshift + dshift & (this.height - 1);
			BlockType block = Blocks.fromId(this.blocks[(y * this.depth + z) * this.width + x]);
			if(block != null && block.getPhysics() != null && this.openclassic.getPhysicsEnabled() && !EventManager.callEvent(new BlockPhysicsEvent(this.openclassic.getBlockAt(x, y, z))).isCancelled()) {
				block.getPhysics().update(this.openclassic.getBlockAt(x, y, z));
			}
		}

		this.openclassic.tick();
	}

	public int countInstanceOf(Class<? extends Entity> clazz) {
		int instances = 0;

		for(int count = 0; count < this.blockMap.all.size(); count++) {
			Entity entity = this.blockMap.all.get(count);
			if(clazz.isAssignableFrom(entity.getClass())) {
				instances++;
			}
		}

		return instances;
	}

	private boolean isInBounds(int x, int y, int z) {
		return x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.height && z < this.depth;
	}

	public float getGroundLevel() {
		return this.getWaterLevel() - 2.0F;
	}

	public float getWaterLevel() {
		return this.waterLevel;
	}

	public boolean containsAnyLiquid(AABB aabb) {
		int x0 = (int) aabb.x0;
		int x1 = (int) aabb.x1 + 1;
		int y0 = (int) aabb.y0;
		int y1 = (int) aabb.y1 + 1;
		int z0 = (int) aabb.z0;
		int z1 = (int) aabb.z1 + 1;
		if(aabb.x0 < 0.0F) {
			x0--;
		}

		if(aabb.y0 < 0.0F) {
			y0--;
		}

		if(aabb.z0 < 0.0F) {
			z0--;
		}

		if(x0 < 0) {
			x0 = 0;
		}

		if(y0 < 0) {
			y0 = 0;
		}

		if(z0 < 0) {
			z0 = 0;
		}

		if(x1 > this.width) {
			x1 = this.width;
		}

		if(y1 > this.height) {
			y1 = this.height;
		}

		if(z1 > this.depth) {
			z1 = this.depth;
		}

		for(int x = x0; x < x1; x++) {
			for(int y = y0; y < y1; y++) {
				for(int z = z0; z < z1; z++) {
					BlockType type = Blocks.fromId(this.getTile(x, y, z));
					if(type != null && type.isLiquid()) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public boolean containsLiquid(AABB aabb, BlockType block) {
		block = toMoving(block);
		int x0 = (int) aabb.x0;
		int x1 = (int) aabb.x1 + 1;
		int y0 = (int) aabb.y0;
		int y1 = (int) aabb.y1 + 1;
		int z0 = (int) aabb.z0;
		int z1 = (int) aabb.z1 + 1;
		if(aabb.x0 < 0.0F) {
			x0--;
		}

		if(aabb.y0 < 0.0F) {
			y0--;
		}

		if(aabb.z0 < 0.0F) {
			z0--;
		}

		if(x0 < 0) {
			x0 = 0;
		}

		if(y0 < 0) {
			y0 = 0;
		}

		if(z0 < 0) {
			z0 = 0;
		}

		if(x1 > this.width) {
			x1 = this.width;
		}

		if(y1 > this.height) {
			y1 = this.height;
		}

		if(z1 > this.depth) {
			z1 = this.depth;
		}

		for(int x = x0; x < x1; x++) {
			for(int y = y0; y < y1; y++) {
				for(int z = z0; z < z1; z++) {
					BlockType type = Blocks.fromId(this.getTile(x, y, z));
					if(type != null && toMoving(type) == block) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public static BlockType toMoving(BlockType block) {
		if(block == VanillaBlock.STATIONARY_LAVA) return VanillaBlock.LAVA;
		if(block == VanillaBlock.STATIONARY_WATER) return VanillaBlock.WATER;
		return block;
	}

	public void addToTickNextTick(int x, int y, int z, int block) {
		if(!this.networkMode) {
			TickNextTick next = new TickNextTick(x, y, z, block);
			if(block > 0 && Blocks.fromId(block) != null) {
				next.ticks = Blocks.fromId(block).getTickDelay();
			}

			this.tickNextTicks.add(next);
		}
	}

	public boolean isFree(AABB aabb) {
		return this.blockMap.getEntities(null, aabb).size() == 0;
	}

	public List<Entity> findEntities(Entity entity, AABB aabb) {
		return this.blockMap.getEntities(entity, aabb);
	}

	public boolean preventsRendering(float x, float y, float z, float distance) {
		return this.preventsRendering(x - distance, y - distance, z - distance) || this.preventsRendering(x - distance, y - distance, z + distance) || this.preventsRendering(x - distance, y + distance, z - distance) || this.preventsRendering(x - distance, y + distance, z + distance) || this.preventsRendering(x + distance, y - distance, z - distance) || this.preventsRendering(x + distance, y - distance, z + distance) || this.preventsRendering(x + distance, y + distance, z - distance) || this.preventsRendering(x + distance, y + distance, z + distance);
	}

	private boolean preventsRendering(float x, float y, float z) {
		int tile = this.getTile((int) x, (int) y, (int) z);
		return tile > 0 && Blocks.fromId(tile) != null && Blocks.fromId(tile).getPreventsRendering();
	}

	public int getHighestTile(int x, int z) {
		int y = this.height;
		while((this.getTile(x, y - 1, z) == 0 || (Blocks.fromId(this.getTile(x, y - 1, z)) != null && Blocks.fromId(this.getTile(x, y - 1, z)).isLiquid())) && y > 0) {
			y--;
		}

		return y;
	}

	public void setSpawnPos(float x, float y, float z, float yaw, float pitch) {
		Position old = new Position(this.openclassic, this.xSpawn, this.ySpawn, this.zSpawn, this.yawSpawn, this.pitchSpawn);
		this.xSpawn = x;
		this.ySpawn = y;
		this.zSpawn = z;
		this.yawSpawn = yaw;
		this.pitchSpawn = pitch;

		EventManager.callEvent(new SpawnChangeEvent(this.openclassic, old));
	}

	public float getBrightness(int x, int y, int z) {
		BlockType block = this.openclassic.getBlockTypeAt(x, y, z);
		Minecraft mc = GeneralUtils.getMinecraft();
		float mod = mc.settings.night ? 0.4F : 0;
		return block != null && block.getBrightness() > 0 ? block.getBrightness() : this.isLit(x, y, z) ? 1 - mod : 0.6F - mod;
	}

	public byte[] copyBlocks() {
		return Arrays.copyOf(this.blocks, this.blocks.length);
	}

	public boolean isWater(int x, int y, int z) {
		int tile = this.getTile(x, y, z);
		return tile > 0 && (Blocks.fromId(tile) == VanillaBlock.WATER || Blocks.fromId(tile) == VanillaBlock.STATIONARY_WATER);
	}

	public void setNetworkMode(boolean network) {
		this.networkMode = network;
	}

	public Intersection clip(Vector vec1, Vector vec2) {
		return this.clip(vec1, vec2, false);
	}

	public Intersection clip(Vector vec1, Vector vec2, boolean selection) {
		if(!Float.isNaN(vec1.x) && !Float.isNaN(vec1.y) && !Float.isNaN(vec1.z)) {
			if(!Float.isNaN(vec2.x) && !Float.isNaN(vec2.y) && !Float.isNaN(vec2.z)) {
				int bx2 = (int) Math.floor(vec2.x);
				int by2 = (int) Math.floor(vec2.y);
				int bz2 = (int) Math.floor(vec2.z);
				int bx1 = (int) Math.floor(vec1.x);
				int by1 = (int) Math.floor(vec1.y);
				int bz1 = (int) Math.floor(vec1.z);
				int total = 20;

				while(total-- >= 0) {
					if(Float.isNaN(vec1.x) || Float.isNaN(vec1.y) || Float.isNaN(vec1.z)) {
						return null;
					}

					if(bx1 == bx2 && by1 == by2 && bz1 == bz2) {
						return null;
					}

					float xmax1 = 999.0F;
					float ymax1 = 999.0F;
					float zmax1 = 999.0F;
					if(bx2 > bx1) {
						xmax1 = bx1 + 1.0F;
					}

					if(bx2 < bx1) {
						xmax1 = bx1;
					}

					if(by2 > by1) {
						ymax1 = by1 + 1.0F;
					}

					if(by2 < by1) {
						ymax1 = by1;
					}

					if(bz2 > bz1) {
						zmax1 = bz1 + 1.0F;
					}

					if(bz2 < bz1) {
						zmax1 = bz1;
					}

					float dxmax = 999.0F;
					float dymax = 999.0F;
					float dzmax = 999.0F;
					float dx = vec2.x - vec1.x;
					float dy = vec2.y - vec1.y;
					float dz = vec2.z - vec1.z;
					if(xmax1 != 999.0F) {
						dxmax = (xmax1 - vec1.x) / dx;
					}

					if(ymax1 != 999.0F) {
						dymax = (ymax1 - vec1.y) / dy;
					}

					if(zmax1 != 999.0F) {
						dzmax = (zmax1 - vec1.z) / dz;
					}

					byte face = 0;
					if(dxmax < dymax && dxmax < dzmax) {
						if(bx2 > bx1) {
							face = 4;
						} else {
							face = 5;
						}

						vec1.x = xmax1;
						vec1.y += dy * dxmax;
						vec1.z += dz * dxmax;
					} else if(dymax < dzmax) {
						if(by2 > by1) {
							face = 0;
						} else {
							face = 1;
						}

						vec1.x += dx * dymax;
						vec1.y = ymax1;
						vec1.z += dz * dymax;
					} else {
						if(bz2 > bz1) {
							face = 2;
						} else {
							face = 3;
						}

						vec1.x += dx * dzmax;
						vec1.y += dy * dzmax;
						vec1.z = zmax1;
					}

					Vector vec = new com.mojang.minecraft.entity.model.Vector(vec1.x, vec1.y, vec1.z);
					bx1 = (int) (vec.x = (float) Math.floor(vec1.x));
					if(face == 5) {
						bx1--;
						vec.x++;
					}

					by1 = (int) (vec.y = (float) Math.floor(vec1.y));
					if(face == 1) {
						by1--;
						vec.y++;
					}

					bz1 = (int) (vec.z = (float) Math.floor(vec1.z));
					if(face == 3) {
						bz1--;
						vec.z++;
					}

					BlockType type = Blocks.fromId(this.getTile(bx1, by1, bz1));
					if(type != null && type.getId() != 0 && (type != null && !type.isLiquid())) {
						Intersection clipped = null;
						if(selection) {
							clipped = BlockUtils.clipSelection(type.getId(), bx1, by1, bz1, vec1, vec2);
						} else {
							clipped = BlockUtils.clip(type.getId(), bx1, by1, bz1, vec1, vec2);
						}

						if(type.getModel().getCollisionBox(bx1, by1, bz1) != null) {
							if(clipped != null) {
								return clipped;
							}
						} else {
							if(selection) {
								clipped = BlockUtils.clipSelection(type.getId(), bx1, by1, bz1, vec1, vec2);
							} else {
								clipped = BlockUtils.clip(type.getId(), bx1, by1, bz1, vec1, vec2);
							}

							if(clipped != null) {
								return clipped;
							}
						}
					}
				}

				return null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public void playSound(String name, Entity entity, float volume, float pitch) {
		if(this.rendererContext != null) {
			Minecraft mc = this.rendererContext;
			if(!mc.settings.sound) {
				return;
			}

			if(entity.distanceToSqr(mc.player) < 1024) {
				mc.audio.playSound(name, entity.x, entity.y, entity.z, volume, pitch);
			}
		}
	}

	public void playSound(String name, float x, float y, float z, float volume, float pitch) {
		if(this.rendererContext != null) {
			Minecraft mc = this.rendererContext;
			if(!mc.settings.sound) {
				return;
			}

			mc.audio.playSound(name, x, y, z, volume, pitch);
		}
	}

	public boolean maybeGrowTree(int x, int y, int z) {
		int height = this.random.nextInt(3) + 4;
		boolean spaceFree = true;
		for(int by = y; by <= y + 1 + height; by++) {
			byte radius = 1;
			if(by == y) {
				radius = 0;
			}

			if(by >= y + 1 + height - 2) {
				radius = 2;
			}

			for(int bx = x - radius; bx <= x + radius && spaceFree; bx++) {
				for(int bz = z - radius; bz <= z + radius && spaceFree; bz++) {
					if(bx >= 0 && by >= 0 && bz >= 0 && bx < this.width && by < this.height && bz < this.depth) {
						if(this.getTile(bx, by, bz) != 0) {
							spaceFree = false;
						}
					} else {
						spaceFree = false;
					}
				}
			}
		}

		if(!spaceFree) {
			return false;
		} else if(this.getTile(x, y - 1, z) == VanillaBlock.GRASS.getId() && y < this.height - height - 1) {
			this.setTile(x, y - 1, z, VanillaBlock.DIRT.getId());
			for(int ly = y - 3 + height; ly <= y + height; ly++) {
				int baseDist = ly - (y + height);
				int radius = 1 - baseDist / 2;
				for(int lx = x - radius; lx <= x + radius; lx++) {
					int xdist = lx - x;
					for(int lz = z - radius; lz <= z + radius; lz++) {
						int zdist = lz - z;
						if(Math.abs(xdist) != radius || Math.abs(zdist) != radius || this.random.nextInt(2) != 0 && baseDist != 0) {
							this.setTile(lx, ly, lz, VanillaBlock.LEAVES.getId());
						}
					}
				}
			}

			for(int ly = 0; ly < height; ly++) {
				this.setTile(x, y + ly, z, VanillaBlock.LOG.getId());
			}

			return true;
		} else {
			return false;
		}
	}

	public Entity getPlayer() {
		return this.player;
	}

	public void addEntity(Entity entity) {
		this.blockMap.insert(entity);
		entity.setLevel(this);
	}

	public void removeEntity(Entity entity) {
		this.blockMap.remove(entity);
	}

	public void explode(Entity entity, float x, float y, float z, float power) {
		int minx = (int) (x - power - 1.0F);
		int maxx = (int) (x + power + 1.0F);
		int miny = (int) (y - power - 1.0F);
		int maxy = (int) (y + power + 1.0F);
		int minz = (int) (z - power - 1.0F);
		int maxz = (int) (z + power + 1.0F);
		for(int bx = minx; bx < maxx; bx++) {
			for(int by = maxy - 1; by >= miny; by--) {
				for(int bz = minz; bz < maxz; bz++) {
					float dx = bx + 0.5F - x;
					float dy = by + 0.5F - y;
					float dz = bz + 0.5F - z;
					int tile = this.getTile(bx, by, bz);
					if(bx >= 0 && by >= 0 && bz >= 0 && bx < this.width && by < this.height && bz < this.depth && dx * dx + dy * dy + dz * dz < power * power && tile > 0 && BlockUtils.canExplode(Blocks.fromId(tile))) {
						BlockUtils.dropItems(tile, this, bx, by, bz, 0.3F);
						this.setTile(bx, by, bz, 0);
						if(Blocks.fromId(tile) == VanillaBlock.TNT && !this.creativeMode) {
							PrimedTnt tnt = new PrimedTnt(this, bx + 0.5F, by + 0.5F, bz + 0.5F);
							tnt.life = rand.nextInt(tnt.life / 4) + tnt.life / 8;
							this.addEntity(tnt);
						}
					}
				}
			}
		}

		List<Entity> entities = this.blockMap.getEntities(entity, minx, miny, minz, maxx, maxy, maxz);
		for(int index = 0; index < entities.size(); index++) {
			Entity e = entities.get(index);
			float pow = e.distanceTo(x, y, z) / power;
			if(pow <= 1.0F) {
				e.hurt(entity, (int) ((1.0F - pow) * 15.0F + 1.0F));
			}
		}

	}

	public Entity findSubclassOf(Class<? extends Entity> clazz) {
		for(Entity entity : this.blockMap.all) {
			if(clazz.isAssignableFrom(entity.getClass())) {
				return entity;
			}
		}

		return null;
	}

	public void removeAllNonCreativeModeEntities() {
		this.blockMap.removeAllNonCreativeModeEntities();
	}
}
