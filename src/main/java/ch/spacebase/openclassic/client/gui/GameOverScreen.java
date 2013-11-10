package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.Constants;

public class GameOverScreen extends GuiScreen {

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 4 + 72, this, OpenClassic.getGame().getTranslator().translate("gui.game-over.respawn")));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 96, this, OpenClassic.getGame().getTranslator().translate("gui.game-over.main-menu")));
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 0) {
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

		if(button.getId() == 1) {
			OpenClassic.getClient().exitGameSession();
		}
	}

	public void render() {
		Player player = OpenClassic.getClient().getPlayer();
		RenderHelper.getHelper().color(0, 0, this.getWidth(), this.getHeight(), 1615855616, -1602211792);

		RenderHelper.getHelper().pushMatrix();
		RenderHelper.getHelper().scale(2, 2, 2);
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.game-over.game-over"), this.getWidth() / 2 / 2, 30);
		RenderHelper.getHelper().popMatrix();
		RenderHelper.getHelper().renderText(String.format(OpenClassic.getGame().getTranslator().translate("gui.game-over.score"), player.getScore()), this.getWidth() / 2, 100);
		super.render();
	}
	
}
