package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.event.level.LevelUnloadEvent;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.mode.Multiplayer;
import ch.spacebase.openclassic.client.player.ClientPlayer;

public class GameOverScreen extends GuiScreen {

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 200, this.getHeight() / 4 + 144, this, OpenClassic.getGame().getTranslator().translate("gui.gameover.respawn")));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 200, this.getHeight() / 4 + 192, this, OpenClassic.getGame().getTranslator().translate("gui.gameover.main-menu")));
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 0) {
			((ClientPlayer) OpenClassic.getClient().getPlayer()).respawn();
			OpenClassic.getClient().setCurrentScreen(null);
		}

		if(button.getId() == 1) {
			if(!(((ClassicClient) OpenClassic.getClient()).getMode() instanceof Multiplayer)) {
				if(OpenClassic.getGame().getEventManager().dispatch(new LevelUnloadEvent(OpenClassic.getClient().getLevel())).isCancelled()) {
					return;
				}
			}
			
			((ClassicClient) OpenClassic.getClient()).exitLevel();
		}
	}

	public void render() {
		RenderHelper.getHelper().color(0, 0, this.getWidth(), this.getHeight(), 0x60500000, 0xa0803030);
		RenderHelper.getHelper().renderScaledText(OpenClassic.getGame().getTranslator().translate("gui.gameover.title"), this.getWidth() / 2, 120);
		super.render();
	}
}
