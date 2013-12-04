package com.mojang.minecraft.entity.particle;

import ch.spacebase.openclassic.api.Position;

public class SmokeParticle extends Particle {

	public SmokeParticle(Position pos) {
		super(pos, 0, 0, 0);
        this.xd *= 0.1F;
        this.yd *= 0.1F;
        this.zd *= 0.1F;
		this.rCol = this.gCol = this.bCol = (float) (Math.random() * 0.3D);
		this.lifetime = (int) (8 / (Math.random() * 0.8D + 0.2D));
		this.noPhysics = true;
	}

	public void tick() {
		// Reset previous values by setting pos to itself.
		this.pos.set(this.pos);
		if(this.age++ >= this.lifetime) {
			this.removed = true;
		}

        this.tex = 7 - (this.age << 3) / this.lifetime;
        this.yd = (float) (this.yd + 0.004D);
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.96F;
        this.yd *= 0.96F;
        this.zd *= 0.96F;
        if(this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
        }
	}
}
