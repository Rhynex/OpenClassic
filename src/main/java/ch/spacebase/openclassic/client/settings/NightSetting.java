package ch.spacebase.openclassic.client.settings;

import java.awt.Color;

import com.mojang.minecraft.Minecraft;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.settings.BooleanSetting;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class NightSetting extends BooleanSetting {

	public NightSetting(String name) {
		super(name);
	}
	
	@Override
	public void toggle() {
		super.toggle();
		Minecraft mc = GeneralUtils.getMinecraft();
		if(mc.level != null) {
			if(this.getValue()) {
				OpenClassic.getClient().getLevel().setSkyColor(0);
				OpenClassic.getClient().getLevel().setFogColor(new Color(30, 30, 30, 70).getRGB());
				OpenClassic.getClient().getLevel().setCloudColor(new Color(30, 30, 30, 70).getRGB());
			} else {
				OpenClassic.getClient().getLevel().setSkyColor(10079487);
				OpenClassic.getClient().getLevel().setFogColor(16777215);
				OpenClassic.getClient().getLevel().setCloudColor(16777215);
			}

			if(mc.ingame) {
				mc.levelRenderer.refresh();
			}
		}
	}

}
