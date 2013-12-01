package ch.spacebase.openclassic.client.settings;

import ch.spacebase.openclassic.client.render.MipmapMode;
import ch.spacebase.openclassic.client.render.RenderHelper;

public class SmoothingSetting extends TextureRefreshSetting {

	public SmoothingSetting(String name) {
		super(name);
	}
	
	@Override
	public boolean isVisible() {
		return RenderHelper.getHelper().getMipmapMode() != MipmapMode.NONE;
	}

}
