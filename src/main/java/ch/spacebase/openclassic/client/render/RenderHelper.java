package ch.spacebase.openclassic.client.render;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.model.CuboidModel;
import ch.spacebase.openclassic.api.block.model.Model;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.entity.model.Vector;
import com.mojang.minecraft.entity.particle.ParticleManager;
import com.mojang.minecraft.entity.particle.TerrainParticle;
import com.mojang.minecraft.entity.player.LocalPlayer;
import com.mojang.minecraft.render.FontRenderer;

public class RenderHelper {

	private static final Random rand = new Random();
	private static final RenderHelper helper = new RenderHelper();
	
	/**
	 * Gets the RenderHelper instance.
	 * @return The RenderHelper instance.
	 */
	public static RenderHelper getHelper() {
		return helper;
	}

	private int bound = -1;
	private MipmapMode mipmap = MipmapMode.NONE;
	
	public void init() {
		if(GLContext.getCapabilities().OpenGL30) {
			this.mipmap = MipmapMode.GL30;
		} else if(GLContext.getCapabilities().GL_EXT_framebuffer_object) {
			this.mipmap = MipmapMode.FRAMEBUFFER_EXT;
		} else if(GLContext.getCapabilities().OpenGL14) {
			this.mipmap = MipmapMode.GL14;
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		}
	}
	
	public void drawDefaultBG(int x, int y, int width, int height) {
		this.bindTexture("/textures/gui/dirt.png", true);
		Renderer.get().begin();
		Renderer.get().color(4210752);
		Renderer.get().vertexuv(x, y + height, 0, 0, height / 64);
		Renderer.get().vertexuv(x + width, y + height, 0, width / 64, height / 64);
		Renderer.get().vertexuv(x + width, y, 0, width / 64, 0);
		Renderer.get().vertexuv(x, y, 0, 0, 0);
		Renderer.get().end();
	}

	public void drawBlackBG(int x, int y, int width, int height) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Renderer.get().begin();
		Renderer.get().color(0);
		Renderer.get().vertexuv(x, y + height, 0, 0, 0);
		Renderer.get().vertexuv(x + width, y + height, 0, 0, 0);
		Renderer.get().vertexuv(x + width, y, 0, 0, 0);
		Renderer.get().vertexuv(x, y, 0, 0, 0);
		Renderer.get().end();
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

		Renderer.get().begin();
		Renderer.get().vertex(x1, y2, 0);
		Renderer.get().vertex(x2, y2, 0);
		Renderer.get().vertex(x2, y1, 0);
		Renderer.get().vertex(x1, y1, 0);
		Renderer.get().end();

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

	public void drawSubImage(int x, int y, int z, int imgX, int imgY, int imgWidth, int imgHeight) {
		Renderer.get().begin();
		Renderer.get().vertexuv(x, (y + imgHeight), z, imgX * 0.00390625F, (imgY + imgHeight) * 0.00390625F);
		Renderer.get().vertexuv((x + imgWidth), (y + imgHeight), z, (imgX + imgWidth) * 0.00390625F, (imgY + imgHeight) * 0.00390625F);
		Renderer.get().vertexuv((x + imgWidth), y, z, (imgX + imgWidth) * 0.00390625F, imgY * 0.00390625F);
		Renderer.get().vertexuv(x, y, z, imgX * 0.00390625F, imgY * 0.00390625F);
		Renderer.get().end();
	}

	public int bindTexture(String file, boolean jar) {
		int id = GeneralUtils.getMinecraft().textureManager.bindTexture(file, jar);
		this.bindTexture(id);
		return id;
	}

