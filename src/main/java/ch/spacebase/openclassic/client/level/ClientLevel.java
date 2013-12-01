package ch.spacebase.openclassic.client.level;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.item.PrimedTnt;
import com.mojang.minecraft.entity.model.Vector;
import com.mojang.minecraft.util.Intersection;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.math.BoundingBox;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.util.BlockUtils;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.level.ClassicLevel;

public class ClientLevel extends ClassicLevel {

	private BlockMap blockMap;

	public ClientLevel() {
		super();
		if(OpenClassic.getClient().getSettings().getBooleanSetting("options.night").getValue()) {
			this.setSkyColor(0);
			this.setFogColor(new Color(30, 30, 30, 70).getRGB());
			this.setCloudColor(new Color(30, 30, 30, 70).getRGB());
		}
	}
	
	public ClientLevel(LevelInfo info) {
		super(info);
		if(OpenClassic.getClient().getSettings().getBooleanSetting("options.night").getValue()) {
			this.setSkyColor(0);
			this.setFogColor(new Color(30, 30, 30, 70).getRGB());
			this.setCloudColor(new Color(30, 30, 30, 70).getRGB());
		}
	}

	@Override
	public void setData(int width, int height, int depth, byte blocks[]) {
		super.setData(width, height, depth, blocks);
		this.blockMap = new BlockMap(width, height, depth);
		GeneralUtils.getMinecraft().levelRenderer.refresh();
	}

	@Override
	protected void highestUpdated(int x, int z, int lower, int highest) {
		GeneralUtils.getMinecraft().levelRenderer.queueChunks(x - 1, lower - 1, z - 1, x + 1, highest + 1, z + 1);
	}

	@Override
	public List<Player> getPlayers() {
		List<Player> result = new ArrayList<Player>();
		for(com.mojang.minecraft.entity.player.Player p : this.findAll(com.mojang.minecraft.entity.player.Player.class)) {
			result.add(p.openclassic);
		}

		return result;
	}

