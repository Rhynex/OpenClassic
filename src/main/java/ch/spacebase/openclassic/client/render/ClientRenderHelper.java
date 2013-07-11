package ch.spacebase.openclassic.client.render;

import java.awt.image.BufferedImage;
import java.util.Random;

import org.lwjgl.opengl.Display;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.asset.AssetSource;
import ch.spacebase.openclassic.api.asset.texture.SubTexture;
import ch.spacebase.openclassic.api.asset.texture.Texture;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.BoundingBox;
import ch.spacebase.openclassic.api.block.model.CuboidModel;
import ch.spacebase.openclassic.api.block.model.EmptyModel;
import ch.spacebase.openclassic.api.block.model.LiquidModel;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.item.Item;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.particle.TerrainParticle;

public class ClientRenderHelper extends RenderHelper {

	private static final Texture BG = OpenClassic.getGame().getAssetManager().load("/gui/bg.png", AssetSource.JAR, Texture.class);
	
	private ImageBinder imgBinder = new ImageBinder();
	private FontRenderer text;
	private Random random = new Random();

	public static ClientRenderHelper getHelper() {
		return (ClientRenderHelper) helper;
	}

	public void setup() {
		this.text = new FontRenderer();
	}

	public void drawDefaultBG() {
		BG.bind();
		int width = Display.getWidth();
		int height = Display.getHeight();

		Renderer.get().begin();
		Renderer.get().vertexuv(0, height, 0, 0, 1);
		Renderer.get().vertexuv(width, height, 0, 1, 1);
		Renderer.get().vertexuv(width, 0, 0, 1, 0);
		Renderer.get().vertexuv(0, 0, 0, 0, 0);
		Renderer.get().end();
	}
	
	public void drawGreenBackground(float x, float y, float width, float height) {
		BG.bind();
		Renderer.get().begin();
		Renderer.get().color(8454016);
		Renderer.get().vertexuv(x, y + height, 0, 0, 1);
		Renderer.get().vertexuv(x + width,  y +height, 0, 1, 1);
		Renderer.get().vertexuv(x + width, y, 0, 1, 0);
		Renderer.get().vertexuv(x, y, 0, 0, 0);
		Renderer.get().end();
	}

