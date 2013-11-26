package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.ProgressBar;
import ch.spacebase.openclassic.api.event.level.LevelUnloadEvent;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonCallback;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;

import com.zachsthings.onevent.EventManager;

public class IngameMenuScreen extends GuiScreen {

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newTranslucentBackground(0, this));
		this.attachWidget(WidgetFactory.getFactory().newButton(1, this.getWidth() / 2 - 100, this.getHeight() / 2 - 48, this, OpenClassic.getGame().getTranslator().translate("gui.menu.options")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(new OptionsScreen(IngameMenuScreen.this, OpenClassic.getClient().getSettings()));
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 - 100, this.getHeight() / 2 - 24, this, OpenClassic.getGame().getTranslator().translate("gui.menu.dump")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(new LevelDumpScreen(IngameMenuScreen.this));
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(3, this.getWidth() / 2 - 100, this.getHeight() / 2, this, OpenClassic.getGame().getTranslator().translate("gui.menu.main-menu")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
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
					if(!OpenClassic.getClient().saveLevel()) {
						OpenClassic.getClient().getProgressBar().setText(String.format(OpenClassic.getGame().getTranslator().translate("level.save-fail")));
						try {
							Thread.sleep(1000L);
						} catch(InterruptedException e) {
						}
					}
					
					progress.setVisible(false);
				}

				OpenClassic.getClient().exitGameSession();
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(4, this.getWidth() / 2 - 100, this.getHeight() / 2 + 60, this, OpenClassic.getGame().getTranslator().translate("gui.menu.back")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(null);
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newLabel(5, this.getWidth() / 2, 40, this, OpenClassic.getGame().getTranslator().translate("gui.menu.title"), true));
		if(!OpenClassic.getClient().isInMultiplayer()) {
			this.getWidget(2, Button.class).setActive(false);
		}
	}
	
}
