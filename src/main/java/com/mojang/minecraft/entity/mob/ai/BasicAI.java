package com.mojang.minecraft.entity.mob.ai;

import java.util.List;
import java.util.Random;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.mob.Mob;
import com.mojang.minecraft.level.Level;

public class BasicAI extends AI {

	public Random random = new Random();
	public float xxa;
	public float yya;
	protected float yawA;
	public Level level;
	public Mob mob;
	public boolean jumping = false;
	protected int attackDelay = 0;
	public float runSpeed = 0.7F;
	protected int noActionTime = 0;
	public Entity attackTarget = null;
	public boolean flying = false;
	public boolean flyDown = false;

	public void tick(Level level, Mob mob) {
		this.noActionTime++;

		Entity player = level.minecraft.player;
		if(this.noActionTime > 600 && this.random.nextInt(800) == 0 && player != null) {
			float xDistance = player.x - mob.x;
			float yDistance = player.y - mob.y;
			float zDistance = player.z - mob.z;
			float sqDistance = xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;

			if(sqDistance < 1024) {
				this.noActionTime = 0;
			} else {
				mob.remove();
			}
		}

		this.level = level;
		this.mob = mob;
		if(this.attackDelay > 0) {
			this.attackDelay--;
		}

		if(mob.health <= 0) {
			this.jumping = false;
			this.xxa = 0;
			this.yya = 0;
			this.yawA = 0;
		} else {
			this.update();
		}

		if(this.jumping) {
			if(this.flying) {
				mob.move(0, 0.4F, 0);
			} else if(mob.getLiquid() != null) {
				mob.yd += 0.04F;
			} else if(mob.onGround) {
				this.jumpFromGround();
			}
		}

		if(this.flying) {
			if(this.flyDown) {
				mob.move(0, -0.4F, 0);
				if(mob.onGround) {
					this.flying = false;
				}
			}
		}

		this.xxa *= 0.98F;
		this.yya *= 0.98F;

		this.yawA *= 0.9F;
		mob.travel(this.xxa, this.yya);
		List<Entity> entities = level.findEntities(mob, mob.bb.grow(0.2F, 0, 0.2F));
		if(entities != null && entities.size() > 0) {
			for(Entity e : entities) {
				if(e.isPushable()) {
					e.push(mob);
				}
			}
		}

	}

	protected void jumpFromGround() {
		this.mob.yd = 0.42F;
	}

	public void update() {
		if(this.random.nextFloat() < 0.07F) {
			this.xxa = (this.random.nextFloat() - 0.5F) * this.runSpeed;
			this.yya = this.random.nextFloat() * this.runSpeed;
		}

		this.jumping = this.random.nextFloat() < 0.01F;
		if(this.random.nextFloat() < 0.04F) {
			this.yawA = (this.random.nextFloat() - 0.5F) * 60;
		}

		this.mob.yaw += this.yawA;
		this.mob.pitch = this.defaultLookAngle;
		if(this.attackTarget != null) {
			this.yya = this.runSpeed;
			this.jumping = this.random.nextFloat() < 0.04F;
		}

		if(this.mob.getLiquid() != null) {
			this.jumping = this.random.nextFloat() < 0.8F;
		}
	}

	public void beforeRemove() {
	}

	public void hurt(Entity cause, int damage) {
		this.noActionTime = 0;
	}
	
}
