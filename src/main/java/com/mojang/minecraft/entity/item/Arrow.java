package com.mojang.minecraft.entity.item;

import java.util.List;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.render.Renderer;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.player.LocalPlayer;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.render.TextureManager;

public class Arrow extends Entity {

	private float xd;
	private float yd;
	private float zd;
	private float yaw;
	private float pitch;
	private float oYaw;
	private float oPitch;
	private boolean hasHit = false;
	private int stickTime = 0;
	private Entity owner;
	private int time = 0;
	private int type = 0;
	private float gravity = 0.0F;
	private int damage;

	public Arrow(Level level, Entity owner, float x, float y, float z, float yaw, float pitch, float power) {
		super(level);
		this.owner = owner;
		this.setSize(0.3F, 0.5F);
		this.heightOffset = this.bbHeight / 2.0F;
		this.damage = 3;
		if (!(owner instanceof LocalPlayer)) {
			this.type = 1;
		} else {
			this.damage = 7;
		}

		this.heightOffset = 0.25F;
		float ycos = MathHelper.cos(-yaw * MathHelper.DEG_TO_RAD - MathHelper.PI);
		float ysin = MathHelper.sin(-yaw * MathHelper.DEG_TO_RAD - MathHelper.PI);
		float pcos = MathHelper.cos(-pitch * MathHelper.DEG_TO_RAD);
		float psin = MathHelper.sin(-pitch * MathHelper.DEG_TO_RAD);
		this.slide = false;
		this.gravity = 1.0F / power;
		this.xo -= ycos * 0.2F;
		this.zo += ysin * 0.2F;
		x -= ycos * 0.2F;
		z += ysin * 0.2F;
		this.xd = ysin * pcos * power;
		this.yd = psin * power;
		this.zd = ycos * pcos * power;
		this.setPos(x, y, z);
		float len = (float) Math.sqrt(this.xd * this.xd + this.zd * this.zd);
		this.yaw = (float) (Math.atan2(this.xd, this.zd) * MathHelper.DRAD_TO_DEG);
		this.oYaw = this.yaw;
		this.pitch = (float) (Math.atan2(this.yd, len) * MathHelper.DRAD_TO_DEG);
		this.oPitch = this.pitch;
		this.makeStepSound = false;
	}

	public void tick() {
		this.time++;
		this.oPitch = this.pitch;
		this.oYaw = this.yaw;
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.hasHit) {
			this.stickTime++;
			if (this.type == 0) {
				if (this.stickTime >= 300 && Math.random() < 0.009999999776482582D) {
					this.remove();
					return;
				}
			} else if (this.type == 1 && this.stickTime >= 20) {
				this.remove();
			}

		} else {
			this.xd *= 0.998F;
			this.yd *= 0.998F;
			this.zd *= 0.998F;
			this.yd -= 0.02F * this.gravity;
			int len = (int) ((float) Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd) / 0.2F + 1.0F);
			float movX = this.xd / len;
			float movY = this.yd / len;
			float movZ = this.zd / len;

			for (int mov = 0; mov < len && !this.collision; mov++) {
				AABB collision = this.bb.expand(movX, movY, movZ);
				if (this.level.getCubes(collision).size() > 0) {
					this.collision = true;
				}

				List<Entity> entities = this.level.blockMap.getEntities(this, collision);
				for (int count = 0; count < entities.size(); count++) {
					Entity entity = entities.get(count);
					if (entity.isShootable() && (entity != this.owner || this.time > 5)) {
						entity.hurt(this, this.damage);
						this.collision = true;
						this.remove();
						return;
					}
				}

				if (!this.collision) {
					this.bb.move(movX, movY, movZ);
					this.x += movX;
					this.y += movY;
					this.z += movZ;
					this.blockMap.moved(this);
				}
			}

			if (this.collision) {
				this.hasHit = true;
				this.xd = this.yd = this.zd = 0.0F;
			}

