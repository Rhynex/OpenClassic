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

		for (int character = 0; character < 256; ++character) {
			int var6 = character % 16;
			int var7 = character / 16;
			int chWidth = 0;

			for (boolean var9 = false; chWidth < 8 && !var9; chWidth++) {
				int var10 = (var6 << 3) + chWidth;
				var9 = true;

				for (int var11 = 0; var11 < 8 && var9; ++var11) {
					int var12 = ((var7 << 3) + var11) * width;
					if ((fontData[var10 + var12] & 255) > 128) {
						var9 = false;
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
			int var7 = 0;

			for (int count = 0; count < chars.length; ++count) {
				if (chars[count] == '&' && chars.length > count + 1) {
					int code = "0123456789abcdef".indexOf(chars[count + 1]);
					if (code < 0) {
						code = 15;
					}

					int var9 = (code & 8) << 3;
					int var10 = (code & 1) * 191 + var9;
					int var11 = ((code & 2) >> 1) * 191 + var9;
					int blue = ((code & 4) >> 2) * 191 + var9;
					if (this.settings.anaglyph) {
						var9 = (code * 30 + var11 * 59 + var10 * 11) / 100;
						var11 = (code * 30 + var11 * 70) / 100;
						var10 = (code * 30 + var10 * 70) / 100;
						blue = var9;
					}

					int c = blue << 16 | var11 << 8 | var10;
					if (shadow) {
						c = (c & 16579836) >> 2;
					}

					Renderer.get().color(c);
					count += 2;
				}

				color = chars[count] % 16 << 3;
				int var9 = chars[count] / 16 << 3;
				float var13 = 7.99F;
				Renderer.get().vertexuv((x + var7), y + var13, 0.0F, color / 128.0F, (var9 + var13) / 128.0F);
				Renderer.get().vertexuv((x + var7) + var13, y + var13, 0.0F, (color + var13) / 128.0F, (var9 + var13) / 128.0F);
				Renderer.get().vertexuv((x + var7) + var13, y, 0.0F, (color + var13) / 128.0F, var9 / 128.0F);
				Renderer.get().vertexuv((x + var7), y, 0.0F, color / 128.0F, var9 / 128.0F);
				if (chars[count] < this.font.length) {
					var7 += this.font[chars[count]];
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
