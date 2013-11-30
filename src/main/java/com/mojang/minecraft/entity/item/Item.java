package com.mojang.minecraft.entity.item;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.client.level.ClientLevel;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.player.LocalPlayer;
import com.mojang.minecraft.render.TextureManager;

public class Item extends Entity {

	private static ItemModel[] models = new ItemModel[256];
	
	public float xd;
	public float yd;
	public float zd;
	public float rot;
	private int resource;
	private int tickCount;
	private int age = 0;
	private int count = 0;
	public int delay = 10;

	public static void initModels() {
		for(int id = 1; id < 256; id++) {
			if(Blocks.fromId(id) != null) {
				models[id] = new ItemModel(id);
			}
		}
	}

	public Item(ClientLevel level, float x, float y, float z, int block) {
		this(level, x, y, z, block, 1);
	}

	public Item(ClientLevel level, float x, float y, float z, int block, int count) {
		super(level);
		this.setSize(0.25F, 0.25F);
		this.heightOffset = this.bbHeight / 2;
		this.setPos(x, y, z);
		this.resource = block;
		this.count = count;
		this.rot = (float) (Math.random() * 360);
		this.xd = (float) (Math.random() * 0.2D - 0.1D);
		this.yd = 0.2F;
		this.zd = (float) (Math.random() * 0.2D - 0.1D);
		this.makeStepSound = false;
	}

	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.yd -= 0.04F;
		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.98F;
		this.yd *= 0.98F;
		this.zd *= 0.98F;
		if(this.onGround) {
			this.xd *= 0.7F;
			this.zd *= 0.7F;
			this.yd *= -0.5F;
		}

		this.tickCount++;
		this.age++;
		this.delay--;
		if(this.age >= 6000) {
			this.remove();
		}
		
		if(this.getLiquid() != null && this.getLiquid().getLiquidName().equals("lava")) {
			OpenClassic.getGame().getAudioManager().playSound("random.fizz", this.x, this.y, this.z, 0.4f, 2 + this.level.getRandom().nextFloat() * 0.4f);
			this.remove();
		}
	}

	public void render(TextureManager textures, float dt) {
		float rot = this.rot + (this.tickCount + dt) * 3;
		GL11.glPushMatrix();
		float rsin = MathHelper.sin(rot / 10);
		float bob = rsin * 0.1F + 0.1F;
		GL11.glTranslatef(this.xo + (this.x - this.xo) * dt, this.yo + (this.y - this.yo) * dt + bob, this.zo + (this.z - this.zo) * dt);
		GL11.glRotatef(rot, 0, 1, 0);

		if(models[this.resource] == null && Blocks.fromId(this.resource) != null) {
			models[this.resource] = new ItemModel(this.resource);
		}

		models[this.resource].render();
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glPopMatrix();
	}

	public void playerTouch(LocalPlayer player) {
		if(this.delay <= 0 && player.inventory.addResource(this.resource, this.count)) {
			OpenClassic.getGame().getAudioManager().playSound("random.pop", player.x, player.y, player.z, 0.2f, ((this.level.getRandom().nextFloat() - this.level.getRandom().nextFloat()) * 0.7f + 1) * 2);
			this.level.addEntity(new TakeEntityAnim(this.level, this, player));
			this.remove();
		}
	}

}
