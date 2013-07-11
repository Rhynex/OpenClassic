package ch.spacebase.openclassic.client.render;

import java.awt.Font;
import java.lang.reflect.Field;

import static org.lwjgl.opengl.GL11.glColor4f;

import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;

import ch.spacebase.openclassic.api.render.RenderHelper;

public class FontRenderer {

	private static final String FONT = "/gui/font.ttf";
	private static final String FALLBACK = "Serif";
	private static final float FONT_SIZE = 15f;
	
	private TrueTypeFont font;
	private TrueTypeFont scaled;
	private Font awt;
	private Font awtScaled;
	
	// TODO: asset system
	public FontRenderer() {
		try {
			this.awt = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream(FONT)).deriveFont(FONT_SIZE);
			this.awtScaled = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream(FONT)).deriveFont(FONT_SIZE * 2);
		} catch(Exception e) {
			e.printStackTrace();
			this.awt = Font.getFont(FALLBACK).deriveFont(FONT_SIZE);
			this.awtScaled = Font.getFont(FALLBACK).deriveFont(FONT_SIZE * 2);
		}
		
		this.font = new TrueTypeFont(this.awt, false);
		this.scaled = new TrueTypeFont(this.awtScaled, false);
	}
	
	public void render(String text, float x, float y) {
		this.render(text, x, y, true);
	}
	
	public void render(String text, float x, float y, boolean shadow) {
		this.render(text, x, y, shadow, false);
	}
	
	public void render(String text, float x, float y, boolean shadow, boolean scaled) {
		RenderHelper.getHelper().bindTexture(this.getFontTexture(scaled).getTextureID());
		StringBuilder line = new StringBuilder();
		Color col = Color.white;
		for(int count = 0; count < text.length(); count++) {
			char c = text.charAt(count);
			if(c == '&' && count != text.length() - 1 && "0123456789abcdef".contains(String.valueOf(text.charAt(count + 1)))) {
				if(line.length() > 0) {
					if(shadow) (scaled ? this.scaled : this.font).drawString(x + 1, y + 1, line.toString(), col.darker(2));
					(scaled ? this.scaled : this.font).drawString(x, y, line.toString(), col);
				}
				
				col = toSlick(ch.spacebase.openclassic.api.Color.getByChar(text.charAt(count + 1)));
				count++;
				x += (scaled ? this.scaled : this.font).getWidth(line.toString());
				line = new StringBuilder();
			} else {
				line.append(c);
			}
		}
		
		if(line.length() > 0) {
			if(shadow) (scaled ? this.scaled : this.font).drawString(x + 1, y + 1, line.toString(), col.darker(2));
			(scaled ? this.scaled : this.font).drawString(x, y, line.toString(), col);
		}
		
		glColor4f(1, 1, 1, 1);
	}
	
	public int getWidth(String str) {
		return this.getWidth(str, false);
	}
	
	public int getWidth(String str, boolean scaled) {
		return (scaled ? this.scaled : this.font).getWidth(ch.spacebase.openclassic.api.Color.stripColor(str));
	}
	
	private Texture getFontTexture(boolean scaled) {
		try {
			Field f = TrueTypeFont.class.getDeclaredField("fontTexture");
			f.setAccessible(true);
			return (Texture) f.get(scaled ? this.scaled : this.font);
		} catch(Exception e) {
			return null;
		}
	}
	
	private static Color toSlick(ch.spacebase.openclassic.api.Color col) {
		return new Color(col.getRed(), col.getGreen(), col.getBlue());
	}
	
}
