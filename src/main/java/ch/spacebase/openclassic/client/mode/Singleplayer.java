package ch.spacebase.openclassic.client.mode;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.player.OtherPlayer;

public class Singleplayer extends Mode {

	public Singleplayer(ClientLevel level) {
		this.setLevel(level);
		this.getLevel().addPlayer(new OtherPlayer((byte) 0, "hi", this.getLevel().getSpawn().clone()));
		this.getPlayer().moveTo(this.getLevel().getSpawn().clone());
	}
	
	@Override
	public void renderPerspective(float delta) {
		super.renderPerspective(delta);
		//PlayerModel.get().render(this.getPlayer().getPosition().clone().add(20, 0, 0), delta);
	}
	
	@Override
	public void unload() {
		OpenClassic.getClient().saveLevel();
		super.unload();
	}

}
