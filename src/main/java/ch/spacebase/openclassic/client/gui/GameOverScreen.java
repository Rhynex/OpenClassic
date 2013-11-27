package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiComponent;
import ch.spacebase.openclassic.api.gui.base.Button;
import ch.spacebase.openclassic.api.gui.base.ButtonCallback;
import ch.spacebase.openclassic.api.gui.base.Label;
import ch.spacebase.openclassic.api.gui.base.TranslucentBackground;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.util.Constants;

public class GameOverScreen extends GuiComponent {

	public GameOverScreen() {
		super("gameoverscreen");
	}
	
	@Override
	public void onAttached(GuiComponent parent) {
		this.setSize(parent.getWidth(), parent.getHeight());
		this.attachComponent(new TranslucentBackground("bg"));
		this.attachComponent(new Button("respawn", this.getWidth() / 2 - 200, this.getHeight() / 4 + 144, OpenClassic.getGame().getTranslator().translate("gui.game-over.respawn")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				Player player = OpenClassic.getClient().getPlayer();
				for(int slot = 0; slot < 9; slot++) {
					player.getInventoryContents()[slot] = -1;
					player.getInventoryAmounts()[slot] = 0;
				}

				player.setAir(20);
				player.setArrows(20);
				player.setHealth(Constants.MAX_HEALTH);
				player.respawn();

				OpenClassic.getClient().setActiveComponent(null);
			}
		}));
		
		this.attachComponent(new Button("mainmenu", this.getWidth() / 2 - 200, this.getHeight() / 4 + 192, OpenClassic.getGame().getTranslator().translate("gui.game-over.main-menu")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().exitGameSession();
			}
		}));
		
		this.attachComponent(new Label("title", this.getWidth() / 2, 120, OpenClassic.getGame().getTranslator().translate("gui.game-over.game-over"), true, true));
		
		Player player = OpenClassic.getClient().getPlayer();
		this.attachComponent(new Label("score", this.getWidth() / 2, 200, String.format(OpenClassic.getGame().getTranslator().translate("gui.game-over.score"), player.getScore()), true));
	}
	
}
