package com.mojang.minecraft.render;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;

import com.mojang.minecraft.Minecraft;

public class FogRenderer {

	public Minecraft mc;
	public float fogEnd;
	public float fogRed;
	public float fogGreen;
	public float fogBlue;

	public FogRenderer(Minecraft mc) {
		this.mc = mc;
	}

	public void updateFog() {
		float fogRed = this.fogRed;
		float fogGreen = this.fogGreen;
		float fogBlue = this.fogBlue;
		BlockType type = Blocks.fromId(this.mc.level.getTile((int) this.mc.player.x, (int) (this.mc.player.y + 0.12F), (int) this.mc.player.z));
		if(type != null && (type.getFogDensity() != -1 || type.getFogRed() != -1 || type.getFogGreen() != -1 || type.getFogBlue() != -1 || type.isLiquid())) {
			if(type.getFogDensity() != -1) {
				GL11.glFogf(GL11.GL_FOG_DENSITY, type.getFogDensity());
			}
			
			if(type.getFogRed() != -1) {
				fogRed = type.getFogRed() / 255f;
			}
			
			if(type.getFogGreen() != -1) {
				fogGreen = type.getFogGreen() / 255f;
			}
			
			if(type.getFogBlue() != -1) {
				fogBlue = type.getFogBlue() / 255f;
			}
			
			if(type.isLiquid()) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				float r = type.getFogRed() != -1 ? type.getFogRed() / 255f : 1;
				float g = type.getFogGreen() != -1 ? type.getFogGreen() / 255f : 1;
				float b = type.getFogBlue() != -1 ? type.getFogBlue() / 255f : 1;
				if(this.mc.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					r = (r * 30 + g * 59 + b * 11) / 100;
					g = (r * 30 + g * 70) / 100;
					b = (r * 30 + b * 70) / 100;
				}

				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, ClientRenderHelper.getHelper().getParamBuffer(r, g, b, 1));
			}
		} else {
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
			GL11.glFogf(GL11.GL_FOG_START, 0);
			GL11.glFogf(GL11.GL_FOG_END, this.fogEnd);
			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, ClientRenderHelper.getHelper().getParamBuffer(1, 1, 1, 1));
		}
		
		if(this.mc.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
			float fred = (fogRed * 30 + fogBlue * 59 + fogGreen * 11) / 100;
			float fgreen = (fogRed * 30 + fogGreen * 70) / 100;
			float fblue = (fogRed * 30 + fogBlue * 70) / 100;
			fogRed = fred;
			fogGreen = fgreen;
			fogBlue = fblue;
		}
		
		GL11.glClearColor(fogRed, fogGreen, fogBlue, 0);
		GL11.glFog(GL11.GL_FOG_COLOR, ClientRenderHelper.getHelper().getParamBuffer(fogRed, fogGreen, fogBlue, 1));
		GL11.glNormal3f(0, -1, 0);
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
	}

}
