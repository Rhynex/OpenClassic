package ch.spacebase.openclassic.client.particle;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.OpenClassic;

public class ParticleManager {

	private List<Particle> particles = new ArrayList<Particle>();
	
	public void spawnParticle(Particle particle) {
		if(!OpenClassic.getClient().getConfig().getBoolean("options.particles", true)) return;
		this.particles.add(particle);
	}
	
	public void update() {
		for(int count = 0; count < this.particles.size(); count++) {
			Particle particle = this.particles.get(count);
			particle.update();
			if(particle.isDisposed()) {
				this.particles.remove(particle);
			}
		}
	}

	public void render(float delta) {
		for(Particle particle : this.particles) {
			particle.render(delta);
		}
	}
	
}
