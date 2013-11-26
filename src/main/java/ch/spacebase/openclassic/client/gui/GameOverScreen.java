package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.util.Constants;

public class GameOverScreen extends GuiScreen {

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newTranslucentBackground(0, this));
		this.attachWidget(WidgetFactory.getFactory().newButton(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 72, this, OpenClassic.getGame().getTranslator().translate("gui.game-over.respawn")));
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 - 100, this.getHeight() / 4 + 96, this, OpenClassic.getGame().getTranslator().translate("gui.game-over.main-menu")));
		this.attachWidget(WidgetFactory.getFactory().newLabel(3, this.getWidth() / 2, 60, this, OpenClassic.getGame().getTranslator().translate("gui.game-over.game-over"), true, true));
		
		Player player = OpenClassic.getClient().getPlayer();
		this.attachWidget(WidgetFactory.getFactory().newLabel(4, this.getWidth() / 2, 100, this, String.format(OpenClassic.getGame().getTranslator().translate("gui.game-over.score"), player.getScore()), true));
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 1) {
			Player player = OpenClassic.getClient().getPlayer();
			for(int slot = 0; slot < 9; slot++) {
				player.getInventoryContents()[slot] = -1;
				player.getInventoryAmounts()[slot] = 0;
			}

			player.setAir(20);
			player.setArrows(20);
			player.setHealth(Constants.MAX_HEALTH);
			player.respawn();

			OpenClassic.getClient().setCurrentScreen(null);
		}

		if(button.getId() == 2) {
			OpenClassic.getClient().exitGameSession();
		}
	}
	
}
