package com.mojang.minecraft.settings;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.gamemode.CreativeGameMode;
import com.mojang.minecraft.gamemode.SurvivalGameMode;

import ch.spacebase.openclassic.api.settings.IntSetting;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class SurvivalSetting extends IntSetting {
	
	public SurvivalSetting(String name, String configKey, String[] stringVals) {
		super(name, configKey, stringVals);
	}

	@Override
	public void toggle() {
		super.toggle();
		Minecraft mc = GeneralUtils.getMinecraft();
		mc.mode = this.getValue() > 0 ? new SurvivalGameMode(mc) : new CreativeGameMode(mc);
		if(mc.level != null) {
			mc.mode.apply(mc.level);
		}

		if(mc.player != null) {
			mc.mode.apply(mc.player);
		}
	}

}
