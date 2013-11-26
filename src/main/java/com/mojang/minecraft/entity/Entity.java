package com.mojang.minecraft.entity;

import java.util.ArrayList;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.math.BoundingBox;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.game.util.InternalConstants;

import com.mojang.minecraft.entity.model.Vector;
import com.mojang.minecraft.entity.player.LocalPlayer;
import com.mojang.minecraft.entity.player.net.PositionUpdate;
import com.mojang.minecraft.level.BlockMap;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.render.TextureManager;

public abstract class Entity {

	public Level level;
	public float xo;
	public float yo;
	public float zo;
	public float x;
	public float y;
	public float z;
	public float xd;
	public float yd;
	public float zd;
	public float yaw;
	public float pitch;
	public float oYaw;
	public float oPitch;
	public BoundingBox bb;
	public boolean onGround = false;
	public boolean horizontalCollision = false;
	public boolean collision = false;
	public boolean slide = true;
	public boolean removed = false;
	public float heightOffset = 0;
	public float bbWidth = 0.6F;
	public float bbHeight = 1.8F;
	public float walkDistO = 0;
	public float walkDist = 0;
	public boolean makeStepSound = true;
	public float fallDistance = 0;
	private int nextStep = 1;
	public BlockMap blockMap;
	public float xOld;
	public float yOld;
	public float zOld;
	public int textureId = 0;
	public float ySlideOffset = 0;
	public float footSize = 0;
	public boolean noPhysics = false;
	public float pushthrough = 0;
	public boolean hovered = false;

	public Entity(Level level) {
		this.level = level;
		this.setPos(0, 0, 0);
	}

	public void resetPos() {
		this.resetPos(null);
	}

	public void resetPos(Position pos) {
		if(pos != null) {
			pos = pos.clone();
			while(pos.getY() < this.level.height && this.level.getCubes(this.bb).size() != 0) {
				pos.setY(pos.getY() + 1);
			}

			this.setPos(pos.getX(), pos.getY(), pos.getZ());
			this.xd = 0;
			this.yd = 0;
			this.zd = 0;
			this.yaw = pos.getYaw();
			this.pitch = pos.getPitch();
		} else if(this.level != null) {
			float x = this.level.xSpawn + 0.5F;
			float y = this.level.ySpawn;
			float z = this.level.zSpawn + 0.5F;
			while(y < this.level.height) {
				this.setPos(x, y, z);
				if(this.level.getCubes(this.bb).size() == 0) {
					break;
				}

				y++;
			}

			this.xd = 0;
			this.yd = 0;
			this.zd = 0;
			this.yaw = this.level.yawSpawn;
			this.pitch = this.level.pitchSpawn;
		}
	}

	public void remove() {
		this.removed = true;
	}

	public void setSize(float width, float height) {
		this.bbWidth = width;
		this.bbHeight = height;
	}

	public void setPos(PositionUpdate pos) {
		if(pos.position) {
			this.setPos(pos.x, pos.y, pos.z);
		} else {
			this.setPos(this.x, this.y, this.z);
		}

		if(pos.rotation) {
			this.setRot(pos.yaw, pos.pitch);
		} else {
			this.setRot(this.yaw, this.pitch);
		}
	}

