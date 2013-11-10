package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.ProgressBar;
import ch.spacebase.openclassic.api.event.level.LevelUnloadEvent;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.render.RenderHelper;

import com.zachsthings.onevent.EventManager;

public class MenuScreen extends GuiScreen {

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 2 - 48, this, OpenClassic.getGame().getTranslator().translate("gui.menu.options")));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 100, this.getHeight() / 2 - 24, this, OpenClassic.getGame().getTranslator().translate("gui.menu.dump")));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 100, this.getHeight() / 2, this, OpenClassic.getGame().getTranslator().translate("gui.menu.main-menu")));
		this.attachWidget(new Button(3, this.getWidth() / 2 - 100, this.getHeight() / 2 + 60, this, OpenClassic.getGame().getTranslator().translate("gui.menu.back")));

		if(!OpenClassic.getClient().isInMultiplayer()) {
			this.getWidget(1, Button.class).setActive(false);
		}
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 0) {
			OpenClassic.getClient().setCurrentScreen(new OptionsScreen(this, OpenClassic.getClient().getSettings()));
		}

		if(button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(new LevelDumpScreen(this));
		}

		if(button.getId() == 2) {
			if(!OpenClassic.getClient().isInMultiplayer()) {
				if(EventManager.callEvent(new LevelUnloadEvent(OpenClassic.getClient().getLevel())).isCancelled()) {
					return;
				}

				ProgressBar progress = OpenClassic.getClient().getProgressBar();
				progress.setVisible(true);
				progress.setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.singleplayer"));
				progress.setSubtitle(OpenClassic.getGame().getTranslator().translate("level.saving"));
				progress.setText("");
				progress.setProgress(-1);
				progress.render();
				OpenClassic.getClient().saveLevel();
				progress.setVisible(false);
			}

			OpenClassic.getClient().exitGameSession();
		}

		if(button.getId() == 3) {
			OpenClassic.getClient().setCurrentScreen(null);
		}
	}

	public void render() {
		RenderHelper.getHelper().color(0, 0, this.getWidth(), this.getHeight(), 1610941696, -1607454624);
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.menu.title"), this.getWidth() / 2, 40);
		super.render();
	}
	
}
