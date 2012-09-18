package ch.spacebase.openclassic.client.render;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;


import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.ClassicClient;

public class TextRenderer {

	private int res;
	
	private final String fontFile;
	private SubTexture texMap[] = new SubTexture[256];
	private Texture tex;
	
	public TextRenderer(String fontFile) {
		this.fontFile = fontFile;
		BufferedImage font;
		
		try {
			font = ImageIO.read(ClassicClient.class.getResourceAsStream(this.fontFile));
		} catch (IOException e) {
			return;
		}

		this.res = font.getWidth() / 16;
		this.tex = new Texture(this.fontFile, true, font.getWidth(), font.getHeight(), this.res);
		int[] fontData = new int[font.getWidth() * font.getHeight()];
		font.getRGB(0, 0, font.getWidth(), font.getHeight(), fontData, 0, font.getWidth());
		
		for(int c = 0; c < 256; c++) {
			int charx = c % 16;
			int chary = c / 16;
			int wid = 0;
			
			boolean stopping = false;
			for (wid = 0; wid < 8 && !stopping; wid++) {
				int x = (charx * this.res) + wid;
				stopping = true;

				for (int high = 0; high < 8 && stopping; high++) {
					int y = ((chary * this.res) + high) * font.getWidth();
					if ((fontData[x + y] & 255) > 128) {
						stopping = false;
					}
				}
			}

			if(c == ' ') {
				wid = this.res / 2;
			}
			
			this.texMap[c] = new SubTexture(this.tex, c, charx * this.res, chary * this.res, wid, this.res);
		}
	}
	
	public void render(String text, float x, float y) {
		this.render(text, x, y, 16777215);
	}
	
	public void render(String text, float x, float y, int color) {
		this.render(text, x + 1, y + 1, color, true);
		this.render(text, x, y, color, false);
	}
	
	public void render(String text, float x, float y, boolean shadow) {
		this.render(text, x, y, 16777215, shadow);
	}
	
	public void render(String text, float x, float y, int color, boolean shadow) {
		if(text != null && text.length() > 0) {
			char[] chars = text.toCharArray();
			if(shadow) {
				color = (color & 16579836) >> 2;
			}

			int width = 0;
			float r = ((color >> 16) & 255) / 255f;
			float g = ((color >> 8) & 255) / 255f;
			float b = (color & 255) / 255f;
			
			for(int count = 0; count < chars.length; count++) {
				if(chars[count] < this.texMap.length) {
					if(chars[count] == '&' && chars.length > count + 1) {
						Color c = Color.getByChar(chars[count + 1]);
						if(c == null) {
							c = Color.WHITE;
						}
						
						if(shadow) {
							int rgb = ((c.getRed() << 16 | c.getGreen() << 8 | c.getBlue()) & 16579836) >> 2;
							r = ((rgb >> 16) & 255) / 255f;
							g = ((rgb >> 8) & 255) / 255f;
							b = (rgb & 255) / 255f;
						} else {
							r = c.getRed() / 255f;
							g = c.getGreen() / 255f;
							b = c.getBlue() / 255f;
						}
						
						count += 2;
					}

					SubTexture tex = this.texMap[chars[count]];
					RenderHelper.getHelper().drawSubTex(tex, x + width, y, 0, 1, r, g, b);
					width += tex.getX2() - tex.getX1();
				}
			}
		}
	}
	
	public float getWidth(String str) {
		if(str == null || str.length() == 0) return 0;
		char chars[] = str.toCharArray();
		int width = 0;
		
		for(int index = 0; index < chars.length; index++) {
			if (chars[index] == '&') {
				index++;
			} else {
				width += this.texMap[chars[index]].getX2() - this.texMap[chars[index]].getX1();
			}
		}
		
		return width;
	}
	
}
