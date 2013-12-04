package com.mojang.minecraft.entity.item;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.client.level.ClientLevel;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.particle.SmokeParticle;
import com.mojang.minecraft.entity.particle.TerrainParticle;
import com.mojang.minecraft.entity.player.LocalPlayer;

public class PrimedTnt extends Entity {

	private float xd;
	private float yd;
	private float zd;
	public int life = 0;
	private boolean defused;

	public PrimedTnt(ClientLevel level, float x, float y, float z) {
		super(level);
		this.setSize(0.98F, 0.98F);
		this.heightOffset = this.bbHeight / 2;
		this.setPos(x, y, z);
		float motion = (float) (Math.random() * MathHelper.DTWO_PI);
		this.xd = -MathHelper.sin(motion * MathHelper.DEG_TO_RAD) * 0.02F;
		this.yd = 0.2F;
		this.zd = -MathHelper.cos(motion * MathHelper.DEG_TO_RAD) * 0.02F;
		this.makeStepSound = false;
		this.life = 40;
		// Reset previous values by setting pos to itself.
		this.pos.set(this.pos);
	}

	public void hurt(Entity damager, int health) {
		if(!this.removed) {
			super.hurt(damager, health);
			if(damager instanceof LocalPlayer) {
				this.remove();
				this.getClientLevel().addEntity(new Item(this.getClientLevel(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), VanillaBlock.TNT.getId()));
			}
		}
	}

	public boolean isPickable() {
		return !this.removed;
	}

	public void tick() {
		// Reset previous values by setting pos to itself.
		this.pos.set(this.pos);
		this.yd -= 0.04F;
		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.98F;
		this.yd *= 0.98F;
		this.zd *= 0.98F;
		if(this.onGround) {
			this.xd *= 0.7F;
			this.zd *= 0.7F;
			this.yd *= -0.5F;
		}

		if(!this.defused) {
			if(this.life-- > 0) {
				this.getClientLevel().getParticleManager().spawnParticle(new SmokeParticle(this.pos.clone().add(0, 0.6f, 0)));
			} else {
				this.remove();
				Random rand = new Random();
				this.getClientLevel().explode(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), 4);
				for(int count = 0; count < 100; count++) {
					float ox = (float) rand.nextGaussian();
					float oy = (float) rand.nextGaussian();
					float oz = (float) rand.nextGaussian();
					float len = (float) Math.sqrt(ox * ox + oy * oy + oz * oz);
					float dx = ox / len / len;
					float dy = oy / len / len;
					float dz = oz / len / len;
					this.getClientLevel().getParticleManager().spawnParticle(new TerrainParticle(this.pos.clone().add(ox, oy, oz), dx, dy, dz, VanillaBlock.TNT));
				}
			}
		}
	}

	public void playerTouch(LocalPlayer player) {
		if(this.defused) {
			if(player.inventory.addResource(VanillaBlock.TNT.getId())) {
				this.getClientLevel().addEntity(new TakeEntityAnim(this.getClientLevel(), this, player));
				this.remove();
			}
		}
	}

	public void render(float dt) {
		float brightness = this.pos.getLevel().getBrightness(this.pos.getBlockX(), this.pos.getBlockY(), this.pos.getBlockZ());
		GL11.glPushMatrix();
		VanillaBlock.TNT.getModel().renderAll(this.pos.getInterpolatedX(dt) - 0.5F, this.pos.getInterpolatedY(dt) - 0.5F, this.pos.getInterpolatedZ(dt) - 0.5F, brightness);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		if(this.life <= 16) {
			if(this.life <= 2) {
				GL11.glColor4f(1, 1, 1, 0.9f);
			} else {
				GL11.glColor4f(1, 1, 1, ((this.life + 1) % 2) * 0.6f);
			}
		} else {
			GL11.glColor4f(1, 1, 1, ((this.life / 4 + 1) % 2) * 0.4F);
		}

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		VanillaBlock.TNT.getModel().renderAll(this.pos.getInterpolatedX(dt) - 0.5F, this.pos.getInterpolatedY(dt) - 0.5F, this.pos.getInterpolatedZ(dt) - 0.5F, -1);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glPopMatrix();
	}
	
}
