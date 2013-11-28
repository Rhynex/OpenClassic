package ch.spacebase.openclassic.client.settings;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.settings.BooleanSetting;
import ch.spacebase.openclassic.client.gui.hud.ClientHUDScreen;

public class MinimapSetting extends BooleanSetting {

	public MinimapSetting(String name) {
		super(name);
	}
	
	@Override
	public void toggle() {
		super.toggle();
		if(OpenClassic.getClient().isInGame()) {
			if(this.getValue()) {
				((ClientHUDScreen) OpenClassic.getClient().getHUD()).addMinimap();
			} else {
				((ClientHUDScreen) OpenClassic.getClient().getHUD()).removeMinimap();
			}
		}
	}

}
