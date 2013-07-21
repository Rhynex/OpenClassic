package com.mojang.minecraft.entity.player;

import java.io.Serializable;
import java.util.List;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.event.player.PlayerMoveEvent;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.entity.item.Item;
import com.mojang.minecraft.entity.mob.ai.BasicAI;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.model.HumanoidModel;
import com.mojang.minecraft.render.TextureManager;
import com.zachsthings.onevent.EventManager;

public class LocalPlayer extends Player {

	public static final long serialVersionUID = 0L;
	public static final int MAX_HEALTH = 20;
	public static final int MAX_ARROWS = 99;
	
	public transient InputHandler input;
	public Inventory inventory = new Inventory();
	public byte userType = 0;
	public float oBob;
	public float bob;
	public int score = 0;
	public int arrows = 20;
	public transient boolean speedHack = false;
	
	public transient ClientPlayer openclassic = new ClientPlayer(this);
	
	public LocalPlayer(Level level) {
		super(level, 0, 0, 0);
		if (level != null) {
			level.player = this;
			level.removeEntity(this);
			level.addEntity(this);
		}

		this.heightOffset = 1.62F;
		this.health = 20;
		this.modelName = "humanoid";
		this.rotOffs = 180.0F;
		this.ai = new PlayerAI(this);
	}

	public void resetPos() {
		this.resetPos(null);
	}
	
	public void resetPos(Position pos) {
		this.heightOffset = 1.62F;
		this.setSize(0.6F, 1.8F);
		super.resetPos(pos);
		if (this.level != null) {
			this.level.player = this;
		}

		this.deathTime = 0;
	}

	public void aiStep() {
		this.inventory.tick();
		this.oBob = this.bob;
		this.input.updateMovement();
		super.aiStep();
		float bob = (float) Math.sqrt(this.xd * this.xd + this.zd * this.zd);
		float tilt = (float) Math.atan((-this.yd * 0.2F)) * 15.0F;
		if (bob > 0.1F) {
			bob = 0.1F;
		}

		if (!this.onGround || this.health <= 0) {
			bob = 0.0F;
		}

		if (this.onGround || this.health <= 0) {
			tilt = 0.0F;
		}

		this.bob += (bob - this.bob) * 0.4F;
		this.tilt += (tilt - this.tilt) * 0.8F;

		List<Entity> entities = this.level.findEntities(this, this.bb.grow(1, 0, 1));
		if (this.health > 0 && entities != null) {
			for (Entity entity : entities) {
				entity.playerTouch(this);
			}
		}
	}

	public void render(TextureManager textureManager, float dt) {
	}

	public void releaseAllKeys() {
		this.input.resetKeys();
	}

	public void setKey(int key, boolean pressed) {
		this.input.setKeyState(key, pressed);
	}
	
	public void keyPress(int key) {
		this.input.keyPress(key);
	}

	public boolean addResource(int block) {
		return this.inventory.addResource(block);
	}
	
	public boolean addResource(int block, int count) {
		boolean result = false;
		for(int cnt = 0; cnt < count; cnt++) {
			if(this.addResource(block)) result = true;
		}
		
		return result;
	}

	public int getScore() {
		return this.score;
	}

	public HumanoidModel getModel() {
		return (HumanoidModel) modelCache.getModel(this.modelName);
	}

	public void die(Entity cause) {
		this.setSize(0.2F, 0.2F);
		this.setPos(this.x, this.y, this.z);
		this.yd = 0.1F;
		
		if (cause != null) {
			this.xd = -MathHelper.cos((this.hurtDir + this.yaw) * MathHelper.DEG_TO_RAD) * 0.1F;
			this.zd = -MathHelper.sin((this.hurtDir + this.yaw) * MathHelper.DEG_TO_RAD) * 0.1F;
		} else {
			this.xd = this.zd = 0.0F;
		}
		
		for(int slot = 0; slot < this.inventory.slots.length; slot++) {
			if(this.inventory.slots[slot] != -1) {
				this.level.addEntity(new Item(this.level, this.x, this.y, this.z, this.inventory.slots[slot], this.inventory.count[slot]));
			}
		}

		this.heightOffset = 0.1F;
	}

	public void remove() {
	}

	public void awardKillScore(Entity killed, int score) {
		this.score += score;
	}

	public boolean isShootable() {
		return true;
	}

	public void hurt(Entity entity, int damage) {
		if (!this.level.creativeMode) {
			super.hurt(entity, damage);
		}
	}

	public boolean isCreativeModeAllowed() {
		return true;
	}
	
	public void moveRelative(float forward, float strafe, float speed) {
		if(GeneralUtils.getMinecraft().settings.speed && this.speedHack &&  GeneralUtils.getMinecraft().hacks) {
			super.moveRelative(forward, strafe, 2.5F);
		} else {
			super.moveRelative(forward, strafe, speed);
		}
	}
	
	@Override
	public void moveTo(float x, float y, float z, float yaw, float pitch) {
		Position from = new Position(this.level.openclassic, this.x, this.y, this.z, (byte) this.yaw, (byte) this.pitch);
		Position to = new Position(this.level.openclassic, x, y, z, (byte) yaw, (byte) pitch);
		PlayerMoveEvent event = EventManager.callEvent(new PlayerMoveEvent(this.openclassic, from, to));
		if(event.isCancelled()) {
			return;
		}
		
		super.moveTo(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ(), event.getTo().getYaw(), event.getTo().getPitch());
	}
	
	public static class PlayerAI extends BasicAI implements Serializable {
		public static final long serialVersionUID = 0L;

		private LocalPlayer parent;
		
		public PlayerAI(LocalPlayer parent) {
			this.parent = parent;
		}
		
		public void update() {
			this.jumping = this.parent.input.jumping;
			this.flyDown = this.parent.input.flyDown;
			this.parent.speedHack = this.parent.input.speed;
			this.xxa = this.parent.input.xxa;
			this.yya = this.parent.input.yya;
			if(this.parent.input.toggleFly && GeneralUtils.getMinecraft().settings.flying &&  GeneralUtils.getMinecraft().hacks) {
				this.flying = !this.flying; 
				if(this.flying) {
					this.mob.yd = 0;
				}
			}
			
			if(!GeneralUtils.getMinecraft().hacks || !GeneralUtils.getMinecraft().settings.flying) {
				this.flying = false;
			}
			
			this.parent.input.toggleFly = false;
		}
	}

	@Override
	public String getName() {
		return GeneralUtils.getMinecraft().data != null ? GeneralUtils.getMinecraft().data.username : "Player";
	}

}