	protected void setRot(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public void setPos(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		float widthCenter = this.bbWidth / 2;
		float heightCenter = this.bbHeight / 2;
		this.bb = new BoundingBox(x - widthCenter, y - heightCenter, z - widthCenter, x + widthCenter, y + heightCenter, z + widthCenter);
	}

	public void turn(float yaw, float pitch) {
		float oldPitch = this.pitch;
		float oldYaw = this.yaw;
		this.yaw = (float) (this.yaw + yaw * InternalConstants.SENSITIVITY_VALUE[1]);
		this.pitch = (float) (this.pitch - pitch * InternalConstants.SENSITIVITY_VALUE[1]);
		if(this.pitch < -90) {
			this.pitch = -90;
		}

		if(this.pitch > 90) {
			this.pitch = 90;
		}

		this.oPitch += this.pitch - oldPitch;
		this.oYaw += this.yaw - oldYaw;
	}

	public void interpolateTurn(float yaw, float pitch) {
		this.yaw = (float) (this.yaw + yaw * InternalConstants.SENSITIVITY_VALUE[1]);
		this.pitch = (float) (this.pitch - pitch * InternalConstants.SENSITIVITY_VALUE[1]);
		if(this.pitch < -90) {
			this.pitch = -90;
		}

		if(this.pitch > 90) {
			this.pitch = 90;
		}
	}

	public void tick() {
		this.walkDistO = this.walkDist;
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.oPitch = this.pitch;
		this.oYaw = this.yaw;
	}

	public boolean isFree(float x, float y, float z, float radius) {
		BoundingBox grown = this.bb.grow(radius, radius, radius).cloneMove(x, y, z);
		return this.level.getCubes(grown).size() <= 0 || !this.level.containsAnyLiquid(grown);
	}

	public boolean isFree(float x, float y, float z) {
		BoundingBox moved = this.bb.cloneMove(x, y, z);
		return this.level.getCubes(moved).size() <= 0 || !this.level.containsAnyLiquid(moved);
	}

	public void move(float x, float y, float z) {
		if(this.noPhysics) {
			this.bb.move(x, y, z);
			this.x = (this.bb.getX1() + this.bb.getX2()) / 2;
			this.y = this.bb.getY1() + this.heightOffset - this.ySlideOffset;
			this.z = (this.bb.getZ1() + this.bb.getZ2()) / 2;
		} else {
			float oldEntityX = this.x;
			float oldEntityZ = this.z;
			float oldX = x;
			float oldY = y;
			float oldZ = z;
			BoundingBox copy = this.bb.clone();
			ArrayList<BoundingBox> cubes = this.level.getCubes(this.bb.expand(x, y, z));
			for(BoundingBox cube : cubes) {
				y = cube.clipYCollide(this.bb, y);
			}

			this.bb.move(0, y, 0);
			if(!this.slide && oldY != y) {
				x = 0;
				y = 0;
				z = 0;
			}

			boolean stepFurther = this.onGround || oldY != y && oldY < 0;
			for(BoundingBox cube : cubes) {
				x = cube.clipXCollide(this.bb, x);
			}

			this.bb.move(x, 0, 0);
			if(!this.slide && oldX != x) {
				z = 0.0F;
				y = 0.0F;
				x = 0.0F;
			}

			for(BoundingBox cube : cubes) {
				z = cube.clipZCollide(this.bb, z);
			}

			this.bb.move(0, 0, z);
			if(!this.slide && oldZ != z) {
				x = 0;
				y = 0;
				z = 0;
			}

			if(this.footSize > 0 && stepFurther && this.ySlideOffset < 0.05F && (oldX != x || oldZ != z)) {
				float newX = x;
				float newY = y;
				float newZ = z;
				x = oldX;
				y = this.footSize;
				z = oldZ;
				BoundingBox newCopy = this.bb.clone();
				this.bb = copy.clone();
				cubes = this.level.getCubes(this.bb.expand(oldX, y, oldZ));

				for(BoundingBox cube : cubes) {
					y = cube.clipYCollide(this.bb, y);
				}

				this.bb.move(0, y, 0);
				if(!this.slide && oldY != y) {
					z = 0;
					y = 0;
					x = 0;
				}

				for(BoundingBox cube : cubes) {
					x = cube.clipXCollide(this.bb, x);
				}

				this.bb.move(x, 0, 0);
				if(!this.slide && oldX != x) {
					z = 0;
					y = 0;
					x = 0;
				}

				for(BoundingBox cube : cubes) {
					z = cube.clipZCollide(this.bb, z);
				}

				this.bb.move(0, 0, z);
				if(!this.slide && oldZ != z) {
					z = 0;
					y = 0;
					x = 0;
				}

				if(newX * newX + newZ * newZ >= x * x + z * z) {
					x = newX;
					y = newY;
					z = newZ;
					this.bb = newCopy.clone();
				} else {
					this.ySlideOffset = (float) (this.ySlideOffset + 0.5D);
				}
			}

			this.horizontalCollision = oldX != x || oldZ != z;
			this.onGround = oldY != y && oldY < 0;
			this.collision = this.horizontalCollision || oldY != y;
			if(this.onGround) {
				if(this.fallDistance > 0) {
					this.causeFallDamage(this.fallDistance);
					this.fallDistance = 0;
				}
			} else if(y < 0.0F) {
				this.fallDistance -= y;
			}

			if(oldX != x) {
				this.xd = 0;
			}

			if(oldY != y) {
				this.yd = 0;
			}

			if(oldZ != z) {
				this.zd = 0;
			}

			this.x = (this.bb.getX1() + this.bb.getX2()) / 2;
			this.y = this.bb.getY1() + this.heightOffset - this.ySlideOffset;
			this.z = (this.bb.getZ1() + this.bb.getZ2()) / 2;
			float xDiff = this.x - oldEntityX;
			float zDiff = this.z - oldEntityZ;
			if(this.onGround) {
				this.walkDist = (float) (this.walkDist + (float) Math.sqrt(xDiff * xDiff + zDiff * zDiff) * 0.6D);
				if(this.makeStepSound) {
					int id = this.level.getTile((int) this.x, (int) (this.y - 0.2F - this.heightOffset), (int) this.z);
					if(this.walkDist > this.nextStep && id > 0) {
						this.nextStep++;
						if(Blocks.fromId(id) != null) {
							StepSound step = Blocks.fromId(id).getStepSound();
							if(step != StepSound.NONE) {
								this.playSound(step.getSound(), step.getVolume() * 0.75F, step.getPitch());
							}
						}
					}
				}
			}

			this.ySlideOffset *= 0.4F;
		}
	}

	protected void causeFallDamage(float distance) {
	}
	
	public BlockType getLiquid() {
		return this.level.getLiquid(this.bb.grow(0, -0.4F, 0));
	}
	
	public BlockType getBlockIn() {
		return this.level.getBlockIn(this.bb.grow(0, -0.4F, 0));
	}

	public boolean isUnderWater() {
		int block = this.level.getTile((int) this.x, (int) (this.y + 0.12F), (int) this.z);
		return block != 0 && (Blocks.fromId(block) == VanillaBlock.WATER || Blocks.fromId(block) == VanillaBlock.STATIONARY_WATER);
	}

	public void moveHeading(float forward, float strafe, float speed) {
		float len = (float) Math.sqrt(forward * forward + strafe * strafe);
		if(len >= 0.01F) {
			if(len < 1) {
				len = 1;
			}

			float mforward = forward * (speed / len);
			float mstrafe = strafe * (speed / len);
			float xangle = MathHelper.cos(this.yaw * MathHelper.DEG_TO_RAD);
			float zangle = MathHelper.sin(this.yaw * MathHelper.DEG_TO_RAD);

			this.xd += mforward * xangle - mstrafe * zangle;
			this.zd += mstrafe * xangle + mforward * zangle;
		}
	}

	public boolean isLit() {
		return this.level.isLit((int) this.x, (int) this.y, (int) this.z);
	}

	public float getBrightness(float dt) {
		int y = (int) (this.y + this.heightOffset / 2 - 0.5F);
		return this.level.getBrightness((int) this.x, y, (int) this.z);
	}

	public void render(TextureManager textures, float dt) {
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public void playSound(String sound, float volume, float pitch) {
		if(this.distanceToSqr(this.level.minecraft.player) < 1024) {
			this.level.minecraft.audio.playSound(sound, this.x, this.y, this.z, volume, pitch);
		}
	}

	public void moveTo(float x, float y, float z, float yaw, float pitch) {
		this.xo = x;
		this.x = x;
		this.yo = y;
		this.y = y;
		this.zo = z;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.setPos(x, y, z);
	}

	public float distanceTo(Entity other) {
		float xDistance = this.x - other.x;
		float yDistance = this.y - other.y;
		float zDistance = this.z - other.z;
		return (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
	}

	public float distanceTo(float x, float y, float z) {
		float xDistance = this.x - x;
		float yDistance = this.y - y;
		float zDistance = this.z - z;
		return (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
	}

	public float distanceToSqr(Entity other) {
		float xDistance = this.x - other.x;
		float yDistance = this.y - other.y;
		float zDistance = this.z - other.z;
		return xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
	}

	public void playerTouch(LocalPlayer player) {
	}

	public void push(Entity entity) {
		float xDiff = entity.x - this.x;
		float zDiff = entity.z - this.z;
		float sqXZDiff = xDiff * xDiff + zDiff * zDiff;
		if(sqXZDiff >= 0.01F) {
			float xzDiff = (float) Math.sqrt(sqXZDiff);
			xDiff /= xzDiff;
			zDiff /= xzDiff;
			xDiff /= xzDiff;
			zDiff /= xzDiff;
			xDiff *= 0.05F;
			zDiff *= 0.05F;
			xDiff *= 1 - this.pushthrough;
			zDiff *= 1 - this.pushthrough;
			this.push(-xDiff, 0, -zDiff);
			entity.push(xDiff, 0, zDiff);
		}
	}

	protected void push(float x, float y, float z) {
		this.xd += x;
		this.yd += y;
		this.zd += z;
	}

	public void hurt(Entity cause, int damage) {
	}

	public boolean intersects(float x1, float y1, float z1, float x2, float y2, float z2) {
		return this.bb.intersects(x1, y1, z1, x2, y2, z2);
	}

	public boolean isPickable() {
		return false;
	}

	public boolean isPushable() {
		return false;
	}

	public boolean isShootable() {
		return false;
	}

	public void awardKillScore(Entity entity, int amount) {
	}

	public boolean shouldRender(Vector point) {
		float x = this.x - point.x;
		float y = this.y - point.y;
		float z = this.z - point.z;
		float sqDistance = x * x + y * y + z * z;
		return this.shouldRenderAtSqrDistance(sqDistance);
	}

	public boolean shouldRenderAtSqrDistance(float sqDistance) {
		float size = this.bb.getSize() * 64;
		return sqDistance < size * size;
	}

	public int getTexture() {
		return this.textureId;
	}

	public boolean isCreativeModeAllowed() {
		return false;
	}

	public void renderHover(TextureManager textures, float dt) {
	}
}
