package ch.spacebase.openclassic.client.settings;

import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.settings.BooleanSetting;

public class SmoothingSetting extends BooleanSetting {

	public SmoothingSetting(String name) {
		super(name);
	}
	
	@Override
	public boolean isVisible() {
		return RenderHelper.getHelper().getMipmapMode() != 0;
	}

}
