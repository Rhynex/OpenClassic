package com.mojang.minecraft.entity.item;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.math.MathHelper;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.particle.SmokeParticle;
import com.mojang.minecraft.entity.particle.TerrainParticle;
import com.mojang.minecraft.entity.player.LocalPlayer;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.render.TextureManager;

public class PrimedTnt extends Entity {

	private float xd;
	private float yd;
	private float zd;
	public int life = 0;
	private boolean defused;

	public PrimedTnt(Level level, float x, float y, float z) {
		super(level);
		this.setSize(0.98F, 0.98F);
		this.heightOffset = this.bbHeight / 2.0F;
		this.setPos(x, y, z);
		float motion = (float) (Math.random() * MathHelper.DTWO_PI);
		this.xd = -MathHelper.sin(motion * MathHelper.DEG_TO_RAD) * 0.02F;
		this.yd = 0.2F;
		this.zd = -MathHelper.cos(motion * MathHelper.DEG_TO_RAD) * 0.02F;
		this.makeStepSound = false;
		this.life = 40;
		this.xo = x;
		this.yo = y;
		this.zo = z;
	}

	public void hurt(Entity damager, int health) {
		if (!this.removed) {
			super.hurt(damager, health);
			if (damager instanceof LocalPlayer) {
				this.remove();
				this.level.addEntity(new Item(this.level, this.x, this.y, this.z, VanillaBlock.TNT.getId()));
			}
		}
	}

	public boolean isPickable() {
		return !this.removed;
	}

	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.yd -= 0.04F;
		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.98F;
		this.yd *= 0.98F;
		this.zd *= 0.98F;
		if (this.onGround) {
			this.xd *= 0.7F;
			this.zd *= 0.7F;
			this.yd *= -0.5F;
		}

		if (!this.defused) {
			if (this.life-- > 0) {
				this.level.particleEngine.spawnParticle(new SmokeParticle(this.level, this.x, this.y + 0.6F, this.z));
			} else {
				this.remove();
				Random rand = new Random();
				this.level.explode(null, this.x, this.y, this.z, 4);
				for (int count = 0; count < 100; count++) {
					float ox = (float) rand.nextGaussian();
					float oy = (float) rand.nextGaussian();
					float oz = (float) rand.nextGaussian();
					float len = (float) Math.sqrt(ox * ox + oy * oy + oz * oz);
					float dx = ox / len / len;
					float dy = oy / len / len;
					float dz = oz / len / len;
					this.level.particleEngine.spawnParticle(new TerrainParticle(this.level, this.x + ox, this.y + oy, this.z + oz, dx, dy, dz, VanillaBlock.TNT));
				}

			}
		}
	}

	public void playerTouch(LocalPlayer player) {
		if (this.defused) {
			if (player.addResource(VanillaBlock.TNT.getId())) {
				this.level.addEntity(new TakeEntityAnim(this.level, this, player));
				this.remove();
			}
		}
	}

	public void render(TextureManager textures, float dt) {
		float brightness = this.level.getBrightness((int) this.x, (int) this.y, (int) this.z);
		GL11.glPushMatrix();
		VanillaBlock.TNT.getModel().renderAll(this.xo + (this.x - this.xo) * dt - 0.5F, this.yo + (this.y - this.yo) * dt - 0.5F, this.zo + (this.z - this.zo) * dt - 0.5F, brightness);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor4f(1, 1, 1, ((this.life / 4 + 1) % 2) * 0.4F);
		if (this.life <= 16) {
			GL11.glColor4f(1, 1, 1, ((this.life + 1) % 2) * 0.6F);
		}

		if (this.life <= 2) {
			GL11.glColor4f(1, 1, 1, 0.9F);
		}

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		VanillaBlock.TNT.getModel().renderAll(this.xo + (this.x - this.xo) * dt - 0.5F, this.yo + (this.y - this.yo) * dt - 0.5F, this.zo + (this.z - this.zo) * dt - 0.5F, -1);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}
}
