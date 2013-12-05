package ch.spacebase.openclassic.client.level.particle;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.model.Quad;

public class TerrainParticle extends Particle {

	public TerrainParticle(Position pos, float xd, float yd, float zd, BlockType block) {
		super(pos, xd, yd, zd);
		Quad quad = block.getModel().getQuads().size() > 2 ? block.getModel().getQuad(2) : block.getModel().getQuads().size() > 0 ? block.getModel().getQuad(block.getModel().getQuads().size() - 1) : null;
		if(quad != null) {
			this.texture = quad.getTexture();
		}

		this.textureSizeDivider = 4;
		this.textureOffset = ((float) Math.random() * 3) / 64;
		this.gravity = 1;
		this.rCol = 0.6f;
		this.gCol = 0.6f;
		this.bCol = 0.6f;
	}
	
}
