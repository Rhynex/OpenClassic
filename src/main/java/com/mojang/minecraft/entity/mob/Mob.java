package com.mojang.minecraft.entity.mob;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.api.render.RenderHelper;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.mob.ai.AI;
import com.mojang.minecraft.entity.mob.ai.BasicAI;
import com.mojang.minecraft.entity.model.ModelManager;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.render.TextureManager;

public class Mob extends Entity {

	public static final int ATTACK_DURATION = 5;
	public static final int TOTAL_AIR_SUPPLY = 300;
	public static final ModelManager modelCache = new ModelManager();
	public int invulnerableDuration = 20;
	public float rot;
	public float timeOffs;
	public float speed;
	public float rotA = (float) (Math.random() + 1.0D) * 0.01F;
	protected float bodyYaw = 0.0F;
	protected float oBodyYaw = 0.0F;
	protected float oRun;
	protected float run;
	protected float animStep;
	protected float animStepO;
	protected int tickCount = 0;
	public boolean hasHair = true;
	protected String textureName = "/char.png";
	public boolean allowAlpha = true;
	public float rotOffs = 0.0F;
	public String modelName = null;
	protected float bobStrength = 1.0F;
	protected int deathScore = 0;
	public float renderOffset = 0.0F;
	public int health = 20;
	public int lastHealth;
	public int invulnerableTime = 0;
	public int airSupply = 300;
	public int hurtTime;
	public int hurtDuration;
	public float hurtDir = 0.0F;
	public int deathTime = 0;
	public int attackTime = 0;
	public float oTilt;
	public float tilt;
	public boolean dead = false;
	public AI ai;

	public Mob(Level level) {
		super(level);
		this.setPos(this.x, this.y, this.z);
		this.timeOffs = (float) Math.random() * 12398.0F;
		this.rot = (float) (Math.random() * MathHelper.DTWO_PI);
		this.speed = 1.0F;
		this.ai = new BasicAI();
		this.footSize = 0.5F;
	}

	public boolean isPickable() {
		return !this.removed;
	}

	public boolean isPushable() {
		return !this.removed;
	}

	public final void tick() {
		super.tick();
		this.oTilt = this.tilt;
		if(this.attackTime > 0) {
			this.attackTime--;
		}

		if(this.hurtTime > 0) {
			this.hurtTime--;
		}

		if(this.invulnerableTime > 0) {
			this.invulnerableTime--;
		}

		if(this.health <= 0) {
			this.deathTime++;
			if(this.deathTime > 20) {
				if(this.ai != null) {
					this.ai.beforeRemove();
				}

				this.remove();
			}
		}

		if(this.isUnderWater()) {
			if(this.airSupply > 0) {
				this.airSupply--;
			} else {
				this.hurt(null, 2);
			}
		} else {
			this.airSupply = 300;
		}

		BlockType liquid = this.getLiquid();
		if(liquid != null) {
			this.fallDistance = 0;
			if(liquid.getId() == VanillaBlock.LAVA.getId() || liquid.getId() == VanillaBlock.STATIONARY_LAVA.getId()) {
				this.hurt(null, 10);
			}
		}

		this.animStepO = this.animStep;
		this.oBodyYaw = this.bodyYaw;
		this.oYaw = this.yaw;
		this.oPitch = this.pitch;
		this.tickCount++;
		this.aiStep();
		float xDistance = this.x - this.xo;
		float zDistance = this.z - this.zo;
		float xzDistance = (float) Math.sqrt(xDistance * xDistance + zDistance * zDistance);
		float yaw = this.bodyYaw;
		float animStep = 0.0F;
		this.oRun = this.run;
		float friction = 0.0F;
		if(xzDistance > 0.05F) {
			friction = 1.0F;
			animStep = xzDistance * 3.0F;
		}

		if(!this.onGround) {
			friction = 0.0F;
		}

		this.run += (friction - this.run) * 0.3F;

		float change = yaw - this.bodyYaw;
		while(change < -180.0F) {
			change += 360.0F;
		}

		while(change >= 180) {
			change -= 360;
		}

		this.bodyYaw += change * 0.1;

		change = this.yaw - this.bodyYaw;
		while(change < -180.0F) {
			change += 360.0F;
		}

		while(change >= 180) {
			change -= 360;
		}

		boolean negative = change < -90 || change >= 90;
		if(change < -75) {
			change = -75;
		}

		if(change >= 75) {
			change = 75;
		}

		this.bodyYaw = this.yaw - change;
		this.bodyYaw += change * 0.1F;
		if(negative) {
			animStep = -animStep;
		}

		while(this.yaw - this.oYaw < -180) {
			this.oYaw -= 360;
		}

		while(this.yaw - this.oYaw >= 180) {
			this.oYaw += 360;
		}

		while(this.bodyYaw - this.oBodyYaw < -180) {
			this.oBodyYaw -= 360;
		}

		while(this.bodyYaw - this.oBodyYaw >= 180) {
			this.oBodyYaw += 360;
		}

		while(this.pitch - this.oPitch < -180) {
			this.oPitch -= 360;
		}

		while(this.pitch - this.oPitch >= 180) {
			this.oPitch += 360;
		}

		this.animStep += animStep;
	}

