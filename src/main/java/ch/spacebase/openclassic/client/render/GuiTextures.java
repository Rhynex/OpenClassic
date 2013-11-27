package ch.spacebase.openclassic.client.render;

import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.block.model.Texture;

public class GuiTextures {

	public static final Texture LOGO = new Texture("/gui/logo.png", true, 502, 96);
	public static final Texture GUI = new Texture("/gui/gui.png", true, 512, 512, 32);
	public static final Texture ICONS = new Texture("/gui/icons.png", true, 512, 512, 32);
	public static final SubTexture CROSSHAIR = ICONS.getSubTexture(0, 0, 32, 32);
    public static final SubTexture EMPTY_HEART = ICONS.getSubTexture(32, 0, 18, 18);
    public static final SubTexture EMPTY_HEART_FLASH =ICONS.getSubTexture(50, 0, 18, 18);
    public static final SubTexture FULL_HEART = ICONS.getSubTexture(104, 0, 18, 18);
    public static final SubTexture FULL_HEART_FLASH = ICONS.getSubTexture(140, 0, 18, 18);
    public static final SubTexture HALF_HEART = ICONS.getSubTexture(122, 0, 18, 18);
    public static final SubTexture HALF_HEART_FLASH = ICONS.getSubTexture(158, 0, 18, 18);
    public static final SubTexture BUBBLE = ICONS.getSubTexture(32, 36, 18, 18);
    public static final SubTexture POPPING_BUBBLE = ICONS.getSubTexture(50, 36, 18, 18);
	public static final SubTexture QUICK_BAR = GUI.getSubTexture(0, 0, 364, 44);
	public static final SubTexture SELECTION = GUI.getSubTexture(0, 44, 48, 44);
	public static final SubTexture BUTTON = GUI.getSubTexture(0, 132, 400, 40);
	public static final SubTexture BUTTON_HOVER = GUI.getSubTexture(0, 172, 400, 40);
	public static final SubTexture BUTTON_INACTIVE = GUI.getSubTexture(0, 92, 400, 40);
	
}
