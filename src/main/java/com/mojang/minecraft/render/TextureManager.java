package com.mojang.minecraft.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.render.MipmapMode;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.settings.Settings;

import com.mojang.minecraft.render.animation.AnimatedTexture;

public class TextureManager {

	public HashMap<String, Integer> textures = new HashMap<String, Integer>();
	public HashMap<String, Boolean> jarTexture = new HashMap<String, Boolean>();
	public HashMap<Integer, BufferedImage> textureImgs = new HashMap<Integer, BufferedImage>();
	public IntBuffer textureBuffer = BufferUtils.createIntBuffer(1);
	public List<AnimatedTexture> animations = new ArrayList<AnimatedTexture>();
	public Settings settings;

	public TextureManager(Settings settings) {
		this.settings = settings;
	}

	public final int bindTexture(String file) {
		return this.bindTexture(file, true);
	}

	public final int bindTexture(String file, boolean jar) {
		if(this.textures.get(file) != null) {
			return this.textures.get(file);
		} else {
			try {
				this.textureBuffer.clear();
				GL11.glGenTextures(this.textureBuffer);
				int textureId = this.textureBuffer.get(0);

				BufferedImage img = null;
				if(!jar) {
					img = ImageIO.read(new FileInputStream(file));
				} else {
					String pack = OpenClassic.getGame().getConfig().getString("options.texture-pack");
					if(pack.equals("none")) {
						img = ImageIO.read(TextureManager.class.getResourceAsStream(file));
					} else {
						ZipFile zip = new ZipFile(new File(OpenClassic.getClient().getDirectory(), "texturepacks/" + pack));
						if(zip.getEntry(file.startsWith("/") ? file.substring(1, file.length()) : file) != null) {
							img = ImageIO.read(zip.getInputStream(zip.getEntry(file.startsWith("/") ? file.substring(1, file.length()) : file)));
						} else {
							img = ImageIO.read(TextureManager.class.getResourceAsStream(file));
						}

						IOUtils.closeQuietly(zip);
					}
				}

				this.bindTexture(img, textureId);
				this.textures.put(file, textureId);
				this.jarTexture.put(file, jar);
				return textureId;
			} catch(IOException e) {
				throw new RuntimeException(OpenClassic.getGame().getTranslator().translate("core.fail-texture"), e);
			}
		}
	}

	public final int bindTexture(BufferedImage image) {
		this.textureBuffer.clear();
		GL11.glGenTextures(this.textureBuffer);
		int textureId = this.textureBuffer.get(0);
		this.bindTexture(image, textureId);
		return textureId;
	}

	public void bindTexture(BufferedImage image, int textureId) {
		if(image == null) return;

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		if(this.settings.getBooleanSetting("options.smoothing").getValue() && RenderHelper.getHelper().getMipmapMode() != MipmapMode.NONE) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 2);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}

		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);

		for(int y = 0; y < image.getHeight(); y++) {
			for(int x = 0; x < image.getWidth(); x++) {
				int pixel = pixels[y * image.getWidth() + x];
				int red = (pixel >> 16) & 0xFF;
				int blue = pixel & 0xFF;
				int green = (pixel >> 8) & 0xFF;
				int alpha = (pixel >> 24) & 0xFF;

				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					green = (red * 30 + green * 70) / 100;
					blue = (red * 30 + blue * 70) / 100;
					red = (red * 30 + green * 59 + blue * 11) / 100;
				}

				buffer.put((byte) red);
				buffer.put((byte) green);
				buffer.put((byte) blue);
				buffer.put((byte) alpha);
			}
		}

		buffer.flip();
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		this.textureImgs.put(textureId, image);

		if(this.settings.getBooleanSetting("options.smoothing").getValue()) {
			switch(RenderHelper.getHelper().getMipmapMode()) {
				case GL30:
					GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
					break;
				case FRAMEBUFFER_EXT:
					EXTFramebufferObject.glGenerateMipmapEXT(GL11.GL_TEXTURE_2D);
					break;
			}
		}
	}

	public final void addAnimatedTexture(AnimatedTexture animation) {
		this.animations.add(animation);
		animation.animate();
	}

	public void clear() {
		this.textures.clear();
		this.jarTexture.clear();
		this.textureImgs.clear();
	}

}
