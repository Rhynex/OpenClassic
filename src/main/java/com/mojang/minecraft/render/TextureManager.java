package com.mojang.minecraft.render;

import com.mojang.minecraft.GameSettings;
import com.mojang.minecraft.render.animation.AnimatedTexture;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

public class TextureManager {

	public HashMap<String, Integer> textures = new HashMap<String, Integer>();
	public HashMap<String, Boolean> jarTexture = new HashMap<String, Boolean>();
	public HashMap<Integer, BufferedImage> textureImgs = new HashMap<Integer, BufferedImage>();
	public IntBuffer textureBuffer = BufferUtils.createIntBuffer(1);
	public List<AnimatedTexture> animations = new ArrayList<AnimatedTexture>();
	public GameSettings settings;

	public TextureManager(GameSettings settings) {
		this.settings = settings;
	}

	public final int bindTexture(String file) {
		return this.bindTexture(file, true);
	}

	public final int bindTexture(String file, boolean jar) {
		if (this.textures.get(file) != null) {
			return this.textures.get(file);
		} else {
			try {
				this.textureBuffer.clear();
				GL11.glGenTextures(this.textureBuffer);
				int textureId = this.textureBuffer.get(0);

				BufferedImage img = !jar ? ImageIO.read(new FileInputStream(file)) : ImageIO.read(TextureManager.class.getResourceAsStream(file));
				this.bindTexture(img, textureId);

				if(file.contains("logo.png")) {
					System.out.println(img.getWidth() + ", " + img.getHeight());
				}

				this.textures.put(file, textureId);
				this.jarTexture.put(file, jar);
				return textureId;
			} catch (IOException e) {
				throw new RuntimeException("Failed to bind texture!", e);
			}
		}
	}

	public final int bindTexture(BufferedImage image) {
		this.textureBuffer.clear();
		GL11.glGenTextures(this.textureBuffer);
		int textureId = this.textureBuffer.get(0);
		this.bindTexture(image, textureId);
		this.textureImgs.put(textureId, image);
		return textureId;
	}

	public void bindTexture(BufferedImage image, int textureId) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		if(this.settings.smoothing) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 2);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}

		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);

		for(int y = 0; y < image.getHeight(); y++){
			for(int x = 0; x < image.getWidth(); x++){
				int pixel = pixels[y * image.getWidth() + x];
				int red = (pixel >> 16) & 0xFF;
				int blue = pixel & 0xFF;
				int green = (pixel >> 8) & 0xFF;
				int alpha = (pixel >> 24) & 0xFF;

				if (this.settings.anaglyph) {
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
	}

	public final void addAnimatedTexture(AnimatedTexture animation) {
		this.animations.add(animation);
		animation.animate();
	}
}