	public void drawBox(float x1, float y1, float x2, float y2, int color) {
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_CULL_FACE);
		Renderer.get().begin();
		Renderer.get().color(color, true);
		Renderer.get().vertex(x1, y2, 0);
		Renderer.get().vertex(x2, y2, 0);
		Renderer.get().vertex(x2, y1, 0);
		Renderer.get().vertex(x1, y1, 0);
		Renderer.get().end();
		glEnable(GL_CULL_FACE);
		glEnable(GL_TEXTURE_2D);
	}

	public void color(int x1, int y1, int x2, int y2, int color) {
		this.color(x1, y1, x2, y2, color, color);
	}

	public void color(int x1, int y1, int x2, int y2, int color, int fadeTo) {
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_CULL_FACE);
		Renderer.get().begin();
		Renderer.get().color(color, true);
		Renderer.get().vertex(x2, y1, 0);
		Renderer.get().vertex(x1, y1, 0);
		Renderer.get().color(fadeTo, true);
		Renderer.get().vertex(x1, y2, 0);
		Renderer.get().vertex(x2, y2, 0);
		Renderer.get().end();
		glEnable(GL_CULL_FACE);
		glEnable(GL_TEXTURE_2D);
	}
	
	public void colorSolid(int x1, int y1, int x2, int y2, int color) {
		this.colorSolid(x1, y1, x2, y2, color, color);
	}

	public void colorSolid(int x1, int y1, int x2, int y2, int color, int fadeTo) {
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_CULL_FACE);
		Renderer.get().begin();
		Renderer.get().color(color);
		Renderer.get().vertex(x2, y1, 0);
		Renderer.get().vertex(x1, y1, 0);
		Renderer.get().color(fadeTo);
		Renderer.get().vertex(x1, y2, 0);
		Renderer.get().vertex(x2, y2, 0);
		Renderer.get().end();
		glEnable(GL_CULL_FACE);
		glEnable(GL_TEXTURE_2D);
	}

	@Deprecated
	public int bindTexture(String file, boolean jar) {
		return 0;
	}

	public void bindTexture(int id) {
		glBindTexture(GL_TEXTURE_2D, id);
	}

	public void glColor(float red, float green, float blue, float alpha) {
		glColor4f(red, green, blue, alpha);
	}

	public int getDisplayWidth() {
		return Display.getWidth();
	}

	public int getDisplayHeight() {
		return Display.getHeight();
	}

	public void drawQuad(Quad quad, float x, float y, float z) {
		this.drawQuad(quad, x, y, z, 1);
	}

	public void drawQuad(Quad quad, float x, float y, float z, float brightness) {
		Renderer.get().begin();
		int id = quad.getTexture().getParent().getTextureId();
		if(id == -1 || id != glGetInteger(GL_TEXTURE_BINDING_2D)) {
			quad.getTexture().getParent().bind();
		}
		
		if(brightness >= 0) {
			Renderer.get().color(brightness, brightness, brightness, 1);
		}

		float y1 = quad.getTexture().getY1();
		float y2 = quad.getTexture().getY2();

		if(quad.getParent() instanceof CuboidModel) {
			BlockFace face = CuboidModel.quadToFace((CuboidModel) quad.getParent(), quad.getId());
			Renderer.get().normal(face.getModX(), face.getModY(), face.getModZ());
		}

		if(quad.getParent() instanceof CuboidModel && !(quad.getParent() instanceof LiquidModel) && quad.getId() > 1 && (quad.getVertex(0).getY() > 0 || quad.getVertex(1).getY() < 1)) {
			y1 = y1 + quad.getVertex(0).getY() * (quad.getTexture().getY2() - quad.getTexture().getY1());
			y2 = y1 + quad.getVertex(1).getY() * (quad.getTexture().getY2() - quad.getTexture().getY1());
		}

		float width = quad.getTexture().getParent().getWidth();
		float height = quad.getTexture().getParent().getHeight();
		Renderer.get().vertexuv(x + quad.getVertex(0).getX(), y + quad.getVertex(0).getY(), z + quad.getVertex(0).getZ(), quad.getTexture().getX2() / width, y2 / height);
		Renderer.get().vertexuv(x + quad.getVertex(1).getX(), y + quad.getVertex(1).getY(), z + quad.getVertex(1).getZ(), quad.getTexture().getX2() / width, y1 / height);
		Renderer.get().vertexuv(x + quad.getVertex(2).getX(), y + quad.getVertex(2).getY(), z + quad.getVertex(2).getZ(), quad.getTexture().getX1() / width, y1 / height);
		Renderer.get().vertexuv(x + quad.getVertex(3).getX(), y + quad.getVertex(3).getY(), z + quad.getVertex(3).getZ(), quad.getTexture().getX1() / width, y2 / height);

		Renderer.get().end();
	}

	public void drawTexture(Texture texture, float x, float y, float brightness) {
		this.drawTexture(texture, x, y, 0, brightness);
	}

	public void drawTexture(Texture texture, float x, float y, float z, float brightness) {
		Renderer.get().begin();
		int id = texture.getTextureId();
		if(id == -1 || id != glGetInteger(GL_TEXTURE_BINDING_2D)) {
			texture.bind();
		}

		if(brightness >= 0) {
			Renderer.get().color(brightness, brightness, brightness);
		}

		Renderer.get().vertexuv(x, y, z, 0, 0);
		Renderer.get().vertexuv(x, y + texture.getHeight(), z, 0, texture.getHeight() / texture.getHeight());
		Renderer.get().vertexuv(x + texture.getWidth(), y + texture.getHeight(), z, texture.getWidth() / texture.getWidth(), texture.getHeight() / texture.getHeight());
		Renderer.get().vertexuv(x + texture.getWidth(), y, z, texture.getWidth() / texture.getWidth(), 0);

		Renderer.get().end();
	}

	public void drawSubTex(SubTexture texture, float x, float y, float brightness) {
		this.drawSubTex(texture, x, y, 0, brightness);
	}

	public void drawSubTex(SubTexture texture, float x, float y, float z, float brightness) {
		this.drawSubTex(texture, x, y, z, 1, brightness);
	}

	public void drawSubTex(SubTexture texture, float x, float y, float z, float scale, float brightness) {
		Renderer.get().begin();
		int id = texture.getParent().getTextureId();
		if(id == -1 || id != glGetInteger(GL_TEXTURE_BINDING_2D)) {
			texture.getParent().bind();
		}

		if(brightness >= 0) {
			Renderer.get().color(brightness, brightness, brightness);
		}

		Renderer.get().vertexuv(x, y, z, texture.getX1() / texture.getParent().getWidth(), texture.getY1() / texture.getParent().getHeight());
		Renderer.get().vertexuv(x, y + (texture.getHeight() * scale), z, texture.getX1() / texture.getParent().getWidth(), texture.getY2() / texture.getParent().getHeight());
		Renderer.get().vertexuv(x + (texture.getWidth() * scale), y + (texture.getHeight() * scale), z, texture.getX2() / texture.getParent().getWidth(), texture.getY2() / texture.getParent().getHeight());
		Renderer.get().vertexuv(x + (texture.getWidth() * scale), y, z, texture.getX2() / texture.getParent().getWidth(), texture.getY1() / texture.getParent().getHeight());
		Renderer.get().end();
	}

	public boolean canRenderSide(BlockType block, int x, int y, int z, BlockFace face) {
		if(block == null || block.getModel() instanceof EmptyModel) return false;
		if(!OpenClassic.getClient().getLevel().isColumnLoaded((x + face.getModX()) >> 4, (z + face.getModZ()) >> 4)) return false;
		BlockType relative = OpenClassic.getClient().getLevel().getBlockTypeAt(x + face.getModX(), y + face.getModY(), z + face.getModZ());
		// TODO: move canRenderSide to BlockType?
		if(VanillaBlock.is(block)) {
			if(block == VanillaBlock.GLASS) {
				return relative == null || (relative != block && !this.isOpaque(relative));
			} else if(block.getId() == VanillaBlock.WATER.getId() || block.getId() == VanillaBlock.STATIONARY_WATER.getId() || block.getId() == VanillaBlock.LAVA.getId() || block.getId() == VanillaBlock.STATIONARY_LAVA.getId()) {
				if(relative == null) {
					return false;
				}
				
				if(toMoving(relative).getId() == toMoving(block).getId() && !(block.getData() != relative.getData() && (face == BlockFace.NORTH || face == BlockFace.SOUTH || face == BlockFace.EAST || face == BlockFace.WEST))) {
					return false;
				}

				return !this.isOpaque(relative);
			} else if(block == VanillaBlock.SLAB) {
				return relative == null || face == BlockFace.UP || (!this.isOpaque(relative) && (face == BlockFace.DOWN || relative != VanillaBlock.SLAB));
			} else if(block == VanillaBlock.CACTUS) {
				return relative == null || (!this.isOpaque(relative) && relative != VanillaBlock.CACTUS);
			}
		}

		return relative == null || !this.isOpaque(relative);
	}

	public static BlockType toMoving(BlockType block) {
		if(block.getId() == VanillaBlock.STATIONARY_LAVA.getId()) return VanillaBlock.LAVA;
		if(block.getId() == VanillaBlock.STATIONARY_WATER.getId()) return VanillaBlock.WATER;

		return block;
	}

	private boolean isOpaque(BlockType block) {
		if(block == null) return false;
		return block.isOpaque() && block.isCube();
	}

	public float getBrightness(BlockType main, int x, int y, int z) {
		return ((ClientLevel) OpenClassic.getClient().getLevel()).getBrightness(x, y, z);
	}

	public void drawRotatedBlock(int x, int y, BlockType block) {
		this.drawRotatedBlock(x, y, block, 0);
	}

	public void drawRotatedBlock(int x, int y, BlockType block, float scale) {
		if(block != null && block.getModel() != null) {
			glPushMatrix();
			glDisable(GL_DEPTH_TEST);
			glTranslatef(x, y, -100);
			glScalef(10, 10, 10);
			glTranslatef(1, 0, 8);
			glRotatef(-30, 1, 0, 0);
			glRotatef(45, 0, 1, 0);
			if(scale > 0) {
				glScalef(scale, scale, scale);
			}

			glTranslatef(-1.5f, 0.5f, 0.5f);
			glScalef(-1, -1, -1);
			block.getModel().renderAll(block, -2, 0, 0, 1);
			glEnable(GL_DEPTH_TEST);
			glPopMatrix();
		}
	}

	public void drawImage(BufferedImage image, int x, int y) {
		this.drawImage(image, x, y, 0);
	}

	public void drawImage(BufferedImage image, int x, int y, int z) {
		this.imgBinder.load(image);
		Renderer.get().begin();
		Renderer.get().vertexuv(x, y, z, 0, 0);
		Renderer.get().vertexuv(x, y + image.getHeight(), z, 0, image.getHeight() / image.getHeight());
		Renderer.get().vertexuv(x + image.getWidth(), y + image.getHeight(), z, image.getWidth() / image.getWidth(), image.getHeight() / image.getHeight());
		Renderer.get().vertexuv(x + image.getWidth(), y, z, image.getWidth() / image.getWidth(), 0);
		Renderer.get().end();
	}

	public void renderText(String text, float x, float y) {
		this.renderText(text, x, y, true);
	}
	
	public void renderScaledText(String text, float x, float y) {
		this.renderScaledText(text, x, y, true);
	}

	public void renderText(String text, float x, float y, boolean xCenter) {
		if(xCenter) {
			this.text.render(text, x - this.text.getWidth(text) / 2, y);
		} else {
			this.text.render(text, x, y);
		}
	}
	
	public void renderScaledText(String text, float x, float y, boolean xCenter) {
		if(xCenter) {
			this.text.render(text, x - this.text.getWidth(text, true) / 2, y, true, true);
		} else {
			this.text.render(text, x, y, true, true);
		}
	}

	public void renderTextNoShadow(String text, float x, float y) {
		this.renderTextNoShadow(text, x, y, true);
	}

	public void renderTextNoShadow(String text, float x, float y, boolean xCenter) {
		if(xCenter) {
			this.text.render(text, x - this.text.getWidth(text) / 2, y, false);
		} else {
			this.text.render(text, x, y, false);
		}
	}

	@Override
	public float getStringWidth(String string) {
		return this.text.getWidth(string);
	}

	public void spawnDestructionParticles(BlockType block, ClientLevel level, Position pos) {
		Position spawn = null;
		for(int xMod = 1; xMod < 4; xMod++) {
			for(int yMod = 1; yMod < 4; yMod++) {
				for(int zMod = 1; zMod < 4; zMod++) {
					spawn = pos.clone();
					spawn.add((xMod + 0.5f) / 4, (yMod + 0.5f) / 4, (zMod + 0.5f) / 4);
					level.getParticleManager().spawnParticle(new TerrainParticle(spawn, spawn.getX() - pos.getX() - 0.5f, spawn.getY() - pos.getY() - 0.5f, spawn.getZ() - pos.getZ() - 0.5f, block));
				}
			}
		}
	}

	public void spawnBlockParticles(BlockType block, ClientLevel level, Position pos, BlockFace face) {
		BoundingBox selection = block.getModel().getSelectionBox(0, 0, 0);
		if(selection == null) return;
		double xx = pos.getBlockX() + this.random.nextDouble() * (selection.getX2() - selection.getX1() - 0.2f) + 0.1f + selection.getX1();
		double yy = pos.getBlockY() + this.random.nextDouble() * (selection.getY2() - selection.getY1() - 0.2f) + 0.1f + selection.getY1();
		double zz = pos.getBlockZ() + this.random.nextDouble() * (selection.getZ2() - selection.getZ1() - 0.2f) + 0.1f + selection.getZ1();
		if(face == BlockFace.DOWN) {
			yy = (pos.getBlockY() + selection.getY1()) - 0.1F;
		} else if(face == BlockFace.UP) {
			yy = pos.getBlockY() + selection.getY2() + 0.1F;
		} else if(face == BlockFace.EAST) {
			zz = (pos.getBlockZ() + selection.getZ1()) - 0.1F;
		} else if(face == BlockFace.WEST) {
			zz = pos.getBlockZ() + selection.getZ2() + 0.1F;
		} else if(face == BlockFace.NORTH) {
			xx = (pos.getBlockX() + selection.getX1()) - 0.1F;
		} else if(face == BlockFace.SOUTH) {
			xx = pos.getBlockX() + selection.getX2() + 0.1F;
		}

		level.getParticleManager().spawnParticle(new TerrainParticle(new Position(level, (float) xx, (float) yy, (float) zz), 0, 0, 0, block).setPower(0.2F).setSize(0.075F));
	}

	@Override
	public void drawScaledQuad(Quad quad, float x, float y, float z, float scale, float brightness) {
		Renderer.get().begin();
		int id = quad.getTexture().getParent().getTextureId();
		if(id == -1 || id != glGetInteger(GL_TEXTURE_BINDING_2D)) {
			quad.getTexture().getParent().bind();
		}

		if(brightness >= 0) {
			Renderer.get().color(brightness, brightness, brightness);
		}

		float y1 = quad.getTexture().getY1();
		float y2 = quad.getTexture().getY2();

		if(quad.getParent() instanceof CuboidModel && !(quad.getParent() instanceof LiquidModel) && quad.getId() > 1 && (quad.getVertex(0).getY() > 0 || quad.getVertex(1).getY() < 1)) {
			y1 = y1 + quad.getVertex(0).getY() * quad.getTexture().getHeight();
			y2 = y1 + quad.getVertex(1).getY() * quad.getTexture().getHeight();
		}

		float width = quad.getTexture().getParent().getWidth();
		float height = quad.getTexture().getParent().getHeight();
		Renderer.get().vertexuv(x + quad.getVertex(0).getX() * scale, y + quad.getVertex(0).getY() * scale, z + quad.getVertex(0).getZ() * scale, quad.getTexture().getX2() / width, y2 / height);
		Renderer.get().vertexuv(x + quad.getVertex(1).getX() * scale, y + quad.getVertex(1).getY() * scale, z + quad.getVertex(1).getZ() * scale, quad.getTexture().getX2() / width, y1 / height);
		Renderer.get().vertexuv(x + quad.getVertex(2).getX() * scale, y + quad.getVertex(2).getY() * scale, z + quad.getVertex(2).getZ() * scale, quad.getTexture().getX1() / width, y1 / height);
		Renderer.get().vertexuv(x + quad.getVertex(3).getX() * scale, y + quad.getVertex(3).getY() * scale, z + quad.getVertex(3).getZ() * scale, quad.getTexture().getX1() / width, y2 / height);

		Renderer.get().end();
	}

	@Override
	public void setCulling(boolean enabled) {
		if(enabled) {
			glEnable(GL_CULL_FACE);
		} else {
			glDisable(GL_CULL_FACE);
		}
	}

	public void drawHeldItem(Item item, float brightness) {
		int id = item.getTexture().getParent().getTextureId();
		if(id == -1 || id != glGetInteger(GL_TEXTURE_BINDING_2D)) {
			item.getTexture().getParent().bind();
		}

		float u1 = item.getTexture().getX1() / 256f;
		float u2 = item.getTexture().getX2() / 256f;
		float v1 = item.getTexture().getY1() / 256f;
		float v2 = item.getTexture().getY2() / 256f;
		glPushMatrix();
		glEnable(GL_RESCALE_NORMAL);
		glTranslatef(0, -0.3f, 0);
		glScalef(1.5f, 1.5f, 1.5f);
		glRotatef(50f, 0, 1, 0);
		glRotatef(335f, 0, 0, 1);
		glTranslatef(-0.9375f, -0.0625f, 0);
		glColor4f(1, 1, 1, brightness);
		Renderer.get().begin();
		Renderer.get().normal(0, 0, 1);
		Renderer.get().vertexuv(0, 0, 0, u2, v2);
		Renderer.get().vertexuv(1, 0, 0, u1, v2);
		Renderer.get().vertexuv(1, 1, 0, u1, v1);
		Renderer.get().vertexuv(0, 1, 0, u2, v1);
		Renderer.get().end();
		Renderer.get().begin();
		Renderer.get().normal(0, 0, -1);
		Renderer.get().vertexuv(0, 1, -0.0625f, u2, v1);
		Renderer.get().vertexuv(1, 1, -0.0625f, u1, v1);
		Renderer.get().vertexuv(1, 0, -0.0625f, u1, v2);
		Renderer.get().vertexuv(0, 0, -0.0625f, u2, v2);
		Renderer.get().end();
		Renderer.get().begin();
		Renderer.get().normal(-1, 0, 0);
		for(int count = 0; count < 16; count++) {
			float mod = count / 16f;
			float u = (u2 + (u1 - u2) * mod) - 0.001953125f;
			float x = 1 * mod;
			Renderer.get().vertexuv(x, 0, -0.0625f, u, v2);
			Renderer.get().vertexuv(x, 0, 0, u, v2);
			Renderer.get().vertexuv(x, 1, 0, u, v1);
			Renderer.get().vertexuv(x, 1, -0.0625f, u, v1);
		}

		Renderer.get().end();
		Renderer.get().begin();
		Renderer.get().normal(1, 0, 0);
		for(int count = 0; count < 16; count++) {
			float mod = count / 16f;
			float u = (u2 + (u1 - u2) * mod) - 0.001953125f;
			float x = 1 * mod + 0.0625f;
			Renderer.get().vertexuv(x, 1, -0.0625f, u, v1);
			Renderer.get().vertexuv(x, 1, 0, u, v1);
			Renderer.get().vertexuv(x, 0, 0, u, v2);
			Renderer.get().vertexuv(x, 0, -0.0625f, u, v2);
		}

		Renderer.get().end();
		Renderer.get().begin();
		Renderer.get().normal(0, 1, 0);
		for(int count = 0; count < 16; count++) {
			float mod = count / 16f;
			float v = (v2 + (v1 - v2) * mod) - 0.001953125f;
			float y = 1 * mod + 0.0625f;
			Renderer.get().vertexuv(0, y, 0, u2, v);
			Renderer.get().vertexuv(1, y, 0, u1, v);
			Renderer.get().vertexuv(1, y, -0.0625f, u1, v);
			Renderer.get().vertexuv(0, y, -0.0625f, u2, v);
		}

		Renderer.get().end();
		Renderer.get().begin();
		Renderer.get().normal(0, -1f, 0);
		for(int count = 0; count < 16; count++) {
			float mod = count / 16f;
			float v = (v2 + (v1 - v2) * mod) - 0.001953125f;
			float y = 1 * mod;
			Renderer.get().vertexuv(1, y, 0, u1, v);
			Renderer.get().vertexuv(0, y, 0, u2, v);
			Renderer.get().vertexuv(0, y, -0.0625f, u2, v);
			Renderer.get().vertexuv(1, y, -0.0625f, u1, v);
		}

		Renderer.get().end();
		glDisable(GL_RESCALE_NORMAL);
		glPopMatrix();
	}

}