			if (!this.hasHit) {
				float xzlen = (float) Math.sqrt(this.xd * this.xd + this.zd * this.zd);
				this.yaw = (float) (Math.atan2(this.xd, this.zd) * 180.0D / Math.PI);
				this.pitch = (float) (Math.atan2(this.yd, xzlen) * 180.0D / Math.PI);
				while(this.pitch - this.oPitch < -180.0F) {
					this.oPitch -= 360.0F;
				}

				while (this.pitch - this.oPitch >= 180.0F) {
					this.oPitch += 360.0F;
				}

				while (this.yaw - this.oYaw < -180.0F) {
					this.oYaw -= 360.0F;
				}

				while (this.yaw - this.oYaw >= 180.0F) {
					this.oYaw += 360.0F;
				}
			}

		}
	}

	public void render(TextureManager textures, float dt) {
		this.textureId = RenderHelper.getHelper().bindTexture("/item/arrows.png", true);
		float brightness = this.level.getBrightness((int) this.x, (int) this.y, (int) this.z);
		GL11.glPushMatrix();
		GL11.glColor4f(brightness, brightness, brightness, 1.0F);
		GL11.glTranslatef(this.xo + (this.x - this.xo) * dt, this.yo + (this.y - this.yo) * dt - this.heightOffset / 2.0F, this.zo + (this.z - this.zo) * dt);
		GL11.glRotatef(this.oYaw + (this.yaw - this.oYaw) * dt - 90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(this.oPitch + (this.pitch - this.oPitch) * dt, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
		float endty2 = (0 + this.type * 10) / 32.0F;
		float endty1 = (5 + this.type * 10) / 32.0F;
		float ty2 = (5 + this.type * 10) / 32.0F;
		float ty1 = (10 + this.type * 10) / 32.0F;
		GL11.glScalef(0.05625F, 0.05625F, 0.05625F);
		Renderer.get().begin();
		Renderer.get().normal(0.05625F, 0.0F, 0.0F);
		Renderer.get().vertexuv(-7.0F, -2.0F, -2.0F, 0.0F, ty2);
		Renderer.get().vertexuv(-7.0F, -2.0F, 2.0F, 0.15625F, ty2);
		Renderer.get().vertexuv(-7.0F, 2.0F, 2.0F, 0.15625F, ty1);
		Renderer.get().vertexuv(-7.0F, 2.0F, -2.0F, 0.0F, ty1);
		Renderer.get().end();
		Renderer.get().begin();
		Renderer.get().normal(-0.05625F, 0.0F, 0.0F);
		Renderer.get().vertexuv(-7.0F, 2.0F, -2.0F, 0.0F, ty2);
		Renderer.get().vertexuv(-7.0F, 2.0F, 2.0F, 0.15625F, ty2);
		Renderer.get().vertexuv(-7.0F, -2.0F, 2.0F, 0.15625F, ty1);
		Renderer.get().vertexuv(-7.0F, -2.0F, -2.0F, 0.0F, ty1);
		Renderer.get().end();

		for (int end = 0; end < 4; end++) {
			GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
			Renderer.get().begin();
			Renderer.get().normal(0.0F, -0.05625F, 0.0F);
			Renderer.get().vertexuv(-8.0F, -2.0F, 0.0F, 0.0F, endty2);
			Renderer.get().vertexuv(8.0F, -2.0F, 0.0F, 0.5F, endty2);
			Renderer.get().vertexuv(8.0F, 2.0F, 0.0F, 0.5F, endty1);
			Renderer.get().vertexuv(-8.0F, 2.0F, 0.0F, 0.0F, endty1);
			Renderer.get().end();
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();
	}

	public void awardKillScore(Entity killed, int score) {
		this.owner.awardKillScore(killed, score);
	}

	public Entity getOwner() {
		return this.owner;
	}

	public void playerTouch(LocalPlayer player) {
		if (this.hasHit && this.owner == player && player.arrows < 99) {
			this.level.addEntity(new TakeEntityAnim(this.level, this, player));
			player.arrows++;
			this.remove();
		}

	}
}
