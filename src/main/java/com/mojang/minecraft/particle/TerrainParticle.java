package com.mojang.minecraft.particle;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.Quad;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.particle.Particle;
import com.mojang.minecraft.render.ShapeRenderer;

public class TerrainParticle extends Particle {

	private static final long serialVersionUID = 1L;

	private BlockType block;
	
	public TerrainParticle(Level level, float x, float y, float z, float var5, float var6, float var7, BlockType block) {
		super(level, x, y, z, var5, var6, var7);
		Quad quad = block.getModel().getQuads().size() >= 3 ? block.getModel().getQuad(2) : block.getModel().getQuad(block.getModel().getQuads().size() - 1);
		this.tex = quad.getTexture().getId();
		this.gravity = block == VanillaBlock.LEAVES ? 0.4F : (block == VanillaBlock.SPONGE ? 0.9F : 1);
		this.rCol = this.gCol = this.bCol = 0.6F;
		this.block = block;
	}

	public int getParticleId() {
		return 1;
	}

	public void render(ShapeRenderer renderer, float delta, float xMod, float yMod, float zMod, float yaw, float pitch) {
		float var8 = (this.tex % 16) / 16 + 0.02f;
		float var9 = var8 + 0.015609375F;
		float var10 = (this.tex / 16f) / 16;
		if(this.block == VanillaBlock.GRAY_CLOTH || this.block == VanillaBlock.BLACK_CLOTH || this.block == VanillaBlock.WHITE_CLOTH) {
			var10 -= 0.01f;
		}
		
		float var11 = var10 + 0.015609375F;
		float var12 = 0.1F * this.size;
		float var13 = this.xo + (this.x - this.xo) * delta;
		float var14 = this.yo + (this.y - this.yo) * delta;
		float var15 = this.zo + (this.z - this.zo) * delta;
		delta = this.getBrightness(delta);
		renderer.color(delta * this.rCol, delta * this.gCol, delta * this.bCol);
		renderer.vertexUV(var13 - xMod * var12 - yaw * var12, var14 - yMod * var12, var15 - zMod * var12 - pitch * var12, var8, var11);
		renderer.vertexUV(var13 - xMod * var12 + yaw * var12, var14 + yMod * var12, var15 - zMod * var12 + pitch * var12, var8, var10);
		renderer.vertexUV(var13 + xMod * var12 + yaw * var12, var14 + yMod * var12, var15 + zMod * var12 + pitch * var12, var9, var10);
		renderer.vertexUV(var13 + xMod * var12 - yaw * var12, var14 - yMod * var12, var15 + zMod * var12 - pitch * var12, var9, var11);
	}
}