	@Override
	public boolean setBlockIdAt(int x, int y, int z, byte type, boolean physics) {
		boolean ret = super.setBlockIdAt(x, y, z, type, physics && !OpenClassic.getClient().isInMultiplayer());
		GeneralUtils.getMinecraft().levelRenderer.queueChunks(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
		return ret;
	}

	@Override
	public float getBrightness(int x, int y, int z) {
		float brightness = super.getBrightness(x, y, z);
		BlockType block = this.getBlockTypeAt(x, y, z);
		if(block != null && block.getBrightness() <= 0) {
			brightness -= OpenClassic.getClient().getSettings().getBooleanSetting("options.night").getValue() ? 0.4f : 0;
		}

		return brightness;
	}

	// Client methods
	public ArrayList<BoundingBox> getCubes(BoundingBox bb) {
		ArrayList<BoundingBox> ret = new ArrayList<BoundingBox>();
		int x0 = (int) bb.getX1();
		int x1 = (int) bb.getX2() + 1;
		int y0 = (int) bb.getY1();
		int y1 = (int) bb.getY2() + 1;
		int z0 = (int) bb.getZ1();
		int z1 = (int) bb.getZ2() + 1;
		if(bb.getX1() < 0) {
			x0--;
		}

		if(bb.getY1() < 0) {
			y0--;
		}

		if(bb.getZ1() < 0) {
			z0--;
		}

		for(int x = x0; x < x1; x++) {
			for(int y = y0; y < y1; y++) {
				for(int z = z0; z < z1; z++) {
					if(x >= 0 && y >= 0 && z >= 0 && x < this.getWidth() && y < this.getHeight() && z < this.getDepth()) {
						BlockType type = this.getBlockTypeAt(x, y, z);
						if(type != null) {
							BoundingBox bb2 = type.getModel(this, x, y, z).getCollisionBox(x, y, z);
							if(type != null && bb2 != null && bb.intersectsInner(bb2)) {
								ret.add(bb2);
							}
						}
					} else if(x < 0 || y < 0 || z < 0 || x >= this.getWidth() || z >= this.getDepth()) {
						BoundingBox bb2 = VanillaBlock.BEDROCK.getModel(this, x, y, z).getCollisionBox(x, y, z);
						if(bb2 != null && bb.intersectsInner(bb2)) {
							ret.add(bb2);
						}
					}
				}
			}
		}

		return ret;
	}

	public BlockType getLiquid(BoundingBox bb) {
		int x0 = (int) bb.getX1();
		int x1 = (int) bb.getX2() + 1;
		int y0 = (int) bb.getY1();
		int y1 = (int) bb.getY2() + 1;
		int z0 = (int) bb.getZ1();
		int z1 = (int) bb.getZ2() + 1;
		if(bb.getX1() < 0) {
			x0--;
		}

		if(bb.getY1() < 0) {
			y0--;
		}

		if(bb.getZ1() < 0) {
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

		if(x1 > this.getWidth()) {
			x1 = this.getWidth();
		}

		if(y1 > this.getHeight()) {
			y1 = this.getHeight();
		}

		if(z1 > this.getDepth()) {
			z1 = this.getDepth();
		}

		for(int x = x0; x < x1; x++) {
			for(int y = y0; y < y1; y++) {
				for(int z = z0; z < z1; z++) {
					BlockType type = this.getBlockTypeAt(x, y, z);
					if(type != null && type.isLiquid()) {
						return type;
					}
				}
			}
		}

		return null;
	}

	public BlockType getBlockIn(BoundingBox bb) {
		int x0 = (int) bb.getX1();
		int x1 = (int) bb.getX2() + 1;
		int y0 = (int) bb.getY1();
		int y1 = (int) bb.getY2() + 1;
		int z0 = (int) bb.getZ1();
		int z1 = (int) bb.getZ2() + 1;
		if(bb.getX1() < 0) {
			x0--;
		}

		if(bb.getY1() < 0) {
			y0--;
		}

		if(bb.getZ1() < 0) {
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

		if(x1 > this.getWidth()) {
			x1 = this.getWidth();
		}

		if(y1 > this.getHeight()) {
			y1 = this.getHeight();
		}

		if(z1 > this.getDepth()) {
			z1 = this.getDepth();
		}

		for(int x = x0; x < x1; x++) {
			for(int y = y0; y < y1; y++) {
				for(int z = z0; z < z1; z++) {
					BlockType type = this.getBlockTypeAt(x, y, z);
					if(type != null) {
						return type;
					}
				}
			}
		}

		return null;
	}

	public boolean containsAnyLiquid(BoundingBox bb) {
		int x0 = (int) bb.getX1();
		int x1 = (int) bb.getX2() + 1;
		int y0 = (int) bb.getY1();
		int y1 = (int) bb.getY2() + 1;
		int z0 = (int) bb.getZ1();
		int z1 = (int) bb.getZ2() + 1;
		if(bb.getX1() < 0) {
			x0--;
		}

		if(bb.getY1() < 0) {
			y0--;
		}

		if(bb.getZ1() < 0) {
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

		if(x1 > this.getWidth()) {
			x1 = this.getWidth();
		}

		if(y1 > this.getHeight()) {
			y1 = this.getHeight();
		}

		if(z1 > this.getDepth()) {
			z1 = this.getDepth();
		}

		for(int x = x0; x < x1; x++) {
			for(int y = y0; y < y1; y++) {
				for(int z = z0; z < z1; z++) {
					BlockType type = this.getBlockTypeAt(x, y, z);
					if(type != null && type.isLiquid()) {
						return true;
					}
				}
			}
		}

		return false;
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

					Vector vec = new Vector(vec1.x, vec1.y, vec1.z);
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

					BlockType type = this.getBlockTypeAt(bx1, by1, bz1);
					if(type != null && type.getId() != 0 && (type != null && !type.isLiquid())) {
						Intersection clipped = null;
						if(selection) {
							clipped = BlockUtils.clipSelection(type.getId(), this, bx1, by1, bz1, vec1, vec2);
						} else {
							clipped = BlockUtils.clip(type.getId(), this, bx1, by1, bz1, vec1, vec2);
						}

						if(type.getModel(this, bx1, by1, bz1).getCollisionBox(bx1, by1, bz1) != null) {
							if(clipped != null) {
								return clipped;
							}
						} else {
							if(selection) {
								clipped = BlockUtils.clipSelection(type.getId(), this, bx1, by1, bz1, vec1, vec2);
							} else {
								clipped = BlockUtils.clip(type.getId(), this, bx1, by1, bz1, vec1, vec2);
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

	public void tickEntities() {
		this.blockMap.tickAll();
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

	public boolean isFree(BoundingBox bb) {
		return this.blockMap.getEntities(null, bb).size() == 0;
	}

	public List<Entity> findEntities(Entity entity, BoundingBox bb) {
		return this.blockMap.getEntities(entity, bb);
	}

	public void addEntity(Entity entity) {
		this.blockMap.insert(entity);
		entity.setLevel(this);
	}

	public void removeEntity(Entity entity) {
		this.blockMap.remove(entity);
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> List<T> findAll(Class<T> clazz) {
		List<T> ret = new ArrayList<T>();
		for(Entity entity : this.blockMap.all) {
			if(clazz.isAssignableFrom(entity.getClass())) {
				ret.add((T) entity);
			}
		}

		return ret;
	}

	public void removeAllNonCreativeModeEntities() {
		this.blockMap.removeAllNonCreativeModeEntities();
	}

	public void explode(Entity entity, float x, float y, float z, float power) {
		if(OpenClassic.getClient().isInMultiplayer()) {
			return;
		}

		int minx = (int) (x - power - 1.0F);
		int maxx = (int) (x + power + 1.0F);
		int miny = (int) (y - power - 1.0F);
		int maxy = (int) (y + power + 1.0F);
		int minz = (int) (z - power - 1.0F);
		int maxz = (int) (z + power + 1.0F);
		OpenClassic.getGame().getAudioManager().playSound("random.explode", x, y, z, 1, 1);
		for(int bx = minx; bx < maxx; bx++) {
			for(int by = maxy - 1; by >= miny; by--) {
				for(int bz = minz; bz < maxz; bz++) {
					float dx = bx + 0.5F - x;
					float dy = by + 0.5F - y;
					float dz = bz + 0.5F - z;
					BlockType block = this.getBlockTypeAt(bx, by, bz);
					if(x >= 0 && y >= 0 && z >= 0 && x < this.getWidth() && y < this.getHeight() && z < this.getDepth() && dx * dx + dy * dy + dz * dz < power * power && block != null && BlockUtils.canExplode(block)) {
						this.setBlockAt(bx, by, bz, VanillaBlock.AIR);
						BlockUtils.dropItems(block, this, bx, by, bz, 0.3F);
						if(block == VanillaBlock.TNT && OpenClassic.getClient().isInSurvival()) {
							PrimedTnt tnt = new PrimedTnt(this, bx + 0.5F, by + 0.5F, bz + 0.5F);
							tnt.life = this.getRandom().nextInt(tnt.life / 4) + tnt.life / 8;
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

	public BlockMap getBlockMap() {
		return this.blockMap;
	}

}
