package com.mojang.minecraft.entity.particle;

import java.util.ArrayList;
import java.util.List;

import com.mojang.minecraft.render.TextureManager;

public class ParticleManager {

	@SuppressWarnings("unchecked")
	public List<Particle>[] particles = new List[2];

	public ParticleManager(TextureManager textureManager) {
		for(int texture = 0; texture < this.particles.length; texture++) {
			this.particles[texture] = new ArrayList<Particle>();
		}
	}

	public void spawnParticle(Particle particle) {
		int texture = particle.getParticleTextureId();
		this.particles[texture].add(particle);
	}

	public void tickParticles() {
		for(int texture = 0; texture < 2; texture++) {
			for(int index = 0; index < this.particles[texture].size(); index++) {
				Particle particle = this.particles[texture].get(index);
				particle.tick();
				if(particle.removed) {
					this.particles[texture].remove(index--);
				}
			}
		}
	}
	
}
