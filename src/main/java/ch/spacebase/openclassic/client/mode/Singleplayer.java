package ch.spacebase.openclassic.client.mode;

import ch.spacebase.openclassic.client.level.ClientLevel;

public class Singleplayer extends Mode {

	public Singleplayer(ClientLevel level) {
		this.setLevel(level);
		this.getPlayer().moveTo(this.getLevel().getSpawn().clone());
	}
	
	@Override
	public void renderPerspective(float delta) {
		super.renderPerspective(delta);
		//PlayerModel.get().render(this.getPlayer().getPosition().clone().add(20, 0, 0), delta);
	}
	
	@Override
	public void unload() {
		this.getLevel().save();
		super.unload();
	}

}
