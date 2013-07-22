package com.mojang.minecraft.render;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;

import com.mojang.minecraft.Minecraft;

public class FogRenderer {

	public Minecraft mc;
	public float fogEnd = 0.0F;
	public float fogRed;
	public float fogBlue;
	public float fogGreen;

	public FogRenderer(Minecraft mc) {
		this.mc = mc;
	}

	public void updateFog() {
		GL11.glFog(GL11.GL_FOG_COLOR, ClientRenderHelper.getHelper().getParamBuffer(this.fogRed, this.fogBlue, this.fogGreen, 1));
		GL11.glNormal3f(0, -1, 0);
		GL11.glColor4f(1, 1, 1, 1);
		BlockType type = Blocks.fromId(this.mc.level.getTile((int) this.mc.player.x, (int) (this.mc.player.y + 0.12F), (int) this.mc.player.z));
		if (type != null && type.isLiquid()) {
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
			if (type == VanillaBlock.WATER || type == VanillaBlock.STATIONARY_WATER) {
				GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
				float r = 0.4F;
				float g = 0.4F;
				float b = 0.9F;
				if (this.mc.settings.anaglyph) {
					r = (r * 30.0F + g * 59.0F + b * 11.0F) / 100.0F;
					g = (r * 30.0F + g * 70.0F) / 100.0F;
					b = (r * 30.0F + b * 70.0F) / 100.0F;
				}

				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, ClientRenderHelper.getHelper().getParamBuffer(r, g, b, 1));
			} else if (type == VanillaBlock.LAVA || type == VanillaBlock.STATIONARY_LAVA) {
				GL11.glFogf(GL11.GL_FOG_DENSITY, 2);
				float r = 0.4F;
				float g = 0.3F;
				float b = 0.3F;
				if (this.mc.settings.anaglyph) {
					r = (r * 30.0F + g * 59.0F + b * 11.0F) / 100.0F;
					g = (r * 30.0F + g * 70.0F) / 100.0F;
					b = (r * 30.0F + b * 70.0F) / 100.0F;
				}

				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, ClientRenderHelper.getHelper().getParamBuffer(r, g, b, 1));
			}
		} else {
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
			GL11.glFogf(GL11.GL_FOG_START, 0);
			GL11.glFogf(GL11.GL_FOG_END, this.fogEnd);
			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, ClientRenderHelper.getHelper().getParamBuffer(1, 1, 1, 1));
		}

		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
	}

}
