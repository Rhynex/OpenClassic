package com.mojang.minecraft.entity.particle;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.client.render.Renderer;

public class TerrainParticle extends Particle {
	
	private Texture texture;
	
	public TerrainParticle(Position pos, float xd, float yd, float zd, BlockType block) {
		super(pos, xd, yd, zd);
		Quad quad = block.getModel().getQuads().size() > 2 ? block.getModel().getQuad(2) : block.getModel().getQuads().size() > 0 ? block.getModel().getQuad(block.getModel().getQuads().size() - 1) : null;
		if(quad != null) {
			this.texture = quad.getTexture();
		}
		
		this.tex = 0;
		this.gravity = 1;
		this.rCol = 0.6f;
		this.gCol = 0.6f;
		this.bCol = 0.6f;
	}

	public void render(float dt, float xmod, float ymod, float zmod, float xdir, float zdir) {
		if(this.texture == null) {
			return;
		}
		
		this.texture.bind();
		float tminX = (this.texture.getX() / (float) this.texture.getFullWidth()) + this.uo / 64f;
		float tmaxX = tminX + (0.25f / 16);
		float tminY = (this.texture.getY() / (float) this.texture.getFullHeight()) + this.vo / 64f;
		float tmaxY = tminY + (0.25f / 16);
		float size = 0.1F * this.size;
		float x = this.pos.getInterpolatedX(dt);
		float y = this.pos.getInterpolatedY(dt);
		float z = this.pos.getInterpolatedZ(dt);
		float brightness = this.getBrightness(dt);
		Renderer.get().color(brightness * this.rCol, brightness * this.gCol, brightness * this.bCol);
		Renderer.get().vertexuv(x - xmod * size - xdir * size, y - ymod * size, z - zmod * size - zdir * size, tminX, tmaxY);
		Renderer.get().vertexuv(x - xmod * size + xdir * size, y + ymod * size, z - zmod * size + zdir * size, tminX, tminY);
		Renderer.get().vertexuv(x + xmod * size + xdir * size, y + ymod * size, z + zmod * size + zdir * size, tmaxX, tminY);
		Renderer.get().vertexuv(x + xmod * size - xdir * size, y - ymod * size, z + zmod * size - zdir * size, tmaxX, tmaxY);
	}
}
