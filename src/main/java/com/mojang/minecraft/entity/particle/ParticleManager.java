package com.mojang.minecraft.entity.particle;

import java.util.ArrayList;
import java.util.List;

public class ParticleManager {

	public List<Particle> particles = new ArrayList<Particle>();

	public void spawnParticle(Particle particle) {
		this.particles.add(particle);
	}

	public void tickParticles() {
		for(int index = 0; index < this.particles.size(); index++) {
			Particle particle = this.particles.get(index);
			particle.tick();
			if(particle.removed) {
				this.particles.remove(particle);
			}
		}
	}
	
}
