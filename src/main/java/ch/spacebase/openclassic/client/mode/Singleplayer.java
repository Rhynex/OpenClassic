package ch.spacebase.openclassic.client.mode;

import ch.spacebase.openclassic.client.level.ClientLevel;

public class Singleplayer extends Mode {
	
	public Singleplayer(ClientLevel level) {
		this.setLevel(level);
		this.getPlayer().moveTo(this.getLevel().getSpawn().clone());
	}
	
	@Override
	public void unload() {
		this.getPlayer().save();
		super.unload();
	}

}
