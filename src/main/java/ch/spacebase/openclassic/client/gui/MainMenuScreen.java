package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.render.RenderHelper;

public class MainMenuScreen extends GuiScreen {

	private static final Texture logo = new Texture("/gui/logo.png", true, 502, 96);
	
	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 200, this.getHeight() / 4 + 32, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.singleplayer")));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 200, this.getHeight() / 4 + 80, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.multiplayer")));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 200, this.getHeight() / 4 + 128, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.options")));
		this.attachWidget(new Button(3, this.getWidth() / 2 - 200, this.getHeight() / 4 + 176, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.resource-packs")));
		this.attachWidget(new Button(4, this.getWidth() / 2 - 200, this.getHeight() / 4 + 224, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.language")));
		this.attachWidget(new Button(5, this.getWidth() / 2 - 204, this.getHeight() / 4 + 288, 200, 40, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.about")));
		this.attachWidget(new Button(6, this.getWidth() / 2 + 4, this.getHeight() / 4 + 288, 200, 40, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.quit")));
	
		if(!OpenClassic.getClient().getAudioManager().isPlaying("menu")) OpenClassic.getClient().getAudioManager().playMusic("menu", true);
	}

	public void onButtonClick(Button button) {
		if (button.getId() == 0) {
			OpenClassic.getClient().setCurrentScreen(new LoadLevelScreen(this));
		}

		if (button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(new ServerListScreen(this));
		}

		if (button.getId() == 2) {
			OpenClassic.getClient().setCurrentScreen(new OptionsScreen(this));
		}
		
		if (button.getId() == 3) {
			OpenClassic.getClient().setCurrentScreen(new ResourcePackScreen(this));
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
		RenderHelper.getHelper().drawTexture(logo, this.getWidth() / 2 - logo.getWidth() / 2, 20, 1);
		super.render();
	}
}
