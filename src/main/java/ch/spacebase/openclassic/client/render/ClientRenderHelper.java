package ch.spacebase.openclassic.client.render;

import java.awt.image.BufferedImage;
import org.lwjgl.opengl.Display;

import static org.lwjgl.opengl.GL11.*;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.CuboidModel;
import ch.spacebase.openclassic.api.block.model.EmptyModel;
import ch.spacebase.openclassic.api.block.model.LiquidModel;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.particle.TerrainParticle;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class ClientRenderHelper extends RenderHelper {
	
	private TextureManager manager = new TextureManager();
	//private TextRenderer text = new TextRenderer("/default.png");
	private FontRenderer text;
	private int binded = -1;
	
	public static ClientRenderHelper getHelper() {
		return (ClientRenderHelper) helper;
	}
	
	public void setup() {
		this.text = new FontRenderer();
	}
	
	public void drawDefaultBG() {
		this.bindTexture("/gui/bg.png", true);

		int width = Display.getWidth();// * 240 / Display.getHeight();
		int height = Display.getHeight();// * 240 / Display.getHeight();

		Renderer.begin();
		Renderer.color(1, 1, 1);
		Renderer.vertuv(0, height, 0, 0, 1);
		Renderer.vertuv(width, height, 0, 1, 1);
		Renderer.vertuv(width, 0, 0, 1, 0);
		Renderer.vertuv(0, 0, 0, 0, 0);
		Renderer.end();
	}
	
	public void drawBox(float x1, float y1, float x2, float y2, int color) {
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_CULL_FACE);
		Renderer.begin();
		Renderer.color(color, true);
		Renderer.vert(x1, y2, 0);
		Renderer.vert(x2, y2, 0);
		Renderer.vert(x2, y1, 0);
		Renderer.vert(x1, y1, 0);
		Renderer.end();
		glEnable(GL_CULL_FACE);
		glEnable(GL_TEXTURE_2D);
	}
	
	public void color(int x1, int y1, int x2, int y2, int color) {
		this.color(x1, y1, x2, y2, color, color);
	}
	
	public void color(int x1, int y1, int x2, int y2, int color, int fadeTo) {
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_CULL_FACE);
		Renderer.begin();
		Renderer.color(color, true);
		Renderer.vert(x2, y1, 0);
		Renderer.vert(x1, y1, 0);
		Renderer.color(fadeTo, true);
		Renderer.vert(x1, y2, 0);
		Renderer.vert(x2, y2, 0);
		Renderer.end();
		glEnable(GL_CULL_FACE);
		glEnable(GL_TEXTURE_2D);
	}
	
	public int bindTexture(String file, boolean jar) {
		int id = this.manager.load(file, jar);
		this.bindTexture(id);
		return id;
	}
	
	public void bindTexture(int id) {
		glBindTexture(GL_TEXTURE_2D, id);
		this.binded = id;
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
		Renderer.begin();
		int id = this.manager.getTextureId(quad.getTexture().getParent().getTexture());
		if(id == -1 || id != this.binded) {
			this.bindTexture(quad.getTexture().getParent().getTexture(), quad.getTexture().getParent().isInJar());
		}
		
		if(brightness >= 0) {
			Renderer.color(brightness, brightness, brightness, 1);
		}
		
		float y1 = quad.getTexture().getY1();
		float y2 = quad.getTexture().getY2();

		if(quad.getParent() instanceof CuboidModel) {
			BlockFace face = CuboidModel.quadToFace((CuboidModel) quad.getParent(), quad.getId());
			Renderer.normal(face.getModX(), face.getModY(), face.getModZ());
		}
		
		if(quad.getParent() instanceof CuboidModel && !(quad.getParent() instanceof LiquidModel) && quad.getId() > 1 && (quad.getVertex(0).getY() > 0 || quad.getVertex(1).getY() < 1)) {
			y1 = y1 + quad.getVertex(0).getY() * (quad.getTexture().getY2() - quad.getTexture().getY1());
			y2 = y1 + quad.getVertex(1).getY() * (quad.getTexture().getY2() - quad.getTexture().getY1());
		}
		
		float width = quad.getTexture().getParent().getWidth();
		float height = quad.getTexture().getParent().getHeight();
		Renderer.vertuv(x + quad.getVertex(0).getX(), y + quad.getVertex(0).getY(), z + quad.getVertex(0).getZ(), quad.getTexture().getX2() / width, y2 / height);
		Renderer.vertuv(x + quad.getVertex(1).getX(), y + quad.getVertex(1).getY(), z + quad.getVertex(1).getZ(), quad.getTexture().getX2() / width, y1 / height);
		Renderer.vertuv(x + quad.getVertex(2).getX(), y + quad.getVertex(2).getY(), z + quad.getVertex(2).getZ(), quad.getTexture().getX1() / width, y1 / height);
		Renderer.vertuv(x + quad.getVertex(3).getX(), y + quad.getVertex(3).getY(), z + quad.getVertex(3).getZ(), quad.getTexture().getX1() / width, y2 / height);
		
		Renderer.end();
	}
	
	public void drawTexture(Texture texture, float x, float y, float brightness) {
		this.drawTexture(texture, x, y, 0, brightness);
	}
	
	public void drawTexture(Texture texture, float x, float y, float z, float brightness) {
		Renderer.begin();
		int id = this.manager.getTextureId(texture.getTexture());
		if(id == -1 || id != this.binded) {
			this.bindTexture(texture.getTexture(), texture.isInJar());
		}
		
		if(brightness >= 0) {
			Renderer.color(brightness, brightness, brightness);
		}
		
		Renderer.vertuv(x, y, z, 0, 0);
		Renderer.vertuv(x, y + texture.getHeight(), z, 0, texture.getHeight() / texture.getHeight());
		Renderer.vertuv(x + texture.getWidth(), y + texture.getHeight(), z, texture.getWidth() / texture.getWidth(), texture.getHeight() / texture.getHeight());
		Renderer.vertuv(x + texture.getWidth(), y, z, texture.getWidth() / texture.getWidth(), 0);
		
		Renderer.end();
	}
	
	public void drawSubTex(SubTexture texture, float x, float y, float brightness) {
		this.drawSubTex(texture, x, y, 0, brightness);
	}
	
	public void drawSubTex(SubTexture texture, float x, float y, float z, float brightness) {
		this.drawSubTex(texture, x, y, z, 1, brightness, brightness, brightness);
	}
	
	public void drawSubTex(SubTexture texture, float x, float y, float z, float scale, float r, float g, float b) {
		Renderer.begin();
		int id = this.manager.getTextureId(texture.getParent().getTexture());
		if(id == -1 || id != this.binded) {
			this.bindTexture(texture.getParent().getTexture(), texture.getParent().isInJar());
		}
		
		if(r >= 0 && g >= 0 && b >= 0) {
			Renderer.color(r, g, b);
		}
		
		Renderer.vertuv(x, y, z, texture.getX1() / texture.getParent().getWidth(), texture.getY1() / texture.getParent().getHeight());
		Renderer.vertuv(x, y + ((texture.getY2() - texture.getY1()) * scale), z, texture.getX1() / texture.getParent().getWidth(), texture.getY2() / texture.getParent().getHeight());
		Renderer.vertuv(x + ((texture.getX2() - texture.getX1()) * scale), y + ((texture.getY2() - texture.getY1()) * scale), z, texture.getX2() / texture.getParent().getWidth(), texture.getY2() / texture.getParent().getHeight());
		Renderer.vertuv(x + ((texture.getX2() - texture.getX1()) * scale), y, z, texture.getX2() / texture.getParent().getWidth(), texture.getY1() / texture.getParent().getHeight());
		
		Renderer.end();
	}
	
	public boolean canRenderSide(BlockType block, int x, int y, int z, BlockFace face) {
		if(block == null || block.getModel() instanceof EmptyModel) return false;
		if(!OpenClassic.getClient().getLevel().isColumnLoaded((x + face.getModX()) >> 4, (z + face.getModZ()) >> 4)) return false;
		BlockType relative = OpenClassic.getClient().getLevel().getBlockTypeAt(x + face.getModX(), y + face.getModY(), z + face.getModZ());
		if(VanillaBlock.is(block)) {
			if(block == VanillaBlock.GLASS) {
				return relative == null || (relative != block && !this.isOpaque(relative));
			} else if(block == VanillaBlock.WATER || block == VanillaBlock.STATIONARY_WATER || block == VanillaBlock.LAVA || block == VanillaBlock.STATIONARY_LAVA) {
				if(relative == null) {
					return false;
				}
				
				if(toMoving(relative) == toMoving(block)) {
					return false;
				}
				
				return !this.isOpaque(relative);
			} else if(block == VanillaBlock.SLAB) {
				return relative == null || face == BlockFace.UP || (!this.isOpaque(relative) && (face == BlockFace.DOWN || relative != VanillaBlock.SLAB));
			} else if(block == VanillaBlock.CACTUS) {
				return relative == null || (!this.isOpaque(relative) && relative != VanillaBlock.CACTUS);
			}
		} else {
			// TODO: Custom canRenderSide for custom blocks?
		}
		
		return relative == null || !this.isOpaque(relative);
	}
	
	public static BlockType toMoving(BlockType block) {
		if(block == VanillaBlock.STATIONARY_LAVA) return VanillaBlock.LAVA;
		if(block == VanillaBlock.STATIONARY_WATER) return VanillaBlock.WATER;
		
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
			Renderer.begin();
			block.getModel().renderAll(block, -2, 0, 0, 1);
			Renderer.end();
			glEnable(GL_DEPTH_TEST);
			glPopMatrix();
		}
	}

	public void drawImage(BufferedImage image, int x, int y) {
		this.drawImage(image, x, y, 0);
	}
	
	public void drawImage(BufferedImage image, int x, int y, int z) {
		this.manager.load(image);
		
		this.glColor(1, 1, 1, 1);
		Renderer.begin();
		Renderer.vertuv(x, y, z, 0, 0);
		Renderer.vertuv(x, y + image.getHeight(), z, 0, image.getHeight() / image.getHeight());
		Renderer.vertuv(x + image.getWidth(), y + image.getHeight(), z, image.getWidth() / image.getWidth(), image.getHeight() / image.getHeight());
		Renderer.vertuv(x + image.getWidth(), y, z, image.getWidth() / image.getWidth(), 0);
		Renderer.end();
	}

	public void renderText(String text, float x, float y) {
		this.renderText(text, x, y, true);
	}
	
	public void renderText(String text, float x, float y, boolean xCenter) {
		if(xCenter) {
			this.text.render(text, x - this.text.getWidth(text) / 2, y);//, color);
		} else {
			this.text.render(text, x, y);//, color);
		}
	}
	
	public void renderTextNoShadow(String text, float x, float y) {
		this.renderTextNoShadow(text, x, y, true);
	}
	
	public void renderTextNoShadow(String text, float x, float y, boolean xCenter) {
		if(xCenter) {
			this.text.render(text, x - this.text.getWidth(text) / 2, y, false);//, color);
		} else {
			this.text.render(text, x, y, false);//, color);
		}
	}

	@Override
	public float getStringWidth(String string) {
		return this.text.getWidth(string);
	}
	
	public void spawnDestructionParticles(BlockType block, ClientLevel level, Position pos) {
		Position spawn = null;
		for (int xMod = 1; xMod < 4; xMod++) {
			for (int yMod = 1; yMod < 4; yMod++) {
				for (int zMod = 1; zMod < 4; zMod++) {
					spawn = pos.clone();
					spawn.add((xMod + 0.5f) / 4, (yMod + 0.5f) / 4, (zMod + 0.5f) / 4);
					level.getParticleManager().spawnParticle(new TerrainParticle(spawn, spawn.getX() - pos.getX() - 0.5f, spawn.getY() - pos.getY() - 0.5f, spawn.getZ() - pos.getZ() - 0.5f, block));
				}
			}
		}
	}

	@Override
	public void drawScaledQuad(Quad quad, float x, float y, float z, float scale, float brightness) {
		Renderer.begin();
		int id = this.manager.getTextureId(quad.getTexture().getParent().getTexture());
		if(id == -1 || id != this.binded) {
			this.bindTexture(quad.getTexture().getParent().getTexture(), quad.getTexture().getParent().isInJar());
		}

		if(brightness >= 0) {
			Renderer.color(brightness, brightness, brightness);
		}
		
		float y1 = quad.getTexture().getY1();
		float y2 = quad.getTexture().getY2();

		if(quad.getParent() instanceof CuboidModel && !(quad.getParent() instanceof LiquidModel) && quad.getId() > 1 && (quad.getVertex(0).getY() > 0 || quad.getVertex(1).getY() < 1)) {
			y1 = y1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight();
			y2 = y1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight();
		}

		float width = quad.getTexture().getParent().getWidth();
		float height = quad.getTexture().getParent().getHeight();
		Renderer.vertuv(x + quad.getVertex(0).getX() * scale, y + quad.getVertex(0).getY() * scale, z + quad.getVertex(0).getZ() * scale, quad.getTexture().getX2() / width, y2 / height);
		Renderer.vertuv(x + quad.getVertex(1).getX() * scale, y + quad.getVertex(1).getY() * scale, z + quad.getVertex(1).getZ() * scale, quad.getTexture().getX2() / width, y1 / height);
		Renderer.vertuv(x + quad.getVertex(2).getX() * scale, y + quad.getVertex(2).getY() * scale, z + quad.getVertex(2).getZ() * scale, quad.getTexture().getX1() / width, y1 / height);
		Renderer.vertuv(x + quad.getVertex(3).getX() * scale, y + quad.getVertex(3).getY() * scale, z + quad.getVertex(3).getZ() * scale, quad.getTexture().getX1() / width, y2 / height);
		
		Renderer.end();
	}

	public TextureManager getTextureManager() {
		return this.manager;
	}

	@Override
	public void setCulling(boolean enabled) {
		if(enabled) {
			glEnable(GL_CULL_FACE);
		} else {
			glDisable(GL_CULL_FACE);
		}
	}
	
}
