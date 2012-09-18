package ch.spacebase.openclassic.client.particle;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.Model;
import ch.spacebase.openclassic.client.level.ClientLevel;

public class TerrainParticle extends Particle {
	
	private BlockType type;
	private Model model;
	
	public TerrainParticle(Position pos, float velX, float velY, float velZ, BlockType type) {
		super(pos, velX, velY, velZ, type == VanillaBlock.LEAVES ? 0.4f : type == VanillaBlock.SPONGE ? 0.9f : 1);
		this.type = type;
		this.model = type.getModel();
		
		this.setBoundingBox(this.model.getSelectionBox(0, 0, 0).scale(0.125f));
	}

	public BlockType getType() {
		return this.type;
	}

	@Override
	public void render(float delta) {
		float brightness = ((ClientLevel) this.getPosition().getLevel()).getBrightness(this.getPosition().getBlockX(), this.getPosition().getBlockY(), this.getPosition().getBlockZ());
		this.model.renderScaled(this.getPosition().getInterpolatedX(delta), this.getPosition().getInterpolatedY(delta), this.getPosition().getInterpolatedZ(delta), 0.125f, brightness);
	}

}
