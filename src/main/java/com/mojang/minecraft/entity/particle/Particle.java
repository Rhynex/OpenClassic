package com.mojang.minecraft.entity.particle;

import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.render.GuiTextures;
import ch.spacebase.openclassic.client.render.Renderer;

import com.mojang.minecraft.entity.Entity;

public class Particle extends Entity {

	protected int tex;
	protected float uo;
	protected float vo;
	protected int age = 0;
	protected int lifetime = 0;
	protected float size;
	protected float gravity;
	protected float rCol;
	protected float gCol;
	protected float bCol;

	public Particle(ClientLevel level, float x, float y, float z, float xd, float yd, float zd) {
		super(level);
		this.setSize(0.2F, 0.2F);
		this.heightOffset = this.bbHeight / 2;
		this.setPos(x, y, z);
		this.rCol = this.gCol = this.bCol = 1.0F;
		this.xd = xd + (float) (Math.random() * 2 - 1) * 0.4F;
		this.yd = yd + (float) (Math.random() * 2 - 1) * 0.4F;
		this.zd = zd + (float) (Math.random() * 2 - 1) * 0.4F;
		float multiplier = (float) (Math.random() + Math.random() + 1) * 0.15F;
		float len = (float) Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
		this.xd = this.xd / len * multiplier * 0.4F;
		this.yd = this.yd / len * multiplier * 0.4F + 0.1F;
		this.zd = this.zd / len * multiplier * 0.4F;
		this.uo = (float) Math.random() * 3;
		this.vo = (float) Math.random() * 3;
		this.size = (float) (Math.random() * 0.5D + 0.5D);
		this.lifetime = (int) (4.0D / (Math.random() * 0.9D + 0.1D));
		this.age = 0;
		this.makeStepSound = false;
	}

	public Particle setPower(float power) {
		this.xd *= power;
		this.yd = (this.yd - 0.1F) * power + 0.1F;
		this.zd *= power;
		return this;
	}

	public Particle scale(float scale) {
		this.setSize(0.2F * scale, 0.2F * scale);
		this.size *= scale;
		return this;
	}

	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if(this.age++ >= this.lifetime) {
			this.remove();
		}

		this.yd = (float) (this.yd - 0.04D * this.gravity);
		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.98F;
		this.yd *= 0.98F;
		this.zd *= 0.98F;
		if(this.onGround) {
			this.xd *= 0.7F;
			this.zd *= 0.7F;
		}
	}

	public void render(float dt, float xmod, float ymod, float zmod, float xdir, float zdir) {
		GuiTextures.PARTICLES.bind();
		float tminX = (this.tex % 16) / 16f;
		float tmaxX = tminX + (1 / 16f);
		float tminY = (this.tex / 16f) / 16f;
		float tmaxY = tminY + (1 / 16f);
		float size = 0.1F * this.size;
		float x = this.xo + (this.x - this.xo) * dt;
		float y = this.yo + (this.y - this.yo) * dt;
		float z = this.zo + (this.z - this.zo) * dt;
		float brightness = this.getBrightness(dt);
		Renderer.get().color(this.rCol * brightness, this.gCol * brightness, this.bCol * brightness);
		Renderer.get().vertexuv(x - xmod * size - xdir * size, y - ymod * size, z - zmod * size - zdir * size, tminX, tmaxY);
		Renderer.get().vertexuv(x - xmod * size + xdir * size, y + ymod * size, z - zmod * size + zdir * size, tminX, tminY);
		Renderer.get().vertexuv(x + xmod * size + xdir * size, y + ymod * size, z + zmod * size + zdir * size, tmaxX, tminY);
		Renderer.get().vertexuv(x + xmod * size - xdir * size, y - ymod * size, z + zmod * size - zdir * size, tmaxX, tmaxY);
	}
	
}
