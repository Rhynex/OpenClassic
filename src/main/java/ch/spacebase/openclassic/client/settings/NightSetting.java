package ch.spacebase.openclassic.client.settings;

import java.awt.Color;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.settings.BooleanSetting;
import ch.spacebase.openclassic.client.level.ClientLevel;

public class NightSetting extends BooleanSetting {

	public NightSetting(String name) {
		super(name);
	}
	
	@Override
	public void toggle() {
		super.toggle();
		ClientLevel level = (ClientLevel) OpenClassic.getClient().getLevel();
		if(level != null) {
			if(this.getValue()) {
				level.setSkyColor(0);
				level.setFogColor(new Color(30, 30, 30, 70).getRGB());
				level.setCloudColor(new Color(30, 30, 30, 70).getRGB());
			} else {
				level.setSkyColor(10079487);
				level.setFogColor(16777215);
				level.setCloudColor(16777215);
			}

			level.refreshRenderer();
		}
	}

}