	public void aiStep() {
		if(this.ai != null) {
			this.ai.tick(this.level, this);
		}
	}

	protected void bindTexture(TextureManager textures) {
		this.textureId = RenderHelper.getHelper().bindTexture(this.textureName, true);
	}

	public void render(TextureManager textures, float dt) {
		if(this.modelName != null) {
			float attackTime = this.attackTime - dt;
			if(attackTime < 0.0F) {
				attackTime = 0.0F;
			}

			while(this.oBodyYaw - this.bodyYaw < -180.0F) {
				this.oBodyYaw += 360.0F;
			}

			while(this.oBodyYaw - this.bodyYaw >= 180.0F) {
				this.oBodyYaw -= 360.0F;
			}

			while(this.oPitch - this.pitch < -180.0F) {
				this.oPitch += 360.0F;
			}

			while(this.oPitch - this.pitch >= 180.0F) {
				this.oPitch -= 360.0F;
			}

			while(this.oYaw - this.yaw < -180.0F) {
				this.oYaw += 360.0F;
			}

			while(this.oYaw - this.yaw >= 180.0F) {
				this.oYaw -= 360.0F;
			}

			float bodyYaw = this.oBodyYaw + (this.bodyYaw - this.oBodyYaw) * dt;
			float runProgress = this.oRun + (this.run - this.oRun) * dt;
			float yaw = (this.oYaw + (this.yaw - this.oYaw) * dt) - bodyYaw;
			float pitch = this.oPitch + (this.pitch - this.oPitch) * dt;
			float animStep = this.animStepO + (this.animStep - this.animStepO) * dt;
			float brightness = this.getBrightness(dt);
			float bob = -Math.abs(MathHelper.cos(animStep * 0.6662F)) * 5.0F * runProgress * this.bobStrength - 23.0F;
			GL11.glPushMatrix();
			GL11.glColor3f(brightness, brightness, brightness);
			GL11.glTranslatef(this.xo + (this.x - this.xo) * dt, this.yo + (this.y - this.yo) * dt - 1.62F + this.renderOffset, this.zo + (this.z - this.zo) * dt);
			float hurtRot = this.hurtTime - dt;
			if(hurtRot > 0 || this.health <= 0) {
				if(hurtRot < 0) {
					hurtRot = 0;
				} else {
					hurtRot /= this.hurtDuration;
					hurtRot = MathHelper.sin(hurtRot * hurtRot * hurtRot * hurtRot * MathHelper.PI) * 14.0F;
				}

				float deathRot = 0;
				if(this.health <= 0) {
					deathRot = (this.deathTime + dt) / 20.0F;
					hurtRot += deathRot * deathRot * 800.0F;
					if(hurtRot > 90.0F) {
						hurtRot = 90.0F;
					}
				}

				GL11.glRotatef(180.0F - bodyYaw + this.rotOffs, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(1.0F, 1.0F, 1.0F);
				GL11.glRotatef(-this.hurtDir, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(-hurtRot, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(this.hurtDir, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(-(180.0F - bodyYaw + this.rotOffs), 0.0F, 1.0F, 0.0F);
			}

			GL11.glTranslatef(0.0F, -bob * 0.0625F, 0.0F);
			GL11.glScalef(1.0F, -1.0F, 1.0F);
			GL11.glRotatef(180.0F - bodyYaw + this.rotOffs, 0.0F, 1.0F, 0.0F);
			if(!this.allowAlpha) {
				GL11.glDisable(GL11.GL_ALPHA_TEST);
			} else {
				GL11.glDisable(GL11.GL_CULL_FACE);
			}

			GL11.glScalef(-1.0F, 1.0F, 1.0F);
			modelCache.getModel(this.modelName).attackTime = attackTime / 5.0F;
			this.bindTexture(textures);
			this.renderModel(textures, animStep, dt, runProgress, yaw, pitch, 0.0625F);
			if(this.invulnerableTime > this.invulnerableDuration - 10) {
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.75F);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
				this.bindTexture(textures);
				this.renderModel(textures, animStep, dt, runProgress, yaw, pitch, 0.0625F);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}

			GL11.glEnable(GL11.GL_ALPHA_TEST);
			if(this.allowAlpha) {
				GL11.glEnable(GL11.GL_CULL_FACE);
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
	}

	public void renderModel(TextureManager textures, float animStep, float dt, float runProgress, float yaw, float pitch, float scale) {
		modelCache.getModel(this.modelName).render(animStep, runProgress, this.tickCount + dt, yaw, pitch, scale);
	}

	public void heal(int amount) {
		if(this.health > 0) {
			this.health += amount;
			if(this.health > 20) {
				this.health = 20;
			}

			this.invulnerableTime = this.invulnerableDuration / 2;
		}
	}

	public void hurt(Entity cause, int damage) {
		if(!this.level.creativeMode) {
			if(this.health > 0) {
				this.ai.hurt(cause, damage);
				if(this.invulnerableTime > this.invulnerableDuration / 2.0F) {
					if(this.lastHealth - damage >= this.health) {
						return;
					}

					this.health = this.lastHealth - damage;
				} else {
					this.lastHealth = this.health;
					this.invulnerableTime = this.invulnerableDuration;
					this.health -= damage;
					this.hurtTime = this.hurtDuration = 10;
				}

				this.hurtDir = 0;
				if(cause != null) {
					float xDistance = cause.x - this.x;
					float zDistance = cause.z - this.z;
					this.hurtDir = (float) (Math.atan2(zDistance, xDistance) * 180 / Math.PI) - this.yaw;
					this.knockback(cause, damage, xDistance, zDistance);
				} else {
					this.hurtDir = ((int) (Math.random() * 2.0D) * 180);
				}

				if(this.health <= 0) {
					this.die(cause);
				}
			}
		}
	}

	public void knockback(Entity entity, int damage, float xDistance, float zDistance) {
		float len = (float) Math.sqrt(xDistance * xDistance + zDistance * zDistance);
		this.xd /= 2.0F;
		this.yd /= 2.0F;
		this.zd /= 2.0F;
		this.xd -= xDistance / len * 0.4F;
		this.yd += 0.4F;
		this.zd -= zDistance / len * 0.4F;
		if(this.yd > 0.4F) {
			this.yd = 0.4F;
		}

	}

	public void die(Entity cause) {
		if(!this.level.creativeMode) {
			if(this.deathScore > 0 && cause != null) {
				cause.awardKillScore(this, this.deathScore);
			}

			this.dead = true;
		}
	}

	protected void causeFallDamage(float distance) {
		if(!this.level.creativeMode) {
			int damage = (int) Math.ceil((distance - 3));
			if(damage > 0) {
				this.hurt(null, damage);
			}

		}
	}

	public void travel(float x, float z) {
		boolean flying = this.ai instanceof BasicAI && ((BasicAI) this.ai).flying;
		BlockType blockIn = this.getBlockIn();
		if(blockIn != null && blockIn.isLiquid()) {
			float y = this.y;
			this.moveRelative(x, z, flying ? 0.125F : 0.02F);
			this.move(this.xd, this.yd, this.zd);
			this.xd *= blockIn.getSpeedModifier();
			this.yd *= blockIn.getSpeedModifier();
			this.zd *= blockIn.getSpeedModifier();
			if(!flying) {
				this.yd = (float) (this.yd - 0.02D);
			}

			if(this.horizontalCollision && this.isFree(this.xd, this.yd + 0.6F - this.y + y, this.zd)) {
				this.yd = 0.3F;
			}
		} else {
			this.moveRelative(x, z, flying ? 0.125F : this.onGround ? 0.1F : 0.02F);
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.91F;
			this.yd *= 0.98F;
			this.zd *= 0.91F;
			if(blockIn != null) {
				this.xd *= blockIn.getSpeedModifier();
				this.yd *= blockIn.getSpeedModifier();
				this.zd *= blockIn.getSpeedModifier();
			}
			
			if(!flying) {
				this.yd = (float) (this.yd - 0.08D);
			}

			if(this.onGround) {
				float y = 0.6F;
				this.xd *= y;
				this.zd *= y;
			}
		}
	}

	public boolean isShootable() {
		return true;
	}
}
