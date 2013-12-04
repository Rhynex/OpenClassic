package com.mojang.minecraft.entity.particle;

import ch.spacebase.openclassic.api.Position;

public class RainParticle extends Particle {

	public RainParticle(Position pos) {
		super(pos, 0, 0, 0);
		this.xd *= 0.3F;
		this.yd = (float) Math.random() * 0.2F + 0.1F;
		this.zd *= 0.3F;
		this.tex = 16;
		this.setSize(0.01f);
		this.lifetime = (int) (8 / (Math.random() * 0.8D + 0.2D));
	}

	public void tick() {
		// Reset previous values by setting pos to itself.
		this.pos.set(this.pos);
		this.yd = (float) (this.yd - 0.06D);
		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.98F;
		this.yd *= 0.98F;
		this.zd *= 0.98F;
		if(this.age++ >= this.lifetime) {
			this.removed = true;
		}

		if(this.onGround) {
			if(Math.random() < 0.5D) {
				this.removed = true;
			}

			this.xd *= 0.7F;
			this.zd *= 0.7F;
		}
	}
	
}
