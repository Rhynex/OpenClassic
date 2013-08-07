package com.mojang.minecraft.settings;

import java.awt.Color;

import com.mojang.minecraft.Minecraft;

import ch.spacebase.openclassic.api.settings.BooleanSetting;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class NightSetting extends BooleanSetting {

	public NightSetting(String name, String configKey) {
		super(name, configKey);
	}
	
	@Override
	public void toggle() {
		super.toggle();
		Minecraft mc = GeneralUtils.getMinecraft();
		if(mc.level != null) {
			if(this.getValue()) {
				mc.level.skyColor = 0;
				mc.level.fogColor = new Color(30, 30, 30, 70).getRGB();
				mc.level.cloudColor = new Color(30, 30, 30, 70).getRGB();
			} else {
				mc.level.skyColor = 10079487;
				mc.level.fogColor = 16777215;
				mc.level.cloudColor = 16777215;
			}

			if(mc.ingame) {
				mc.levelRenderer.refresh();
			}
		}
	}

}
