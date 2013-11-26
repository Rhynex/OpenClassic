package ch.spacebase.openclassic.client.settings;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.settings.BooleanSetting;

public class MusicSetting extends BooleanSetting {

	public MusicSetting(String name) {
		super(name);
	}
	
	@Override
	public void toggle() {
		super.toggle();
		if(!this.getValue()) {
			OpenClassic.getGame().getAudioManager().stopMusic();
		}
	}

}
