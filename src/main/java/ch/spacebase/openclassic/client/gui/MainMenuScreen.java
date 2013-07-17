package ch.spacebase.openclassic.client.gui;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.GuiTextures;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import com.mojang.minecraft.gui.LoadLevelScreen;
import com.mojang.minecraft.gui.OptionsScreen;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class MainMenuScreen extends GuiScreen {
	
	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 4 + 16, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.singleplayer")));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 40, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.multiplayer")));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 100, this.getHeight() / 4 + 64, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.options")));
		this.attachWidget(new Button(3, this.getWidth() / 2 - 100, this.getHeight() / 4 + 88, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.texture-packs")));
		this.attachWidget(new Button(4, this.getWidth() / 2 - 100, this.getHeight() / 4 + 112, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.language")));
		this.attachWidget(new Button(5, this.getWidth() / 2 - 102, this.getHeight() / 4 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.about")));
		this.attachWidget(new Button(6, this.getWidth() / 2 + 2, this.getHeight() / 4 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.quit")));
	
		if(!OpenClassic.getClient().getAudioManager().isPlaying("menu")) {
			OpenClassic.getClient().getAudioManager().playMusic("menu", true);
		}
		
		if(GeneralUtils.getMinecraft().data == null || GeneralUtils.getMinecraft().settings.survival) {
			this.getWidget(1, Button.class).setActive(false);
		}
	}

	public void onButtonClick(Button button) {
		if (button.getId() == 0) {
			OpenClassic.getClient().setCurrentScreen(new LoadLevelScreen(this));
		}

		if (button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(new ServerListScreen(this));
		}

		if (button.getId() == 2) {
			OpenClassic.getClient().setCurrentScreen(new OptionsScreen(this, GeneralUtils.getMinecraft().settings));
		}
		
		if (button.getId() == 3) {
			OpenClassic.getClient().setCurrentScreen(new TexturePackScreen(this));
		}
		
		if (button.getId() == 4) {
			OpenClassic.getClient().setCurrentScreen(new LanguageScreen(this));
		}

		if (button.getId() == 5) {
			OpenClassic.getClient().setCurrentScreen(new AboutScreen(this));
		}

		if (button.getId() == 6) {
			OpenClassic.getClient().shutdown();
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		GL11.glEnable(GL11.GL_BLEND);
		RenderHelper.getHelper().drawTexture(GuiTextures.LOGO, this.getWidth() / 2 - GuiTextures.LOGO.getWidth() / 2, 20, 1);
		GL11.glDisable(GL11.GL_BLEND);
		super.render();
	}
}
