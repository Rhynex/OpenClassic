package com.mojang.minecraft.level;

import java.util.ArrayList;
import java.util.List;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.model.Vector;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.render.Frustum;
import com.mojang.minecraft.render.TextureManager;

public class BlockMap {

	private int width;
	private int height;
	private int depth;
	private Slot slot = new Slot();
	private Slot slot2 = new Slot();
	public List<Entity>[] entityGrid;
	public List<Entity> all = new ArrayList<Entity>();
	private List<Entity> tmp = new ArrayList<Entity>();

	@SuppressWarnings("unchecked")
	public BlockMap(int width, int height, int depth) {
		this.width = width / 16;
		this.height = height / 16;
		this.depth = depth / 16;
		if (this.width == 0) {
			this.width = 1;
		}

		if (this.height == 0) {
			this.height = 1;
		}

		if (this.depth == 0) {
			this.depth = 1;
		}

		this.entityGrid = new ArrayList[this.width * this.height * this.depth];
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				for (int z = 0; z < this.depth; z++) {
					this.entityGrid[(z * this.height + y) * this.width + x] = new ArrayList<Entity>();
				}
			}
		}

	}

	public void insert(Entity entity) {
		this.all.add(entity);
		this.slot.init(this, entity.x, entity.y, entity.z).add(entity);
		entity.xOld = entity.x;
		entity.yOld = entity.y;
		entity.zOld = entity.z;
		entity.blockMap = this;
	}

	public void remove(Entity entity) {
		this.slot.init(this, entity.xOld, entity.yOld, entity.zOld).remove(entity);
		this.all.remove(entity);
	}

	public void moved(Entity entity) {
		Slot old = this.slot.init(this, entity.xOld, entity.yOld, entity.zOld);
		Slot newSlot = this.slot2.init(this, entity.x, entity.y, entity.z);
		if (!old.equals(newSlot)) {
			old.remove(entity);
			newSlot.add(entity);
			entity.xOld = entity.x;
			entity.yOld = entity.y;
			entity.zOld = entity.z;
		}
	}

	public List<Entity> getEntities(Entity exclude, float x, float y, float z, float x2, float y2, float z2) {
		this.tmp.clear();
		return this.getEntities(exclude, x, y, z, x2, y2, z2, this.tmp);
	}

	public List<Entity> getEntities(Entity exclude, float x, float y, float z, float x2, float y2, float z2, List<Entity> result) {
		Slot slot = this.slot.init(this, x, y, z);
		Slot slot2 = this.slot2.init(this, x2, y2, z2);
		for (int ex = slot.xSlot - 1; ex <= slot2.xSlot + 1; ex++) {
			for (int ey = slot.ySlot - 1; ey <= slot2.ySlot + 1; ey++) {
				for (int ez = slot.zSlot - 1; ez <= slot2.zSlot + 1; ez++) {
					if (ex >= 0 && ey >= 0 && ez >= 0 && ex < this.width && ey < this.height && ez < this.depth) {
						List<Entity> entities = this.entityGrid[(ez * this.height + ey) * this.width + ex];
						for (Entity entity : entities) {
							if (entity != exclude && entity.intersects(x, y, z, x2, y2, z2)) {
								result.add(entity);
							}
						}
					}
				}
			}
		}

		return result;
	}

	public void removeAllNonCreativeModeEntities() {
		List<Entity> cache = new ArrayList<Entity>();
		
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				for (int z = 0; z < this.depth; z++) {
					List<Entity> entities = this.entityGrid[(z * this.height + y) * this.width + x];
					cache.addAll(entities);
					
					for (Entity entity : cache) {
						if (!entity.isCreativeModeAllowed()) {
							entities.remove(entity);
						}
					}
					
					cache.clear();
				}
			}
		}

		cache.addAll(this.all);
		for(Entity entity : cache) {
			if(!entity.isCreativeModeAllowed()) {
				this.all.remove(entity);
			}
		}
	}

	public void clear() {
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				for (int z = 0; z < this.depth; z++) {
					this.entityGrid[(z * this.height + y) * this.width + x].clear();
				}
			}
		}
	}

	public List<Entity> getEntities(Entity exclude, AABB bb) {
		this.tmp.clear();
		return this.getEntities(exclude, bb.x0, bb.y0, bb.z0, bb.x1, bb.y1, bb.z1, this.tmp);
	}

	public List<Entity> getEntities(Entity exclude, AABB bb, List<Entity> to) {
		return this.getEntities(exclude, bb.x0, bb.y0, bb.z0, bb.x1, bb.y1, bb.z1, to);
	}

	public void tickAll() {
		for (int index = 0; index < this.all.size(); index++) {
			Entity entity = this.all.get(index);
			entity.tick();
			if (entity.removed) {
				this.all.remove(index--);
				this.slot.init(this, entity.xOld, entity.yOld, entity.zOld).remove(entity);
			} else {
				int omx = (int) (entity.xOld / 16.0F);
				int omy = (int) (entity.yOld / 16.0F);
				int omz = (int) (entity.zOld / 16.0F);
				int mx = (int) (entity.x / 16.0F);
				int my = (int) (entity.y / 16.0F);
				int mz = (int) (entity.z / 16.0F);
				if (omx != mx || omy != my || omz != mz) {
					this.moved(entity);
				}
			}
		}

	}

	public void render(Vector pos, TextureManager textureManager, float dt) {
		for (int x = 0; x < this.width; x++) {
			float x1 = (x << 4) - 2;
			float x2 = (x + 1 << 4) + 2;
			for (int y = 0; y < this.height; y++) {
				float y1 = (y << 4) - 2;
				float y2 = (y + 1 << 4) + 2;
				for (int z = 0; z < this.depth; z++) {
					List<Entity> entities = this.entityGrid[(z * this.height + y) * this.width + x];
					if (entities.size() != 0) {
						float z1 = (z << 4) - 2;
						float z2 = (z + 1 << 4) + 2;
						if (Frustum.isBoxInFrustum(x1, y1, z1, x2, y2, z2)) {
							int plane = 0;
							boolean empty = true;
							while (true) {
								if (plane >= 6) {
									empty = true;
									break;
								}

								if (Frustum.frustum[plane][0] * x1 + Frustum.frustum[plane][1] * y1 + Frustum.frustum[plane][2] * y1 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if (Frustum.frustum[plane][0] * x2 + Frustum.frustum[plane][1] * y1 + Frustum.frustum[plane][2] * y1 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if (Frustum.frustum[plane][0] * x1 + Frustum.frustum[plane][1] * y2 + Frustum.frustum[plane][2] * y1 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if (Frustum.frustum[plane][0] * x2 + Frustum.frustum[plane][1] * y2 + Frustum.frustum[plane][2] * y1 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if (Frustum.frustum[plane][0] * x1 + Frustum.frustum[plane][1] * y1 + Frustum.frustum[plane][2] * z2 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if (Frustum.frustum[plane][0] * x2 + Frustum.frustum[plane][1] * y1 + Frustum.frustum[plane][2] * z2 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if (Frustum.frustum[plane][0] * x1 + Frustum.frustum[plane][1] * y2 + Frustum.frustum[plane][2] * z2 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if (Frustum.frustum[plane][0] * x2 + Frustum.frustum[plane][1] * y2 + Frustum.frustum[plane][2] * z2 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								plane++;
							}

							for (int index = 0; index < entities.size(); index++) {
								Entity entity = entities.get(index);
								if (entity.shouldRender(pos)) {
									if (!empty) {
										AABB aabb = entity.bb;
										if (!Frustum.isBoxInFrustum(aabb.x0, aabb.y0, aabb.z0, aabb.x1, aabb.y1, aabb.z1)) {
											continue;
										}
									}

									entity.render(textureManager, dt);
								}
							}
						}
					}
				}
			}
		}

	}
	
	public static class Slot {
		private BlockMap parent;
		private int xSlot;
		private int ySlot;
		private int zSlot;

		public Slot init(BlockMap parent, float x, float y, float z) {
			this.parent = parent;
			this.xSlot = (int) (x / 16);
			this.ySlot = (int) (y / 16);
			this.zSlot = (int) (z / 16);
			if (this.xSlot < 0) {
				this.xSlot = 0;
			}

			if (this.ySlot < 0) {
				this.ySlot = 0;
			}

			if (this.zSlot < 0) {
				this.zSlot = 0;
			}

			if (this.xSlot >= parent.width) {
				this.xSlot = parent.width - 1;
			}

			if (this.ySlot >= parent.height) {
				this.ySlot = parent.height - 1;
			}

			if (this.zSlot >= parent.depth) {
				this.zSlot = parent.depth - 1;
			}

			return this;
		}

		public void add(Entity entity) {
			if (this.xSlot >= 0 && this.ySlot >= 0 && this.zSlot >= 0) {
				parent.entityGrid[(this.zSlot * parent.height + this.ySlot) * parent.width + this.xSlot].add(entity);
			}

		}

		public void remove(Entity entity) {
			if (this.xSlot >= 0 && this.ySlot >= 0 && this.zSlot >= 0) {
				parent.entityGrid[(this.zSlot * parent.height + this.ySlot) * parent.width + this.xSlot].remove(entity);
			}
		}
	}
}
