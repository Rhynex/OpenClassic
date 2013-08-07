package com.mojang.minecraft.settings;

import ch.spacebase.openclassic.api.settings.BooleanSetting;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class TextureRefreshSetting extends BooleanSetting {

	public TextureRefreshSetting(String name, String configKey) {
		super(name, configKey);
	}
	
	@Override
	public void toggle() {
		super.toggle();
		GeneralUtils.getMinecraft().textureManager.clear();
		if(GeneralUtils.getMinecraft().ingame) {
			GeneralUtils.getMinecraft().levelRenderer.refresh();
		}
	}

}
