package ch.spacebase.openclassic.client.level;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.math.BoundingBox;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.model.Vector;
import com.mojang.minecraft.render.Frustum;
import com.mojang.minecraft.render.TextureManager;

public class BlockMap {

	private int width;
	private int height;
	private int depth;
	private BlockMapSlot slot = new BlockMapSlot();
	private BlockMapSlot slot2 = new BlockMapSlot();
	public List<Entity>[] entityGrid;
	public List<Entity> all = new ArrayList<Entity>();
	private List<Entity> tmp = new ArrayList<Entity>();

	@SuppressWarnings("unchecked")
	public BlockMap(int width, int height, int depth) {
		this.width = width / 16;
		this.height = height / 16;
		this.depth = depth / 16;
		if(this.width == 0) {
			this.width = 1;
		}

		if(this.height == 0) {
			this.height = 1;
		}

		if(this.depth == 0) {
			this.depth = 1;
		}

		this.entityGrid = new ArrayList[this.width * this.height * this.depth];
		for(int x = 0; x < this.width; x++) {
			for(int y = 0; y < this.height; y++) {
				for(int z = 0; z < this.depth; z++) {
					this.entityGrid[(z * this.height + y) * this.width + x] = new ArrayList<Entity>();
				}
			}
		}
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public int getDepth() {
		return this.depth;
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
		BlockMapSlot old = this.slot.init(this, entity.xOld, entity.yOld, entity.zOld);
		BlockMapSlot newSlot = this.slot2.init(this, entity.x, entity.y, entity.z);
		if(!old.equals(newSlot)) {
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
		BlockMapSlot slot = this.slot.init(this, x, y, z);
		BlockMapSlot slot2 = this.slot2.init(this, x2, y2, z2);
		for(int ex = slot.getX() - 1; ex <= slot2.getX() + 1; ex++) {
			for(int ey = slot.getY() - 1; ey <= slot2.getY() + 1; ey++) {
				for(int ez = slot.getZ() - 1; ez <= slot2.getZ() + 1; ez++) {
					if(ex >= 0 && ey >= 0 && ez >= 0 && ex < this.width && ey < this.height && ez < this.depth) {
						List<Entity> entities = this.entityGrid[(ez * this.height + ey) * this.width + ex];
						for(Entity entity : entities) {
							if(entity != exclude && entity.intersects(x, y, z, x2, y2, z2)) {
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
		for(int x = 0; x < this.width; x++) {
			for(int y = 0; y < this.height; y++) {
				for(int z = 0; z < this.depth; z++) {
					List<Entity> entities = this.entityGrid[(z * this.height + y) * this.width + x];
					cache.addAll(entities);
					for(Entity entity : cache) {
						if(!entity.isCreativeModeAllowed()) {
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
		for(int x = 0; x < this.width; x++) {
			for(int y = 0; y < this.height; y++) {
				for(int z = 0; z < this.depth; z++) {
					this.entityGrid[(z * this.height + y) * this.width + x].clear();
				}
			}
		}
	}

	public List<Entity> getEntities(Entity exclude, BoundingBox bb) {
		this.tmp.clear();
		return this.getEntities(exclude, bb.getX1(), bb.getY1(), bb.getZ1(), bb.getX2(), bb.getY2(), bb.getZ2(), this.tmp);
	}

	public List<Entity> getEntities(Entity exclude, BoundingBox bb, List<Entity> to) {
		return this.getEntities(exclude, bb.getX1(), bb.getY1(), bb.getZ1(), bb.getX2(), bb.getY2(), bb.getZ2(), to);
	}

	public void tickAll() {
		for(int index = 0; index < this.all.size(); index++) {
			Entity entity = this.all.get(index);
			entity.tick();
			if(entity.removed) {
				this.all.remove(index--);
				this.slot.init(this, entity.xOld, entity.yOld, entity.zOld).remove(entity);
			} else {
				int omx = (int) (entity.xOld / 16.0F);
				int omy = (int) (entity.yOld / 16.0F);
				int omz = (int) (entity.zOld / 16.0F);
				int mx = (int) (entity.x / 16.0F);
				int my = (int) (entity.y / 16.0F);
				int mz = (int) (entity.z / 16.0F);
				if(omx != mx || omy != my || omz != mz) {
					this.moved(entity);
				}
			}
		}

	}

	public void render(Vector pos, TextureManager textureManager, float dt) {
		for(int x = 0; x < this.width; x++) {
			float x1 = (x << 4) - 2;
			float x2 = (x + 1 << 4) + 2;
			for(int y = 0; y < this.height; y++) {
				float y1 = (y << 4) - 2;
				float y2 = (y + 1 << 4) + 2;
				for(int z = 0; z < this.depth; z++) {
					List<Entity> entities = this.entityGrid[(z * this.height + y) * this.width + x];
					if(entities.size() != 0) {
						float z1 = (z << 4) - 2;
						float z2 = (z + 1 << 4) + 2;
						if(Frustum.isBoxInFrustum(x1, y1, z1, x2, y2, z2)) {
							int plane = 0;
							boolean empty = true;
							while(true) {
								if(plane >= 6) {
									empty = true;
									break;
								}

								if(Frustum.frustum[plane][0] * x1 + Frustum.frustum[plane][1] * y1 + Frustum.frustum[plane][2] * y1 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if(Frustum.frustum[plane][0] * x2 + Frustum.frustum[plane][1] * y1 + Frustum.frustum[plane][2] * y1 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if(Frustum.frustum[plane][0] * x1 + Frustum.frustum[plane][1] * y2 + Frustum.frustum[plane][2] * y1 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if(Frustum.frustum[plane][0] * x2 + Frustum.frustum[plane][1] * y2 + Frustum.frustum[plane][2] * y1 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if(Frustum.frustum[plane][0] * x1 + Frustum.frustum[plane][1] * y1 + Frustum.frustum[plane][2] * z2 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if(Frustum.frustum[plane][0] * x2 + Frustum.frustum[plane][1] * y1 + Frustum.frustum[plane][2] * z2 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if(Frustum.frustum[plane][0] * x1 + Frustum.frustum[plane][1] * y2 + Frustum.frustum[plane][2] * z2 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								if(Frustum.frustum[plane][0] * x2 + Frustum.frustum[plane][1] * y2 + Frustum.frustum[plane][2] * z2 + Frustum.frustum[plane][3] <= 0.0F) {
									empty = false;
									break;
								}

								plane++;
							}

							for(int index = 0; index < entities.size(); index++) {
								Entity entity = entities.get(index);
								if(entity.shouldRender(pos)) {
									if(!empty) {
										BoundingBox bb = entity.bb;
										if(!Frustum.isBoxInFrustum(bb.getX1(), bb.getY1(), bb.getZ1(), bb.getX2(), bb.getY2(), bb.getZ2())) {
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
	
}
