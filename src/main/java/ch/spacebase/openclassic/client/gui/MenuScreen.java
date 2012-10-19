package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.event.level.LevelUnloadEvent;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.mode.Multiplayer;

public final class MenuScreen extends GuiScreen {

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 2 - 48, this, OpenClassic.getGame().getTranslator().translate("gui.menu.options")));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 100, this.getHeight() / 2 - 24, this, OpenClassic.getGame().getTranslator().translate("gui.menu.main-menu")));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 100, this.getHeight() / 2 + 60, this, OpenClassic.getGame().getTranslator().translate("gui.menu.back")));
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 0) {
			OpenClassic.getClient().setCurrentScreen(new OptionsScreen(this));
		}

		if(button.getId() == 1) {
			if(!(((ClassicClient) OpenClassic.getClient()).getMode() instanceof Multiplayer)) {
				if(OpenClassic.getGame().getEventManager().dispatch(new LevelUnloadEvent(OpenClassic.getClient().getLevel())).isCancelled()) {
					return;
				}
			}
			
			((ClassicClient) OpenClassic.getClient()).exitLevel();
		}
		
		if(button.getId() == 2) {
			OpenClassic.getClient().setCurrentScreen(null);
		}
	}

	public void render() {
		RenderHelper.getHelper().color(0, 0, this.getWidth(), this.getHeight(), 1610941696, -1607454624);
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.menu.title"), this.getWidth() / 2, 40);
		super.render();
	}
}
