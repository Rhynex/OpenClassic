package ch.spacebase.openclassic.client.render;

import java.awt.image.BufferedImage;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.CubeModel;
import ch.spacebase.openclassic.api.block.model.CuboidModel;
import ch.spacebase.openclassic.api.block.model.Model;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.particle.ParticleManager;
import com.mojang.minecraft.particle.TerrainParticle;
import com.mojang.minecraft.render.FontRenderer;
import com.mojang.minecraft.render.ShapeRenderer;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class ClientRenderHelper extends RenderHelper {
	
	private static final Random rand = new Random();
	
	public static ClientRenderHelper getHelper() {
		return (ClientRenderHelper) helper;
	}
	
	private int binded = -1;
	
	public void drawDefaultBG() {
		this.bindTexture("/dirt.png", true);

		int width = this.getGuiWidth();
		int height = this.getGuiHeight();

		ShapeRenderer.instance.begin();
		ShapeRenderer.instance.color(4210752);
		ShapeRenderer.instance.vertexUV(0, height, 0, 0, height / 32);
		ShapeRenderer.instance.vertexUV(width, height, 0, width / 32, height / 32);
		ShapeRenderer.instance.vertexUV(width, 0, 0, width / 32, 0);
		ShapeRenderer.instance.vertexUV(0, 0, 0, 0, 0);
		ShapeRenderer.instance.end();
	}
	
	public void drawBlackBG(int x, int y, int width, int height) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		ShapeRenderer.instance.begin();
		ShapeRenderer.instance.color(0);
		ShapeRenderer.instance.vertexUV(x, y + height, 0, 0, height / 32);
		ShapeRenderer.instance.vertexUV(x + width, y + height, 0, width / 32, height / 32);
		ShapeRenderer.instance.vertexUV(x + width, y, 0, width / 32, 0);
		ShapeRenderer.instance.vertexUV(x, y, 0, 0, 0);
		ShapeRenderer.instance.end();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	public void renderText(String text, float x, float y) {
		this.renderText(text, x, y, true);
	}
	
	public void renderText(String text, float x, float y, boolean xCenter) {
		this.renderText(text, x, y, 16777215, xCenter);
	}
	
	public void renderScaledText(String text, float x, float y) {
		this.renderScaledText(text, x, y, true);
	}
	
	public void renderScaledText(String text, float x, float y, boolean xCenter) {
		FontRenderer renderer = ((ClassicClient) OpenClassic.getClient()).getMinecraft().fontRenderer;
		
		if(xCenter) {
			renderer.renderWithShadow(text, (int) x - renderer.getWidth(text), (int) y, 16777215, true);
		} else {
			renderer.renderWithShadow(text, (int) x, (int) y, 16777215, true);
		}
	}
	
	public void renderText(String text, float x, float y, int color) {
		this.renderText(text, x, y, color, true);
	}
	
	public void renderText(String text, float x, float y, int color, boolean xCenter) {
		FontRenderer renderer = ((ClassicClient) OpenClassic.getClient()).getMinecraft().fontRenderer;
		
		if(xCenter) {
			renderer.renderWithShadow(text, (int) x - renderer.getWidth(text) / 2, (int) y, color);
		} else {
			renderer.renderWithShadow(text, (int) x, (int) y, color);
		}
	}
	
	public void renderTextNoShadow(String text, float x, float y) {
		this.renderText(text, x, y, true);
	}
	
	public void renderTextNoShadow(String text, float x, float y, boolean xCenter) {
		this.renderText(text, x, y, 16777215, xCenter);
	}
	
	public void renderTextNoShadow(String text, float x, float y, int color) {
		this.renderText(text, x, y, color, true);
	}
	
	public void renderTextNoShadow(String text, float x, float y, int color, boolean xCenter) {
		FontRenderer renderer = ((ClassicClient) OpenClassic.getClient()).getMinecraft().fontRenderer;
		
		if(xCenter) {
			renderer.renderNoShadow(text, (int) x - renderer.getWidth(text) / 2, (int) y, color);
		} else {
			renderer.renderNoShadow(text, (int) x, (int) y, color);
		}
	}
	
	public void drawBox(float x1, float y1, float x2, float y2, int color) {
		float alpha = (color >>> 24) / 255F;
		float red = (color >> 16 & 255) / 255F;
		float green = (color >> 8 & 255) / 255F;
		float blue = (color & 255) / 255F;
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		this.glColor(red, green, blue, alpha);
		
		ShapeRenderer.instance.begin();
		ShapeRenderer.instance.vertex(x1, y2, 0);
		ShapeRenderer.instance.vertex(x2, y2, 0);
		ShapeRenderer.instance.vertex(x2, y1, 0);
		ShapeRenderer.instance.vertex(x1, y1, 0);
		ShapeRenderer.instance.end();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	public void color(int x1, int y1, int x2, int y2, int color) {
		this.color(x1, y1, x2, y2, color, color);
	}
	
	public void color(int x1, int y1, int x2, int y2, int color, int fadeTo) {
		float alpha = (color >>> 24) / 255F;
		float red = (color >> 16 & 255) / 255F;
		float green = (color >> 8 & 255) / 255F;
		float blue = (color & 255) / 255F;
		
		float alpha2 = (fadeTo >>> 24) / 255F;
		float red2 = (fadeTo >> 16 & 255) / 255F;
		float green2 = (fadeTo >> 8 & 255) / 255F;
		float blue2 = (fadeTo & 255) / 255F;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glBegin(GL11.GL_QUADS);
		
		this.glColor(red, green, blue, alpha);
		GL11.glVertex2f(x2, y1);
		GL11.glVertex2f(x1, y1);
		this.glColor(red2, green2, blue2, alpha2);
		GL11.glVertex2f(x1, y2);
		GL11.glVertex2f(x2, y2);
		
		GL11.glEnd();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	public void drawImage(int x, int y, int z, int imgX, int imgY, int imgWidth, int imgHeight) {
		ShapeRenderer.instance.begin();
		ShapeRenderer.instance.vertexUV(x, (y + imgHeight), z, imgX * 0.00390625F, (imgY + imgHeight) * 0.00390625F);
		ShapeRenderer.instance.vertexUV((x + imgWidth), (y + imgHeight), z, (imgX + imgWidth) * 0.00390625F, (imgY + imgHeight) * 0.00390625F);
		ShapeRenderer.instance.vertexUV((x + imgWidth), y, z, (imgX + imgWidth) * 0.00390625F, imgY * 0.00390625F);
		ShapeRenderer.instance.vertexUV(x, y, z, imgX * 0.00390625F, imgY * 0.00390625F);
		ShapeRenderer.instance.end();
	}

	@Override
	public int bindTexture(String file, boolean jar) {
		int id = GeneralUtils.getMinecraft().textureManager.bindTexture(file, jar);
		this.bindTexture(id);
		return id;
	}
	
	@Override
	public void bindTexture(int id) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		this.binded = id;
	}

	@Override
	public void glColor(float red, float green, float blue, float alpha) {
		GL11.glColor4f(red, green, blue, alpha);
	}
	
	@Override
	public int getDisplayWidth() {
		return Display.getWidth();
	}
	
	@Override
	public int getDisplayHeight() {
		return Display.getHeight();
	}

	@Override
	public void drawQuad(Quad quad, float x, float y, float z) {
		this.drawQuad(quad, x, y, z, 1);
	}
	
	@Override
	public void drawQuad(Quad quad, float x, float y, float z, float brightness) {
		ShapeRenderer.instance.begin();
		Integer id = GeneralUtils.getMinecraft().textureManager.textures.get(quad.getTexture().getParent().getTexture());
		if(id == null || id.intValue() != this.binded) {
			this.bindTexture(quad.getTexture().getParent().getTexture(), quad.getTexture().getParent().isInJar());
		}
		
		if(brightness >= 0) {
			ShapeRenderer.instance.color(brightness, brightness, brightness);
		}
		
		float ox1 = quad.getTexture().getX1();
		float oy1 = quad.getTexture().getY1();
		float x1 = quad.getTexture().getX1();
		float x2 = quad.getTexture().getX2();
		float y1 = quad.getTexture().getY1();
		float y2 = quad.getTexture().getY2();
		
		if(quad.getParent() instanceof CuboidModel) {
			BlockFace face = CuboidModel.quadToFace((CuboidModel) quad.getParent(), quad.getId());
			switch(face) {
				case UP:
					x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getX() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getZ() * quad.getTexture().getParent().getSubTextureHeight());
					// x and z
					break;
				case DOWN:
					x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(1).getX() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(2).getZ() * quad.getTexture().getParent().getSubTextureHeight());
					// x and z
					break;
				case NORTH:
					x1 = (int) (ox1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getZ() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
					// y and z
					break;
				case SOUTH:
					x1 = (int) (ox1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getZ() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
					// y and z
					break;
				case EAST:
					x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getX() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
					// x and y
					break;
				case WEST:
					x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getX() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
					// x and y
					break;
			}
		}
	
		float width = quad.getTexture().getParent().getWidth();
		float height = quad.getTexture().getParent().getHeight();
		ShapeRenderer.instance.vertexUV(x + quad.getVertex(0).getX(), y + quad.getVertex(0).getY(), z + quad.getVertex(0).getZ(), x2 / width, y2 / height);
		ShapeRenderer.instance.vertexUV(x + quad.getVertex(1).getX(), y + quad.getVertex(1).getY(), z + quad.getVertex(1).getZ(), x2 / width, y1 / height);
		ShapeRenderer.instance.vertexUV(x + quad.getVertex(2).getX(), y + quad.getVertex(2).getY(), z + quad.getVertex(2).getZ(), x1 / width, y1 / height);
		ShapeRenderer.instance.vertexUV(x + quad.getVertex(3).getX(), y + quad.getVertex(3).getY(), z + quad.getVertex(3).getZ(), x1 / width, y2 / height);
		
		ShapeRenderer.instance.end();
	}

	@Override
	public void drawScaledQuad(Quad quad, float x, float y, float z, float scale, float brightness) {
		ShapeRenderer.instance.begin();
		int id = GeneralUtils.getMinecraft().textureManager.textures.get(quad.getTexture().getParent().getTexture());
		if(id == -1 || id != this.binded) {
			this.bindTexture(quad.getTexture().getParent().getTexture(), quad.getTexture().getParent().isInJar());
		}

		if(brightness >= 0) {
			ShapeRenderer.instance.color(brightness, brightness, brightness);
		}

		float ox1 = quad.getTexture().getX1();
		float oy1 = quad.getTexture().getY1();
		float x1 = quad.getTexture().getX1();
		float x2 = quad.getTexture().getX2();
		float y1 = quad.getTexture().getY1();
		float y2 = quad.getTexture().getY2();
		
		if(quad.getParent() instanceof CuboidModel) {
			BlockFace face = CuboidModel.quadToFace((CuboidModel) quad.getParent(), quad.getId());
			switch(face) {
				case UP:
					x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getX() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getZ() * quad.getTexture().getParent().getSubTextureHeight());
					// x and z
					break;
				case DOWN:
					x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(1).getX() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(2).getZ() * quad.getTexture().getParent().getSubTextureHeight());
					// x and z
					break;
				case NORTH:
					x1 = (int) (ox1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getZ() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
					// y and z
					break;
				case SOUTH:
					x1 = (int) (ox1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getZ() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
					// y and z
					break;
				case EAST:
					x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getX() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
					// x and y
					break;
				case WEST:
					x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getX() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
					// x and y
					break;
			}
		}
	
		float width = quad.getTexture().getParent().getWidth();
		float height = quad.getTexture().getParent().getHeight();
		ShapeRenderer.instance.vertexUV(x + quad.getVertex(0).getX() * scale, y + quad.getVertex(0).getY() * scale, z + quad.getVertex(0).getZ() * scale, x2 / width, y2 / height);
		ShapeRenderer.instance.vertexUV(x + quad.getVertex(1).getX() * scale, y + quad.getVertex(1).getY() * scale, z + quad.getVertex(1).getZ() * scale, x2 / width, y1 / height);
		ShapeRenderer.instance.vertexUV(x + quad.getVertex(2).getX() * scale, y + quad.getVertex(2).getY() * scale, z + quad.getVertex(2).getZ() * scale, x1 / width, y1 / height);
		ShapeRenderer.instance.vertexUV(x + quad.getVertex(3).getX() * scale, y + quad.getVertex(3).getY() * scale, z + quad.getVertex(3).getZ() * scale, x1 / width, y2 / height);
		
		ShapeRenderer.instance.end();
	}
	
	public void drawCracks(Quad quad, int x, int y, int z, int crackTexture) {
		ShapeRenderer.instance.begin();
		this.bindTexture(BlockType.TERRAIN_TEXTURE.getTexture(), true);
		
		SubTexture texture = quad.getTexture().getParent().getSubTexture(crackTexture);
		float ox1 = texture.getX1();
		float oy1 = texture.getY1();
		float x1 = texture.getX1();
		float x2 = texture.getX2();
		float y1 = texture.getY1();
		float y2 = texture.getY2();
		
		if(quad.getParent() instanceof CuboidModel) {
			BlockFace face = CuboidModel.quadToFace((CuboidModel) quad.getParent(), quad.getId());
			switch(face) {
				case UP:
					x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getX() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getZ() * quad.getTexture().getParent().getSubTextureHeight());
					// x and z
					break;
				case DOWN:
					x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(1).getX() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(2).getZ() * quad.getTexture().getParent().getSubTextureHeight());
					// x and z
					break;
				case NORTH:
					x1 = (int) (ox1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getZ() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
					// y and z
					break;
				case SOUTH:
					x1 = (int) (ox1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getZ() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
					// y and z
					break;
				case EAST:
					x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getX() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
					// x and y
					break;
				case WEST:
					x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
					x2 = (int) (ox1 + quad.getVertex(2).getX() * quad.getTexture().getParent().getSubTextureWidth());
					y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
					y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
					// x and y
					break;
			}
		}
		
		float width = quad.getTexture().getParent().getWidth();
		float height = quad.getTexture().getParent().getHeight();
		ShapeRenderer.instance.vertexUV(x + quad.getVertex(0).getX(), y + quad.getVertex(0).getY(), z + quad.getVertex(0).getZ(), x2 / width, y2 / height);
		ShapeRenderer.instance.vertexUV(x + quad.getVertex(1).getX(), y + quad.getVertex(1).getY(), z + quad.getVertex(1).getZ(), x2 / width, y1 / height);
		ShapeRenderer.instance.vertexUV(x + quad.getVertex(2).getX(), y + quad.getVertex(2).getY(), z + quad.getVertex(2).getZ(), x1 / width, y1 / height);
		ShapeRenderer.instance.vertexUV(x + quad.getVertex(3).getX(), y + quad.getVertex(3).getY(), z + quad.getVertex(3).getZ(), x1 / width, y2 / height);
		
		ShapeRenderer.instance.end();
	}
	
	@Override
	public void drawTexture(Texture texture, float x, float y, float brightness) {
		this.drawTexture(texture, x, y, 0, brightness);
	}
	
	@Override
	public void drawTexture(Texture texture, float x, float y, float z, float brightness) {
		ShapeRenderer.instance.begin();
		if(GeneralUtils.getMinecraft().textureManager.textures.get(texture.getTexture()) == null || GeneralUtils.getMinecraft().textureManager.textures.get(texture.getTexture()) != GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)) {
			this.bindTexture(texture.getTexture(), texture.isInJar());
		}
		
		ShapeRenderer.instance.color(brightness, brightness, brightness);
		ShapeRenderer.instance.vertexUV(x, y, z, 0, 0);
		ShapeRenderer.instance.vertexUV(x, y + texture.getHeight(), z, 0, texture.getHeight() / texture.getHeight());
		ShapeRenderer.instance.vertexUV(x + texture.getWidth(), y + texture.getHeight(), z, texture.getWidth() / texture.getWidth(), texture.getHeight() / texture.getHeight());
		ShapeRenderer.instance.vertexUV(x + texture.getWidth(), y, z, texture.getWidth() / texture.getWidth(), 0);
		
		ShapeRenderer.instance.end();
	}
	
	@Override
	public void drawSubTex(SubTexture texture, float x, float y, float brightness) {
		this.drawSubTex(texture, x, y, 0, brightness);
	}
	
	@Override
	public void drawSubTex(SubTexture texture, float x, float y, float z, float brightness) {
		this.drawSubTex(texture, x, y, z, 1, brightness, brightness, brightness);
	}
	
	@Override
	public void drawSubTex(SubTexture texture, float x, float y, float z, float scale, float brightness) {
		this.drawSubTex(texture, x, y, z, scale, brightness, brightness, brightness);
	}

	@Override
	public void drawSubTex(SubTexture texture, float x, float y, float z, float scale, float r, float g, float b) {
		ShapeRenderer.instance.begin();
		Integer id = GeneralUtils.getMinecraft().textureManager.textures.get(texture.getParent().getTexture());
		if(id == null || id.intValue() != this.binded) {
			this.bindTexture(texture.getParent().getTexture(), texture.getParent().isInJar());
		}
		
		if(r >= 0 && g >= 0 && b >= 0) {
			ShapeRenderer.instance.color(r, g, b);
		}
		
		ShapeRenderer.instance.vertexUV(x, y, z, texture.getX1() / texture.getParent().getWidth(), texture.getY1() / texture.getParent().getHeight());
		ShapeRenderer.instance.vertexUV(x, y + ((texture.getY2() - texture.getY1()) * scale), z, texture.getX1() / texture.getParent().getWidth(), texture.getY2() / texture.getParent().getHeight());
		ShapeRenderer.instance.vertexUV(x + ((texture.getX2() - texture.getX1()) * scale), y + ((texture.getY2() - texture.getY1()) * scale), z, texture.getX2() / texture.getParent().getWidth(), texture.getY2() / texture.getParent().getHeight());
		ShapeRenderer.instance.vertexUV(x + ((texture.getX2() - texture.getX1()) * scale), y, z, texture.getX2() / texture.getParent().getWidth(), texture.getY1() / texture.getParent().getHeight());
		
		ShapeRenderer.instance.end();
	}
	
	@Override
	public boolean canRenderSide(BlockType block, int x, int y, int z, BlockFace face) {
		if(block == null) return false;
		BlockType relative = OpenClassic.getClient().getLevel().getBlockTypeAt(x + face.getModX(), y + face.getModY(), z + face.getModZ());
		if(block.isLiquid()) {
			if(relative == null) {
				return false;
			}
			
			if(Level.toMoving(relative) == Level.toMoving(block)) {
				return false;
			}
			
			if(y <= OpenClassic.getClient().getLevel().getWaterLevel() - 1 && (face.getModX() < 0 && x <= 0 || face.getModX() > 0 && x >= OpenClassic.getClient().getLevel().getWidth() - 1 || face.getModZ() < 0 && z <= 0 || face.getModZ() > 0 && z >= OpenClassic.getClient().getLevel().getDepth() - 1)) {
				return false;
			}
			
			return !relative.getPreventsRendering();
		}
		
		if(!(block.getModel() instanceof CubeModel)) {
			//if(block.getModel() instanceof CuboidModel) {
				// TODO: non-complete block logic
			//} else {
				return true;
			//}
		}
		
		if(relative == block) {
			return !relative.getPreventsOwnRendering();
		}
		
		if(block.isOpaque()) {
			return true;
		}
		
		return relative == null || !relative.getPreventsRendering();
	}
	
	@Override
	public float getBrightness(BlockType main, int x, int y, int z) {
		return main == VanillaBlock.LAVA || main == VanillaBlock.STATIONARY_LAVA ? 100 : ((ClientLevel) OpenClassic.getClient().getLevel()).getHandle().getBrightness(x, y, z);
	}
	
	public void spawnDestructionParticles(BlockType block, Level level, int x, int y, int z, ParticleManager particles) {
		for (int xMod = 0; xMod < 4; ++xMod) {
			for (int yMod = 0; yMod < 4; ++yMod) {
				for (int zMod = 0; zMod < 4; ++zMod) {
					float particleX = x + (xMod + 0.5F) / 4;
					float particleY = y + (yMod + 0.5F) / 4;
					float particleZ = z + (zMod + 0.5F) / 4;
					particles.spawnParticle(new TerrainParticle(level, particleX, particleY, particleZ, particleX - x - 0.5F, particleY - y - 0.5F, particleZ - z - 0.5F, block));
				}
			}
		}
	}

	public final void spawnBlockParticles(Level level, int x, int y, int z, int side, ParticleManager particles) {
		Model model = Blocks.fromId(level.getTile(x, y, z)).getModel();
		
		float particleX = x + rand.nextFloat() * (model.getSelectionBox(x, y, z).getX2() - model.getSelectionBox(x, y, z).getX1() - 0.1F * 2.0F) + 0.1F + model.getSelectionBox(x, y, z).getX1();
		float particleY = y + rand.nextFloat() * (model.getSelectionBox(x, y, z).getY2() - model.getSelectionBox(x, y, z).getY1() - 0.1F * 2.0F) + 0.1F + model.getSelectionBox(x, y, z).getY1();
		float particleZ = z + rand.nextFloat() * (model.getSelectionBox(x, y, z).getZ2() - model.getSelectionBox(x, y, z).getZ1() - 0.1F * 2.0F) + 0.1F + model.getSelectionBox(x, y, z).getZ1();
		if (side == 0) {
			particleY = y + model.getSelectionBox(x, y, z).getY1() - 0.1F;
		}

		if (side == 1) {
			particleY = y + model.getSelectionBox(x, y, z).getY2() + 0.1F;
		}

		if (side == 2) {
			particleZ = z + model.getSelectionBox(x, y, z).getZ1() - 0.1F;
		}

		if (side == 3) {
			particleZ = z + model.getSelectionBox(x, y, z).getZ2() + 0.1F;
		}

		if (side == 4) {
			particleX = x + model.getSelectionBox(x, y, z).getX1() - 0.1F;
		}

		if (side == 5) {
			particleX = x + model.getSelectionBox(x, y, z).getX2() + 0.1F;
		}

		particles.spawnParticle((new TerrainParticle(level, particleX, particleY, particleZ, 0.0F, 0.0F, 0.0F, Blocks.fromId(level.getTile(x, y, z)))).setPower(0.2F).scale(0.6F));
	}

	@Override
	public float getStringWidth(String string) {
		return GeneralUtils.getMinecraft().fontRenderer.getWidth(string);
	}
	
	@Override
	public void drawRotatedBlock(int x, int y, BlockType block) {
		this.drawRotatedBlock(x, y, block, 0);
	}
	
	@Override
	public void drawRotatedBlock(int x, int y, BlockType block, float scale) {
		if(block != null && block.getModel() != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(x, y, 0);
			GL11.glScalef(10, 10, 10);
			GL11.glTranslatef(1, 0, 8);
			GL11.glRotatef(-30, 1, 0, 0);
			GL11.glRotatef(45, 0, 1, 0);
			if(scale > 0) {
				GL11.glScalef(scale, scale, scale);
			}
	
			GL11.glTranslatef(-1.5F, 0.5F, 0.5F);
			GL11.glScalef(-1, -1, -1);
			ShapeRenderer.instance.begin();
			block.getModel().renderAll(-2, 0, 0, 1);
			ShapeRenderer.instance.end();
			GL11.glPopMatrix();
		}
	}

	@Override
	public void drawImage(BufferedImage image, int x, int y) {
		this.drawImage(image, x, y, 0);
	}
	
	@Override
	public void drawImage(BufferedImage image, int x, int y, int z) {
		GeneralUtils.getMinecraft().textureManager.bindTexture(image);
		
		this.glColor(1, 1, 1, 1);
		ShapeRenderer.instance.begin();
		ShapeRenderer.instance.vertexUV(x, y, z, 0, 0);
		ShapeRenderer.instance.vertexUV(x, y + image.getHeight(), z, 0, image.getHeight() / image.getHeight());
		ShapeRenderer.instance.vertexUV(x + image.getWidth(), y + image.getHeight(), z, image.getWidth() / image.getWidth(), image.getHeight() / image.getHeight());
		ShapeRenderer.instance.vertexUV(x + image.getWidth(), y, z, image.getWidth() / image.getWidth(), 0);
		ShapeRenderer.instance.end();
	}

	@Override
	public void setCulling(boolean enabled) {
		if(enabled) {
			GL11.glEnable(GL11.GL_CULL_FACE);
		} else {
			GL11.glDisable(GL11.GL_CULL_FACE);
		}
	}
	
}
