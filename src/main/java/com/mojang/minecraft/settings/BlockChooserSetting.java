package com.mojang.minecraft.settings;

import ch.spacebase.openclassic.api.settings.IntSetting;

public class BlockChooserSetting extends IntSetting {
	
	public BlockChooserSetting(String name, String configKey, String[] stringVals) {
		super(name, configKey, stringVals);
	}
	
	@Override
	public void toggle() {
		super.toggle();
	}
}
