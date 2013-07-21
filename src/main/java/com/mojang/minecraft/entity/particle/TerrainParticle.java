package com.mojang.minecraft.entity.particle;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.client.render.Renderer;

import com.mojang.minecraft.level.Level;

public class TerrainParticle extends Particle {

	private static final long serialVersionUID = 1L;

	private BlockType block;
	
	public TerrainParticle(Level level, float x, float y, float z, float xd, float yd, float zd, BlockType block) {
		super(level, x, y, z, xd, yd, zd);
		Quad quad = block.getModel().getQuads().size() >= 3 ? block.getModel().getQuad(2) : block.getModel().getQuad(block.getModel().getQuads().size() - 1);
		this.tex = quad.getTexture().getId();
		this.gravity = block == VanillaBlock.LEAVES ? 0.4F : (block == VanillaBlock.SPONGE ? 0.9F : 1);
		this.rCol = this.gCol = this.bCol = 0.6F;
		this.block = block;
	}

	public int getParticleId() {
		return 1;
	}

	public void render(float dt, float xmod, float ymod, float zmod, float xdir, float zdir) {
		float tminX = (this.tex % 16) / 16f + 0.02f;
		float tmaxX = tminX + 0.015609375F;
		float tminY = (this.tex / 16f) / 16f;
		if(this.block == VanillaBlock.BROWN_MUSHROOM || this.block == VanillaBlock.GRAY_CLOTH || this.block == VanillaBlock.BLACK_CLOTH || this.block == VanillaBlock.WHITE_CLOTH) {
			tminY -= 0.01f;
		}
		
		float tmaxY = tminY + 0.015609375F;
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
