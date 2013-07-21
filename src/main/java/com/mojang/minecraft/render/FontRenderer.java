package com.mojang.minecraft.render;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.render.Renderer;

import com.mojang.minecraft.GameSettings;

public final class FontRenderer {

	private int[] font = new int[256];
	private int fontId = 0;
	private GameSettings settings;

	public FontRenderer(GameSettings settings, String fontImage, TextureManager textures) {
		this.settings = settings;

		BufferedImage font;
		
		try {
			font = ImageIO.read(TextureManager.class.getResourceAsStream(fontImage));
		} catch (IOException e) {
			throw new RuntimeException(OpenClassic.getGame().getTranslator().translate("core.fail-font"), e);
		}

		int width = font.getWidth();
		int height = font.getHeight();
		int[] fontData = new int[width * height];
		font.getRGB(0, 0, width, height, fontData, 0, width);

		for (int character = 0; character < 256; character++) {
			int tx = character % 16;
			int ty = character / 16;
			int chWidth = 0;
			for (boolean empty = false; chWidth < 8 && !empty; chWidth++) {
				int xk = (tx << 3) + chWidth;
				empty = true;
				for (int y = 0; y < 8 && empty; y++) {
					int yk = ((ty << 3) + y) * width;
					if ((fontData[xk + yk] & 255) > 128) {
						empty = false;
					}
				}
			}

			if (character == 32) {
				chWidth = 4;
			}

			this.font[character] = chWidth;
		}

		this.fontId = textures.bindTexture(fontImage);
	}

	public final void renderWithShadow(String text, int x, int y, int color) {
		this.renderWithShadow(text, x, y, color, false);
	}
	
	public final void renderWithShadow(String text, int x, int y, int color, boolean scaled) {
		this.render(text, x + 1, y + 1, color, true, scaled);
		this.renderNoShadow(text, x, y, color, scaled);
	}

	public final void renderNoShadow(String text, int x, int y, int color) {
		this.renderNoShadow(text, x, y, color, false);
	}
	
	public final void renderNoShadow(String text, int x, int y, int color, boolean scaled) {
		this.render(text, x, y, color, false, scaled);
	}
	
	private void render(String text, int x, int y, int color, boolean shadow, boolean scaled) {
		if(scaled) {
			GL11.glScalef(2, 2, 2);
			GL11.glTranslatef(-(x / 2), -(y / 2), 0);
		}
		
		if (text != null) {
			char[] chars = text.toCharArray();
			if (shadow) {
				color = (color & 16579836) >> 2;
			}

			RenderHelper.getHelper().bindTexture(this.fontId);
			Renderer.get().begin();
			Renderer.get().color(color);
			int width = 0;
			for (int count = 0; count < chars.length; count++) {
				if (chars[count] == '&' && chars.length > count + 1) {
					int code = "0123456789abcdef".indexOf(chars[count + 1]);
					if (code < 0) {
						code = 15;
					}

					int alpha = (code & 8) << 3;
					int red = ((code & 4) >> 2) * 191 + alpha;
					int green = ((code & 2) >> 1) * 191 + alpha;
					int blue = (code & 1) * 191 + alpha;
					if (this.settings.anaglyph) {
						red = (code * 30 + green * 59 + blue * 11) / 100;
						green = (code * 30 + green * 70) / 100;
						blue = (code * 30 + blue * 70) / 100;
					}

					int c = red << 16 | green << 8 | blue;
					if (shadow) {
						c = (c & 16579836) >> 2;
					}

					Renderer.get().color(c);
					count += 2;
				}

				int tx = chars[count] % 16 << 3;
				int ty = chars[count] / 16 << 3;
				Renderer.get().vertexuv((x + width), y + 7.99F, 0.0F, tx / 128.0F, (ty + 7.99F) / 128.0F);
				Renderer.get().vertexuv((x + width) + 7.99F, y + 7.99F, 0.0F, (tx + 7.99F) / 128.0F, (ty + 7.99F) / 128.0F);
				Renderer.get().vertexuv((x + width) + 7.99F, y, 0.0F, (tx + 7.99F) / 128.0F, ty / 128.0F);
				Renderer.get().vertexuv((x + width), y, 0.0F, tx / 128.0F, ty / 128.0F);
				if (chars[count] < this.font.length) {
					width += this.font[chars[count]];
				}
			}

			Renderer.get().end();
		}
		
		if(scaled) {
			GL11.glTranslatef(x / 2, y / 2, 0);
			GL11.glScalef(0.5f, 0.5f, 0.5f);
		}
	}

	public final int getWidth(String string) {
		if (string == null) {
			return 0;
		} else {
			char[] chars = string.toCharArray();
			int width = 0;

			for (int index = 0; index < chars.length; index++) {
				if (chars[index] == '&') {
					index++;
				} else if (chars[index] < this.font.length) {
					width += this.font[chars[index]];
				}
			}

			return width;
		}
	}
}
