package com.mojang.minecraft.entity.item;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.render.TextureManager;

public class TakeEntityAnim extends Entity {

	private int time = 0;
	private Entity taking;
	private Entity player;
	private float xorg;
	private float yorg;
	private float zorg;

	public TakeEntityAnim(Level level, Entity item, Entity player) {
		super(level);
		this.taking = item;
		this.player = player;
		this.setSize(1, 1);
		this.xorg = item.x;
		this.yorg = item.y;
		this.zorg = item.z;
	}

	public void tick() {
		this.time++;
		if(this.time >= 3) {
			this.remove();
		}

		float distance = (this.time / 3) * (this.time / 3);
		this.xo = this.taking.xo = this.taking.x;
		this.yo = this.taking.yo = this.taking.y;
		this.zo = this.taking.zo = this.taking.z;
		this.x = this.taking.x = this.xorg + (this.player.x - this.xorg) * distance;
		this.y = this.taking.y = this.yorg + (this.player.y - 1 - this.yorg) * distance;
		this.z = this.taking.z = this.zorg + (this.player.z - this.zorg) * distance;
		this.setPos(this.x, this.y, this.z);
	}

	public void render(TextureManager textures, float dt) {
		this.taking.render(textures, dt);
	}
}
