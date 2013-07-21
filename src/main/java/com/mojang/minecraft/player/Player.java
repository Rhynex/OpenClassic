package com.mojang.minecraft.player;

import java.awt.image.BufferedImage;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.render.RenderHelper;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.mob.HumanoidMob;
import com.mojang.minecraft.render.TextureManager;

public abstract class Player extends HumanoidMob {

	private static final long serialVersionUID = -2700951438056228150L;
	
	protected int newTextureId = -1;
	protected BufferedImage newTexture = null;
	protected TextureManager textures;
	
	public Player(Level level, float x, float y, float z) {
		super(level, x, y, z);
		this.armor = false;
		this.helmet = false;
		OpenClassic.getGame().getScheduler().scheduleTask(this, new SkinDownloadTask(this));
	}
	
	public void bindTexture(TextureManager textures) {
		this.textures = textures;
		if (this.newTexture != null) {
			int[] imageData = new int[512];
			this.newTexture.getRGB(32, 0, 32, 16, imageData, 0, 32);
			int index = 0;

			boolean hair;
			while (true) {
				if (index >= imageData.length) {
					hair = false;
					break;
				}

				if (imageData[index] >>> 24 < 128) {
					hair = true;
					break;
				}

				index++;
			}

			this.hasHair = hair;
			this.newTextureId = textures.bindTexture(this.newTexture);
			this.newTexture = null;
		}

		if (this.newTextureId < 0) {
			RenderHelper.getHelper().bindTexture("/char.png", true);
		} else {
			RenderHelper.getHelper().bindTexture(this.newTextureId);
		}
	}
	
	public abstract String getName();

}
