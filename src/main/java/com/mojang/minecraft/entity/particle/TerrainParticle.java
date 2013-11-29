package com.mojang.minecraft.entity.particle;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.render.Renderer;

public class TerrainParticle extends Particle {

	private Quad quad;
	
	public TerrainParticle(ClientLevel level, float x, float y, float z, float xd, float yd, float zd, BlockType block) {
		super(level, x, y, z, xd, yd, zd);
		this.quad = block.getModel().getQuads().size() >= 3 ? block.getModel().getQuad(2) : block.getModel().getQuad(block.getModel().getQuads().size() - 1);
		this.tex = 0;
		this.gravity = 1;
		this.rCol = 0.6F;
		this.gCol = 0.6F;
		this.bCol = 0.6F;
	}

	public int getParticleTextureId() {
		return 1;
	}

	public void render(float dt, float xmod, float ymod, float zmod, float xdir, float zdir) {
		float tminX = (this.quad.getTexture().getX1() / this.quad.getTexture().getParent().getWidth()) + this.uo / 64f;
		float tmaxX = tminX + (0.25f / 16);
		float tminY = (this.quad.getTexture().getY1() / this.quad.getTexture().getParent().getHeight()) + this.vo / 64f;
		float tmaxY = tminY + (0.25f / 16);
		float size = 0.1F * this.size;
		float x = this.xo + (this.x - this.xo) * dt;
		float y = this.yo + (this.y - this.yo) * dt;
		float z = this.zo + (this.z - this.zo) * dt;
		float brightness = this.getBrightness(dt);
		Renderer.get().color(brightness * this.rCol, brightness * this.gCol, brightness * this.bCol);
		Renderer.get().vertexuv(x - xmod * size - xdir * size, y - ymod * size, z - zmod * size - zdir * size, tminX, tmaxY);
		Renderer.get().vertexuv(x - xmod * size + xdir * size, y + ymod * size, z - zmod * size + zdir * size, tminX, tminY);
		Renderer.get().vertexuv(x + xmod * size + xdir * size, y + ymod * size, z + zmod * size + zdir * size, tmaxX, tminY);
		Renderer.get().vertexuv(x + xmod * size - xdir * size, y - ymod * size, z + zmod * size - zdir * size, tmaxX, tmaxY);
	}
}
