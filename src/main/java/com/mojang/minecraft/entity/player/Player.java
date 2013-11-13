package com.mojang.minecraft.entity.player;

import java.awt.image.BufferedImage;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.player.ClientPlayer;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.mob.HumanoidMob;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.render.TextureManager;

public abstract class Player extends HumanoidMob {

	protected int newTextureId = -1;
	protected BufferedImage newTexture = null;
	protected TextureManager textures;
	public Inventory inventory = new Inventory();
	public int score = 0;
	public int arrows = 20;
	
	public ClientPlayer openclassic = new ClientPlayer(this);

	public Player(Level level, float x, float y, float z) {
		super(level, x, y, z);
		this.armor = false;
		this.helmet = false;
		OpenClassic.getGame().getScheduler().scheduleAsyncTask(this, new SkinDownloadTask(this));
	}
	
	public abstract String getName();

	public void bindTexture(TextureManager textures) {
		this.textures = textures;
		if(this.newTexture != null) {
			int[] imageData = new int[512];
			this.newTexture.getRGB(32, 0, 32, 16, imageData, 0, 32);
			boolean hair = false;
			for(int index = 0; index < imageData.length; index++) {
				if(imageData[index] >>> 24 < 128) {
					hair = true;
					break;
				}
			}
			
			this.hasHair = hair;
			this.newTextureId = textures.bindTexture(this.newTexture);
			this.newTexture = null;
		}

		if(this.newTextureId < 0) {
			RenderHelper.getHelper().bindTexture("/char.png", true);
		} else {
			RenderHelper.getHelper().bindTexture(this.newTextureId);
		}
	}
	
	public int getScore() {
		return this.score;
	}
	
	public void awardKillScore(Entity killed, int score) {
		this.score += score;
	}

}
