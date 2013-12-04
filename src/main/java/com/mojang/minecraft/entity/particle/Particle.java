package com.mojang.minecraft.entity.particle;

import java.util.ArrayList;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.math.BoundingBox;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.render.Textures;
import ch.spacebase.openclassic.client.render.Renderer;

public class Particle {

	public Position pos;
	public BoundingBox bb;
	public float xd;
	public float yd;
	public float zd;
	public boolean onGround = false;
	public boolean removed = false;
	public boolean noPhysics = false;
	
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

	public Particle(Position pos, float xd, float yd, float zd) {
		this.pos = pos;
		float center = this.size / 2;
		this.bb = new BoundingBox(pos.getX() - center, pos.getY() - center, pos.getZ() - center, pos.getX() + center, pos.getY() + center, pos.getZ() + center);
		
		this.rCol = 1;
		this.gCol = 1;
		this.bCol = 1;
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
	}

	public Particle setPower(float power) {
		this.xd *= power;
		this.yd = (this.yd - 0.1F) * power + 0.1F;
		this.zd *= power;
		return this;
	}

	public Particle scale(float scale) {
		this.setSize(0.2f * scale);
		return this;
	}
	
	public Particle setSize(float size) {
		this.size = size;
		return this;
	}

	public void tick() {
		// Reset previous values by setting pos to itself.
		this.pos.set(this.pos);
		if(this.age++ >= this.lifetime) {
			this.removed = true;
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
		Textures.PARTICLES.bind();
		float tminX = (this.tex % 16) / 16f;
		float tmaxX = tminX + (1 / 16f);
		float tminY = (this.tex / 16f) / 16f;
		float tmaxY = tminY + (1 / 16f);
		float size = 0.1F * this.size;
		float x = this.pos.getInterpolatedX(dt);
		float y = this.pos.getInterpolatedY(dt);
		float z = this.pos.getInterpolatedZ(dt);
		float brightness = this.getBrightness(dt);
		Renderer.get().color(this.rCol * brightness, this.gCol * brightness, this.bCol * brightness);
		Renderer.get().vertexuv(x - xmod * size - xdir * size, y - ymod * size, z - zmod * size - zdir * size, tminX, tmaxY);
		Renderer.get().vertexuv(x - xmod * size + xdir * size, y + ymod * size, z - zmod * size + zdir * size, tminX, tminY);
		Renderer.get().vertexuv(x + xmod * size + xdir * size, y + ymod * size, z + zmod * size + zdir * size, tmaxX, tminY);
		Renderer.get().vertexuv(x + xmod * size - xdir * size, y - ymod * size, z + zmod * size - zdir * size, tmaxX, tmaxY);
	}
	
	public void move(float x, float y, float z) {
		if(this.noPhysics) {
			this.bb.move(x, y, z);
			this.pos.set((this.bb.getX1() + this.bb.getX2()) / 2, this.bb.getY1() + 0.1f, (this.bb.getZ1() + this.bb.getZ2()) / 2);
		} else {
			float oldX = x;
			float oldY = y;
			float oldZ = z;
			ArrayList<BoundingBox> cubes = ((ClientLevel) this.pos.getLevel()).getBoxes(this.bb.expand(x, y, z));
			for(BoundingBox cube : cubes) {
				y = cube.clipYCollide(this.bb, y);
			}

			this.bb.move(0, y, 0);
			for(BoundingBox cube : cubes) {
				x = cube.clipXCollide(this.bb, x);
			}

			this.bb.move(x, 0, 0);
			for(BoundingBox cube : cubes) {
				z = cube.clipZCollide(this.bb, z);
			}

			this.bb.move(0, 0, z);
			this.onGround = oldY != y && oldY < 0;
			if(oldX != x) {
				this.xd = 0;
			}

			if(oldY != y) {
				this.yd = 0;
			}

			if(oldZ != z) {
				this.zd = 0;
			}

			this.pos.set((this.bb.getX1() + this.bb.getX2()) / 2, this.bb.getY1() + 0.1f, (this.bb.getZ1() + this.bb.getZ2()) / 2);
		}
	}
	
	protected float getBrightness(float dt) {
		return this.pos.getLevel().getBrightness((int) this.pos.getX(), (int) (this.pos.getY() - 0.45f), (int) this.pos.getZ());
	}
	
}
