package com.mojang.minecraft.entity.mob;

import java.io.Serializable;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.api.render.RenderHelper;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.item.Item;
import com.mojang.minecraft.entity.mob.ai.BasicAI;
import com.mojang.minecraft.entity.model.AnimalModel;
import com.mojang.minecraft.entity.player.LocalPlayer;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.render.TextureManager;

public class Sheep extends QuadrupedMob {

	public static final long serialVersionUID = 0L;
	public boolean hasFur = true;
	public boolean grazing = false;
	public int grazingTime = 0;
	public float graze;
	public float grazeO;

	public Sheep(Level level, float x, float y, float z) {
		super(level, x, y, z);
		this.setSize(1.4F, 1.72F);
		this.setPos(x, y, z);
		this.heightOffset = 1.72F;
		this.modelName = "sheep";
		this.textureName = "/mob/sheep.png";
		this.ai = new SheepAI(this);
	}

	public void aiStep() {
		super.aiStep();
		this.grazeO = this.graze;
		if (this.grazing) {
			this.graze += 0.2F;
		} else {
			this.graze -= 0.2F;
		}

		if (this.graze < 0.0F) {
			this.graze = 0.0F;
		}

		if (this.graze > 1.0F) {
			this.graze = 1.0F;
		}

	}

	public void die(Entity cause) {
		if (cause != null) {
			cause.awardKillScore(this, 10);
		}

		int drops = (int) (Math.random() + Math.random() + 1.0D);

		for (int count = 0; count < drops; count++) {
			this.level.addEntity(new Item(this.level, this.x, this.y, this.z, VanillaBlock.WHITE_CLOTH.getId()));
		}

		super.die(cause);
	}

	public void hurt(Entity cause, int damage) {
		if (this.hasFur && cause instanceof LocalPlayer) {
			this.hasFur = false;
			int wool = (int) (Math.random() * 3.0D + 1.0D);

			for (int count = 0; count < wool; count++) {
				this.level.addEntity(new Item(this.level, this.x, this.y, this.z, VanillaBlock.WHITE_CLOTH.getId()));
			}

		} else {
			super.hurt(cause, damage);
		}
	}

	public void renderModel(TextureManager textures, float animStep, float dt, float runProgress, float yaw, float pitch, float scale) {
		AnimalModel model = (AnimalModel) modelCache.getModel(this.modelName);
		float oHeadY = model.head.y;
		float oHeadZ = model.head.z;
		model.head.y += (this.grazeO + (this.graze - this.grazeO) * dt) * 8.0F;
		model.head.z -= this.grazeO + (this.graze - this.grazeO) * dt;
		super.renderModel(textures, animStep, dt, runProgress, yaw, pitch, scale);
		if (this.hasFur) {
			RenderHelper.getHelper().bindTexture("/mob/sheep_fur.png", true);
			GL11.glDisable(GL11.GL_CULL_FACE);
			AnimalModel fur = (AnimalModel) modelCache.getModel("sheep.fur");
			fur.head.yaw = model.head.yaw;
			fur.head.pitch = model.head.pitch;
			fur.head.y = model.head.y;
			fur.head.x = model.head.x;
			fur.body.yaw = model.body.yaw;
			fur.body.pitch = model.body.pitch;
			fur.leg1.pitch = model.leg1.pitch;
			fur.leg2.pitch = model.leg2.pitch;
			fur.leg3.pitch = model.leg3.pitch;
			fur.leg4.pitch = model.leg4.pitch;
			fur.head.render(scale);
			fur.body.render(scale);
			fur.leg1.render(scale);
			fur.leg2.render(scale);
			fur.leg3.render(scale);
			fur.leg4.render(scale);
		}

		model.head.y = oHeadY;
		model.head.z = oHeadZ;
	}
	
	public static class SheepAI extends BasicAI implements Serializable {
		private static final long serialVersionUID = 1L;

		private Sheep parent;
		
		public SheepAI(Sheep parent) {
			this.parent = parent;
		}
		
		public final void update() {
			float xDiff = -0.7F * MathHelper.sin(parent.yaw * MathHelper.DEG_TO_RAD);
			float zDiff = 0.7F * MathHelper.cos(parent.yaw * MathHelper.DEG_TO_RAD);
			int x = (int) (this.mob.x + xDiff);
			int y = (int) (this.mob.y - 2.0F);
			int z = (int) (this.mob.z + zDiff);
			if (parent.grazing) {
				if (this.level.getTile(x, y, z) != VanillaBlock.GRASS.getId()) {
					parent.grazing = false;
				} else {
					if (parent.grazingTime++ == 60) {
						this.level.setTile(x, y, z, VanillaBlock.DIRT.getId());
						if (this.random.nextInt(5) == 0) {
							parent.hasFur = true;
						}
					}

					this.xxa = 0.0F;
					this.yya = 0.0F;
					this.mob.pitch = 40 + parent.grazingTime / 2 % 2 * 10;
				}
			} else {
				if (this.level.getTile(x, y, z) == VanillaBlock.GRASS.getId()) {
					parent.grazing = true;
					parent.grazingTime = 0;
				}

				super.update();
			}
		}
	}
}
