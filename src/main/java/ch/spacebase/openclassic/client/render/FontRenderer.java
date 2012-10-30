package ch.spacebase.openclassic.client.render;

import java.awt.Font;
import java.lang.reflect.Field;

import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;

import ch.spacebase.openclassic.api.render.RenderHelper;

public class FontRenderer {

	private static final String FONT = "/gui/font.ttf";
	private static final String FALLBACK = "Serif";
	private static final float FONT_SIZE = 15f;
	
	private TrueTypeFont font;
	private Font awt;
	
	public FontRenderer() {
		try {
			this.awt = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream(FONT)).deriveFont(FONT_SIZE);
		} catch(Exception e) {
			e.printStackTrace();
			this.awt = Font.getFont(FALLBACK).deriveFont(FONT_SIZE);
		}
		
		this.font = new TrueTypeFont(this.awt, false);
	}
	
	public void render(String text, float x, float y) {
		this.render(text, x, y, true);
	}
	
	public void render(String text, float x, float y, boolean shadow) {
		RenderHelper.getHelper().bindTexture(this.getFontTexture().getTextureID());
		StringBuilder line = new StringBuilder();
		Color col = Color.white;
		for(int count = 0; count < text.length(); count++) {
			char c = text.charAt(count);
			if(c == '&' && count != text.length() - 1 && "123456789abcdef".contains(String.valueOf(text.charAt(count + 1)))) {
				if(line.length() > 0) {
					if(shadow) this.font.drawString(x + 1, y + 1, line.toString(), Color.black);
					this.font.drawString(x, y, line.toString(), col);
				}
				
				col = toSlick(ch.spacebase.openclassic.api.Color.getByChar(text.charAt(count + 1)));
				count++;
				x += this.font.getWidth(line.toString());
				line = new StringBuilder();
			} else {
				line.append(c);
			}
		}
		
		if(line.length() > 0) {
			if(shadow) this.font.drawString(x + 1, y + 1, line.toString(), Color.black);
			this.font.drawString(x, y, line.toString(), col);
		}
	}
	
	public int getWidth(String str) {
		return this.font.getWidth(ch.spacebase.openclassic.api.Color.stripColor(str));
	}
	
	private Texture getFontTexture() {
		try {
			Field f = TrueTypeFont.class.getDeclaredField("fontTexture");
			f.setAccessible(true);
			return (Texture) f.get(this.font);
		} catch(Exception e) {
			return null;
		}
	}
	
	private static Color toSlick(ch.spacebase.openclassic.api.Color col) {
		return new Color(col.getRed(), col.getGreen(), col.getBlue());
	}
	
}
