package com.mojang.minecraft.entity.player;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.render.GuiTextures;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.mob.HumanoidMob;

public abstract class Player extends HumanoidMob {

	protected Texture skin = null;
	public Inventory inventory = new Inventory();
	public int score = 0;
	public int arrows = 20;
	
	public ClientPlayer openclassic;

	public Player(ClientLevel level, float x, float y, float z, ClientPlayer openclassic) {
		super(level, x, y, z);
		this.armor = false;
		this.helmet = false;
		this.openclassic = openclassic;
		OpenClassic.getGame().getScheduler().scheduleAsyncTask(this, new SkinDownloadTask(this));
	}
	
	public String getName() {
		return this.openclassic.getName() != null ? this.openclassic.getName() : "Player";
	}

	public void bindTexture() {
		if(this.skin != null) {
			int[] imageData = this.skin.getRGBA();
			boolean hair = false;
			for(int index = 0; index < imageData.length; index++) {
				if(imageData[index] >>> 24 < 128) {
					hair = true;
					break;
				}
			}
			
			this.hasHair = hair;
		}

		if(this.skin == null) {
			GuiTextures.DEFAULT_SKIN.bind();
		} else {
			this.skin.bind();
		}
	}
	
	public int getScore() {
		return this.score;
	}
	
	public void awardKillScore(Entity killed, int score) {
		this.score += score;
	}

}
