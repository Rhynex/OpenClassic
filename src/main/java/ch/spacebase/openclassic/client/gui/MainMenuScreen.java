package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.client.render.GuiTextures;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class MainMenuScreen extends GuiScreen {

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		this.attachWidget(WidgetFactory.getFactory().newButton(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 16, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.singleplayer")));
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 - 100, this.getHeight() / 4 + 40, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.multiplayer")));
		this.attachWidget(WidgetFactory.getFactory().newButton(3, this.getWidth() / 2 - 100, this.getHeight() / 4 + 64, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.options")));
		this.attachWidget(WidgetFactory.getFactory().newButton(4, this.getWidth() / 2 - 100, this.getHeight() / 4 + 88, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.texture-packs")));
		this.attachWidget(WidgetFactory.getFactory().newButton(5, this.getWidth() / 2 - 100, this.getHeight() / 4 + 112, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.language")));
		this.attachWidget(WidgetFactory.getFactory().newButton(6, this.getWidth() / 2 - 102, this.getHeight() / 4 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.about")));
		this.attachWidget(WidgetFactory.getFactory().newButton(7, this.getWidth() / 2 + 2, this.getHeight() / 4 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.quit")));
		this.attachWidget(WidgetFactory.getFactory().newImage(8, this.getWidth() / 2 - GuiTextures.LOGO.getWidth() / 2, 20, this, GuiTextures.LOGO.getSubTexture(0, 0, GuiTextures.LOGO.getWidth(), GuiTextures.LOGO.getHeight())));
		
		if(!OpenClassic.getClient().getAudioManager().isPlaying("menu")) {
			OpenClassic.getClient().getAudioManager().playMusic("menu", true);
		}

		if(GeneralUtils.getMinecraft().username == null || OpenClassic.getClient().getSettings().getIntSetting("options.survival").getValue() > 0) {
			this.getWidget(2, Button.class).setActive(false);
		}
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(new LoadLevelScreen(this));
		}

		if(button.getId() == 2) {
			OpenClassic.getClient().setCurrentScreen(new ServerListScreen(this));
		}

		if(button.getId() == 3) {
			OpenClassic.getClient().setCurrentScreen(new OptionsScreen(this, OpenClassic.getClient().getSettings()));
		}

		if(button.getId() == 4) {
			OpenClassic.getClient().setCurrentScreen(new TexturePackScreen(this));
		}

		if(button.getId() == 5) {
			OpenClassic.getClient().setCurrentScreen(new LanguageScreen(this));
		}

		if(button.getId() == 6) {
			OpenClassic.getClient().setCurrentScreen(new AboutScreen(this));
		}

		if(button.getId() == 7) {
			OpenClassic.getClient().shutdown();
		}
	}
	
}