	public void bindTexture(int id) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		this.bound = id;
	}

	public void glColor(float red, float green, float blue, float alpha) {
		GL11.glColor4f(red, green, blue, alpha);
	}

	public int getDisplayWidth() {
		return Display.getWidth();
	}

	public int getDisplayHeight() {
		return Display.getHeight();
	}

	public void drawQuad(Quad quad, float x, float y, float z) {
		this.drawQuad(quad, x, y, z, 1, false);
	}
	
	public static BlockFace quadToFace(int quad) {
		switch(quad) {
			case 0:
				return BlockFace.DOWN;
			case 1:
				return BlockFace.UP;
			case 2:
				return BlockFace.WEST;
			case 3:
				return BlockFace.EAST;
			case 4:
				return BlockFace.SOUTH;
			case 5:
				return BlockFace.NORTH;
			default:
				return null;
		}
	}

	public void drawQuad(Quad quad, float x, float y, float z, float brightness, boolean batch) {
		if(!batch) {
			Renderer.get().begin();
		}
		
		Integer id = GeneralUtils.getMinecraft().textureManager.textures.get(quad.getTexture().getParent().getTexture());
		if(id == null || id.intValue() != this.bound) {
			this.bindTexture(quad.getTexture().getParent().getTexture(), quad.getTexture().getParent().isInJar());
		}
		
		float ox1 = quad.getTexture().getX1();
		float oy1 = quad.getTexture().getY1();
		float x1 = quad.getTexture().getX1();
		float x2 = quad.getTexture().getX2();
		float y1 = quad.getTexture().getY1();
		float y2 = quad.getTexture().getY2();
		
		if(quad.getParent() instanceof CuboidModel) {
			BlockFace face = quadToFace(quad.getId());
			boolean againstSurface = false;
			if(!((CuboidModel) quad.getParent()).isFullCube()) {
				switch(face) {
					case UP:
						x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
						x2 = (int) (ox1 + quad.getVertex(2).getX() * quad.getTexture().getParent().getSubTextureWidth());
						y1 = (int) (oy1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureHeight());
						y2 = (int) (oy1 + quad.getVertex(1).getZ() * quad.getTexture().getParent().getSubTextureHeight());
						if(quad.getVertex(0).getY() == 0 || quad.getVertex(0).getY() == 1) {
							againstSurface = true;
						}
						
						// x and z
						break;
					case DOWN:
						x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
						x2 = (int) (ox1 + quad.getVertex(1).getX() * quad.getTexture().getParent().getSubTextureWidth());
						y1 = (int) (oy1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureHeight());
						y2 = (int) (oy1 + quad.getVertex(2).getZ() * quad.getTexture().getParent().getSubTextureHeight());
						if(quad.getVertex(0).getY() == 0 || quad.getVertex(0).getY() == 1) {
							againstSurface = true;
						}
						
						// x and z
						break;
					case NORTH:
						x1 = (int) (ox1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureWidth());
						x2 = (int) (ox1 + quad.getVertex(2).getZ() * quad.getTexture().getParent().getSubTextureWidth());
						y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
						y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
						if(quad.getVertex(0).getX() == 0 || quad.getVertex(0).getX() == 1) {
							againstSurface = true;
						}
						
						// y and z
						break;
					case SOUTH:
						x1 = (int) (ox1 + quad.getVertex(0).getZ() * quad.getTexture().getParent().getSubTextureWidth());
						x2 = (int) (ox1 + quad.getVertex(2).getZ() * quad.getTexture().getParent().getSubTextureWidth());
						y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
						y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
						if(quad.getVertex(0).getX() == 0 || quad.getVertex(0).getX() == 1) {
							againstSurface = true;
						}
						
						// y and z
						break;
					case EAST:
						x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
						x2 = (int) (ox1 + quad.getVertex(2).getX() * quad.getTexture().getParent().getSubTextureWidth());
						y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
						y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
						if(quad.getVertex(0).getZ() == 0 || quad.getVertex(0).getZ() == 1) {
							againstSurface = true;
						}
						
						// x and y
						break;
					case WEST:
						x1 = (int) (ox1 + quad.getVertex(0).getX() * quad.getTexture().getParent().getSubTextureWidth());
						x2 = (int) (ox1 + quad.getVertex(2).getX() * quad.getTexture().getParent().getSubTextureWidth());
						y1 = (int) (oy1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
						y2 = (int) (oy1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
						if(quad.getVertex(0).getZ() == 0 || quad.getVertex(0).getZ() == 1) {
							againstSurface = true;
						}
						
						// x and y
						break;
				}
			} else {
				againstSurface = true;
			}
			
			if(batch) {
				if(againstSurface && !this.canRenderSide(OpenClassic.getClient().getLevel().getBlockTypeAt((int) x, (int) y, (int) z), quad.getParent(), (int) x, (int) y, (int) z, face)) {
					return;
				}
				
				brightness = OpenClassic.getClient().getLevel().getBrightness((int) x + face.getModX(), (int) y + face.getModY(), (int) z + face.getModZ());
			}
			
			float mod = 0;
			switch(face) {
				case DOWN:
					mod = 0.5F;
					break;
				case UP:
					mod = 1;
					break;
				case WEST:
				case EAST:
					mod = 0.8F;
					break;
				case SOUTH:
				case NORTH:
					mod = 0.6F;
					break;
			}
			
			brightness = brightness * mod;
		}
		
		if(brightness >= 0) {
			Renderer.get().color(brightness, brightness, brightness);
		}
		
		float width = quad.getTexture().getParent().getWidth();
		float height = quad.getTexture().getParent().getHeight();
		Renderer.get().vertexuv(x + quad.getVertex(0).getX(), y + quad.getVertex(0).getY(), z + quad.getVertex(0).getZ(), x2 / width, y2 / height);
		Renderer.get().vertexuv(x + quad.getVertex(1).getX(), y + quad.getVertex(1).getY(), z + quad.getVertex(1).getZ(), x2 / width, y1 / height);
		Renderer.get().vertexuv(x + quad.getVertex(2).getX(), y + quad.getVertex(2).getY(), z + quad.getVertex(2).getZ(), x1 / width, y1 / height);
		Renderer.get().vertexuv(x + quad.getVertex(3).getX(), y + quad.getVertex(3).getY(), z + quad.getVertex(3).getZ(), x1 / width, y2 / height);

		if(!batch) {
			Renderer.get().end();
		}
	}

	public void drawScaledQuad(Quad quad, float x, float y, float z, float scale, float brightness) {
		Renderer.get().begin();
		int id = GeneralUtils.getMinecraft().textureManager.textures.get(quad.getTexture().getParent().getTexture());
		if(id == -1 || id != this.bound) {
			this.bindTexture(quad.getTexture().getParent().getTexture(), quad.getTexture().getParent().isInJar());
		}

		if(brightness >= 0) {
			Renderer.get().color(brightness, brightness, brightness);
		}

		float ox1 = quad.getTexture().getX1();
		float oy1 = quad.getTexture().getY1();
		float x1 = quad.getTexture().getX1();
		float x2 = quad.getTexture().getX2();
		float y1 = quad.getTexture().getY1();
		float y2 = quad.getTexture().getY2();

		if(quad.getParent() instanceof CuboidModel && !((CuboidModel) quad.getParent()).isFullCube()) {
			BlockFace face = quadToFace(quad.getId());
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
		Renderer.get().vertexuv(x + quad.getVertex(0).getX() * scale, y + quad.getVertex(0).getY() * scale, z + quad.getVertex(0).getZ() * scale, x2 / width, y2 / height);
		Renderer.get().vertexuv(x + quad.getVertex(1).getX() * scale, y + quad.getVertex(1).getY() * scale, z + quad.getVertex(1).getZ() * scale, x2 / width, y1 / height);
		Renderer.get().vertexuv(x + quad.getVertex(2).getX() * scale, y + quad.getVertex(2).getY() * scale, z + quad.getVertex(2).getZ() * scale, x1 / width, y1 / height);
		Renderer.get().vertexuv(x + quad.getVertex(3).getX() * scale, y + quad.getVertex(3).getY() * scale, z + quad.getVertex(3).getZ() * scale, x1 / width, y2 / height);

		Renderer.get().end();
	}

	public void drawCracks(Quad quad, int x, int y, int z, int crackTexture) {
		Renderer.get().begin();
		Renderer.get().disableColors();
		this.bindTexture(BlockType.TERRAIN_TEXTURE.getTexture(), true);

		SubTexture texture = quad.getTexture().getParent().getSubTexture(crackTexture);
		float ox1 = texture.getX1();
		float oy1 = texture.getY1();
		float x1 = texture.getX1();
		float x2 = texture.getX2();
		float y1 = texture.getY1();
		float y2 = texture.getY2();

		if(quad.getParent() instanceof CuboidModel && !((CuboidModel) quad.getParent()).isFullCube()) {
			BlockFace face = quadToFace(quad.getId());
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
		Renderer.get().vertexuv(x + quad.getVertex(0).getX(), y + quad.getVertex(0).getY(), z + quad.getVertex(0).getZ(), x2 / width, y2 / height);
		Renderer.get().vertexuv(x + quad.getVertex(1).getX(), y + quad.getVertex(1).getY(), z + quad.getVertex(1).getZ(), x2 / width, y1 / height);
		Renderer.get().vertexuv(x + quad.getVertex(2).getX(), y + quad.getVertex(2).getY(), z + quad.getVertex(2).getZ(), x1 / width, y1 / height);
		Renderer.get().vertexuv(x + quad.getVertex(3).getX(), y + quad.getVertex(3).getY(), z + quad.getVertex(3).getZ(), x1 / width, y2 / height);

		Renderer.get().end();
	}

	public void drawTexture(Texture texture, float x, float y, float brightness) {
		this.drawTexture(texture, x, y, 0, brightness);
	}

	public void drawTexture(Texture texture, float x, float y, float z, float brightness) {
		Renderer.get().begin();
		if(GeneralUtils.getMinecraft().textureManager.textures.get(texture.getTexture()) == null || GeneralUtils.getMinecraft().textureManager.textures.get(texture.getTexture()) != GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)) {
			this.bindTexture(texture.getTexture(), texture.isInJar());
		}

		Renderer.get().color(brightness, brightness, brightness);
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
		this.drawSubTex(texture, x, y, z, 1, brightness, brightness, brightness);
	}

	public void drawSubTex(SubTexture texture, float x, float y, float z, float scale, float brightness) {
		this.drawSubTex(texture, x, y, z, scale, brightness, brightness, brightness);
	}

	public void drawSubTex(SubTexture texture, float x, float y, float z, float scale, float r, float g, float b) {
		Renderer.get().begin();
		Integer id = GeneralUtils.getMinecraft().textureManager.textures.get(texture.getParent().getTexture());
		if(id == null || id.intValue() != this.bound) {
			this.bindTexture(texture.getParent().getTexture(), texture.getParent().isInJar());
		}

		if(r >= 0 && g >= 0 && b >= 0) {
			Renderer.get().color(r, g, b);
		}

		Renderer.get().vertexuv(x, y, z, texture.getX1() / texture.getParent().getWidth(), texture.getY1() / texture.getParent().getHeight());
		Renderer.get().vertexuv(x, y + ((texture.getY2() - texture.getY1()) * scale), z, texture.getX1() / texture.getParent().getWidth(), texture.getY2() / texture.getParent().getHeight());
		Renderer.get().vertexuv(x + ((texture.getX2() - texture.getX1()) * scale), y + ((texture.getY2() - texture.getY1()) * scale), z, texture.getX2() / texture.getParent().getWidth(), texture.getY2() / texture.getParent().getHeight());
		Renderer.get().vertexuv(x + ((texture.getX2() - texture.getX1()) * scale), y, z, texture.getX2() / texture.getParent().getWidth(), texture.getY1() / texture.getParent().getHeight());

		Renderer.get().end();
	}
	
	public void drawStretchedSubTex(SubTexture texture, float x, float y, float width, float height) {
		Renderer.get().begin();
		Integer id = GeneralUtils.getMinecraft().textureManager.textures.get(texture.getParent().getTexture());
		if(id == null || id.intValue() != this.bound) {
			this.bindTexture(texture.getParent().getTexture(), texture.getParent().isInJar());
		}
		
		Renderer.get().color((float) 1, (float) 1, (float) 1);
		Renderer.get().vertexuv(x, y, 0, texture.getX1() / texture.getParent().getWidth(), texture.getY1() / texture.getParent().getHeight());
		Renderer.get().vertexuv(x, y + height, 0, texture.getX1() / texture.getParent().getWidth(), texture.getY2() / texture.getParent().getHeight());
		Renderer.get().vertexuv(x + width, y + height, 0, texture.getX2() / texture.getParent().getWidth(), texture.getY2() / texture.getParent().getHeight());
		Renderer.get().vertexuv(x + width, y, 0, texture.getX2() / texture.getParent().getWidth(), texture.getY1() / texture.getParent().getHeight());

		Renderer.get().end();
	}

	public boolean canRenderSide(BlockType block, Model model, int x, int y, int z, BlockFace face) {
		if(block == null) return false;
		BlockType relative = OpenClassic.getClient().getLevel().getBlockTypeAt(x + face.getModX(), y + face.getModY(), z + face.getModZ());
		if(block.isLiquid()) {
			if(relative == null) {
				return false;
			}

			if(relative.getLiquidName() != null && relative.getLiquidName().equals(block.getLiquidName())) {
				return false;
			}

			if(y <= OpenClassic.getClient().getLevel().getWaterLevel() - 1 && (face.getModX() < 0 && x <= 0 || face.getModX() > 0 && x >= OpenClassic.getClient().getLevel().getWidth() - 1 || face.getModZ() < 0 && z <= 0 || face.getModZ() > 0 && z >= OpenClassic.getClient().getLevel().getDepth() - 1)) {
				return false;
			}

			return !relative.getPreventsRendering();
		}
		
		if(relative == block) {
			return !relative.getPreventsOwnRendering();
		}

		return relative == null || !relative.getPreventsRendering();
	}

	public float getBrightness(BlockType main, int x, int y, int z) {
		return ((ClientLevel) OpenClassic.getClient().getLevel()).getBrightness(x, y, z);
	}

	public void spawnDestructionParticles(BlockType block, ClientLevel level, int x, int y, int z, ParticleManager particles) {
		for(int xMod = 0; xMod < 4; xMod++) {
			for(int yMod = 0; yMod < 4; yMod++) {
				for(int zMod = 0; zMod < 4; zMod++) {
					float particleX = x + (xMod + 0.5F) / 4;
					float particleY = y + (yMod + 0.5F) / 4;
					float particleZ = z + (zMod + 0.5F) / 4;
					particles.spawnParticle(new TerrainParticle(level, particleX, particleY, particleZ, particleX - x - 0.5F, particleY - y - 0.5F, particleZ - z - 0.5F, block));
				}
			}
		}
	}

	public final void spawnBlockParticles(ClientLevel level, int x, int y, int z, int side, ParticleManager particles) {
		Model model = level.getBlockTypeAt(x, y, z).getModel();

		float particleX = x + rand.nextFloat() * (model.getSelectionBox(x, y, z).getX2() - model.getSelectionBox(x, y, z).getX1() - 0.1F * 2.0F) + 0.1F + model.getSelectionBox(x, y, z).getX1();
		float particleY = y + rand.nextFloat() * (model.getSelectionBox(x, y, z).getY2() - model.getSelectionBox(x, y, z).getY1() - 0.1F * 2.0F) + 0.1F + model.getSelectionBox(x, y, z).getY1();
		float particleZ = z + rand.nextFloat() * (model.getSelectionBox(x, y, z).getZ2() - model.getSelectionBox(x, y, z).getZ1() - 0.1F * 2.0F) + 0.1F + model.getSelectionBox(x, y, z).getZ1();
		if(side == 0) {
			particleY = y + model.getSelectionBox(x, y, z).getY1() - 0.1F;
		}

		if(side == 1) {
			particleY = y + model.getSelectionBox(x, y, z).getY2() + 0.1F;
		}

		if(side == 2) {
			particleZ = z + model.getSelectionBox(x, y, z).getZ1() - 0.1F;
		}

		if(side == 3) {
			particleZ = z + model.getSelectionBox(x, y, z).getZ2() + 0.1F;
		}

		if(side == 4) {
			particleX = x + model.getSelectionBox(x, y, z).getX1() - 0.1F;
		}

		if(side == 5) {
			particleX = x + model.getSelectionBox(x, y, z).getX2() + 0.1F;
		}

		particles.spawnParticle((new TerrainParticle(level, particleX, particleY, particleZ, 0.0F, 0.0F, 0.0F, level.getBlockTypeAt(x, y, z))).setPower(0.2F).scale(0.6F));
	}

	public float getStringWidth(String string) {
		return GeneralUtils.getMinecraft().fontRenderer.getWidth(string);
	}

	public void drawRotatedBlock(int x, int y, BlockType block) {
		this.drawRotatedBlock(x, y, block, 0);
	}

	public void drawRotatedBlock(int x, int y, BlockType block, float scale) {
		this.drawRotatedBlock(x, y, block, scale, 0);
	}
	
	public void drawRotatedBlock(int x, int y, BlockType block, float scale, int popTime) {
		if(block != null && block.getModel() != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(x, y, -50);

			if(popTime > 0) {
				float off = (popTime - GeneralUtils.getMinecraft().timer.delta) / 5;
				RenderHelper.getHelper().translate(10, (-MathHelper.sin(off * off * MathHelper.PI) * 8) + 10, 0);
				RenderHelper.getHelper().scale(MathHelper.sin(off * off * MathHelper.PI) + 1, MathHelper.sin(off * MathHelper.PI) + 1, 1);
				RenderHelper.getHelper().translate(-10, -10, 0);
			}

			GL11.glScalef(10, 10, 10);
			GL11.glTranslatef(1, 0, 8);
			GL11.glRotatef(-30, 1, 0, 0);
			GL11.glRotatef(45, 0, 1, 0);
			GL11.glScalef(2, 2, 2);
			if(scale > 0) {
				GL11.glScalef(scale, scale, scale);
			}

			GL11.glTranslatef(-1.5F, 0.5F, 0.5F);
			GL11.glScalef(-1, -1, -1);
			block.getModel().renderAll(-2, 0, 0, 1);
			GL11.glPopMatrix();
		}
	}

	public void drawImage(BufferedImage image, int x, int y) {
		this.drawImage(image, x, y, 0);
	}

	public void drawImage(BufferedImage image, int x, int y, int z) {
		int bound = this.bound;
		GeneralUtils.getMinecraft().textureManager.bindTexture(image);

		this.glColor(1, 1, 1, 1);
		Renderer.get().begin();
		Renderer.get().vertexuv(x, y, z, 0, 0);
		Renderer.get().vertexuv(x, y + image.getHeight(), z, 0, image.getHeight() / image.getHeight());
		Renderer.get().vertexuv(x + image.getWidth(), y + image.getHeight(), z, image.getWidth() / image.getWidth(), image.getHeight() / image.getHeight());
		Renderer.get().vertexuv(x + image.getWidth(), y, z, image.getWidth() / image.getWidth(), 0);
		Renderer.get().end();
		this.bindTexture(bound);
	}

	public void setCulling(boolean enabled) {
		if(enabled) {
			GL11.glEnable(GL11.GL_CULL_FACE);
		} else {
			GL11.glDisable(GL11.GL_CULL_FACE);
		}
	}

	public FloatBuffer getParamBuffer(float param1, float param2, float param3, float param4) {
		FloatBuffer paramBuffer = BufferUtils.createFloatBuffer(16);
		paramBuffer.put(param1).put(param2).put(param3).put(param4);
		paramBuffer.flip();
		return paramBuffer;
	}

	public void ortho() {
		int width = Display.getWidth();//this.getGuiWidth();
		int height = Display.getHeight();//this.getGuiHeight();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, width, height, 0, 100, 300);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0, 0, -200);
	}

	public void setLighting(boolean lighting) {
		if(!lighting) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_COLOR_BUFFER_BIT);
		} else {
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_COLOR_BUFFER_BIT);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
			Vector vec = new Vector(0.0F, -1.0F, 0.5F).normalize();
			GL11.glLight(GL11.GL_COLOR_BUFFER_BIT, GL11.GL_POSITION, this.getParamBuffer(vec.x, vec.y, vec.z, 0));
			GL11.glLight(GL11.GL_COLOR_BUFFER_BIT, GL11.GL_DIFFUSE, this.getParamBuffer(0.3F, 0.3F, 0.3F, 1));
			GL11.glLight(GL11.GL_COLOR_BUFFER_BIT, GL11.GL_AMBIENT, this.getParamBuffer(0, 0, 0, 1));
			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getParamBuffer(0.7F, 0.7F, 0.7F, 1));
		}
	}

	public Vector getPlayerVector(LocalPlayer player, float dt) {
		float x = player.xo + (player.x - player.xo) * dt;
		float y = player.yo + (player.y - player.yo) * dt;
		float z = player.zo + (player.z - player.zo) * dt;
		return new Vector(x, y, z);
	}

	public void hurtEffect(LocalPlayer player, float dt) {
		float effect = player.hurtTime - dt;
		if(player.health <= 0) {
			dt += player.deathTime;
			GL11.glRotatef(40.0F - 8000.0F / (dt + 200.0F), 0, 0, 1);
		}

		if(effect >= 0) {
			effect = MathHelper.sin((effect /= player.hurtDuration) * effect * effect * effect * MathHelper.PI);
			GL11.glRotatef(-player.hurtDir, 0, 1, 0);
			GL11.glRotatef(-effect * 14.0F, 0, 0, 1);
			GL11.glRotatef(player.hurtDir, 0, 1, 0);
		}
	}

	public void applyBobbing(LocalPlayer player, float dt) {
		float dist = player.walkDist + (player.walkDist - player.walkDistO) * dt;
		float bob = player.oBob + (player.bob - player.oBob) * dt;
		float tilt = player.oTilt + (player.tilt - player.oTilt) * dt;
		GL11.glTranslatef(MathHelper.sin(dist * MathHelper.PI) * bob * 0.5F, -Math.abs(MathHelper.cos(dist * MathHelper.PI) * bob), 0);
		GL11.glRotatef(MathHelper.sin(dist * MathHelper.PI) * bob * 3.0F, 0, 0, 1);
		GL11.glRotatef(Math.abs(MathHelper.cos(dist * MathHelper.PI + 0.2F) * bob) * 5.0F, 1, 0, 0);
		GL11.glRotatef(tilt, 1, 0, 0);
	}
	
	public void pushMatrix() {
		GL11.glPushMatrix();
	}
	
	public void popMatrix() {
		GL11.glPopMatrix();
	}
	
	public void scale(float x, float y, float z) {
		GL11.glScalef(x, y, z);
	}

	public void translate(float x, float y, float z) {
		GL11.glTranslatef(x, y, z);
	}

	public void rotate(float angle, float x, float y, float z) {
		GL11.glRotatef(angle, x, y, z);
	}

	public void enableBlend() {
		GL11.glEnable(GL11.GL_BLEND);
	}

	public void disableBlend() {
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	public void drawTranslucentBox(int x, int y, int width, int height) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor4f(0, 0, 0, 0.7F);
		GL11.glVertex2f(x + width, y);
		GL11.glVertex2f(x, y);
		GL11.glColor4f(0.2F, 0.2F, 0.2F, 0.8F);
		GL11.glVertex2f(x, y + height);
		GL11.glVertex2f(x + width, y + height);
		GL11.glEnd();

		RenderHelper.getHelper().disableBlend();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	public MipmapMode getMipmapMode() {
		return this.mipmap;
	}

}
