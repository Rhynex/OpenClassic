package ch.spacebase.openclassic.client.render;

import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.block.model.Texture;

public class GuiTextures {

	public static final Texture LOGO = new Texture("/gui/logo.png", true, 251, 48);
	public static final Texture GUI = new Texture("/gui/gui.png", true, 256, 256, 16);
	public static final Texture ICONS = new Texture("/gui/icons.png", true, 256, 256, 16);
	public static final SubTexture CROSSHAIR = ICONS.getSubTexture(0, 0, 16, 16);
	public static final SubTexture QUICK_BAR = GUI.getSubTexture(0, 0, 182, 22);
	public static final SubTexture SELECTION = GUI.getSubTexture(0, 22, 24, 22);
	public static final SubTexture BUTTON = GUI.getSubTexture(0, 66, 200, 20);
	public static final SubTexture BUTTON_HOVER = GUI.getSubTexture(0, 86, 200, 20);
	public static final SubTexture BUTTON_INACTIVE = GUI.getSubTexture(0, 46, 200, 20);
	
}
