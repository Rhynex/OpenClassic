package com.mojang.minecraft.entity.mob.ai;

import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.api.math.Vector;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.client.util.RayTracer;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.item.Arrow;

public class BasicAttackAI extends BasicAI {

	public int damage = 6;

	public void update() {
		super.update();
		if(this.mob.health > 0) {
			this.doAttack();
		}
	}

	protected void doAttack() {
		Entity player = GeneralUtils.getMinecraft().player;
		if(this.attackTarget != null && this.attackTarget.removed) {
			this.attackTarget = null;
		}

		if(player != null && this.attackTarget == null) {
			float sqDistance = player.pos.distanceSquared(mob.pos);
			if(sqDistance < 256) {
				this.attackTarget = player;
			}
		}

		if(this.attackTarget != null) {
			float xDistance = player.pos.getX() - mob.pos.getX();
			float yDistance = player.pos.getY() - mob.pos.getY();
			float zDistance = player.pos.getZ() - mob.pos.getZ();
			float sqDistance = xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;

			if(sqDistance > 1024 && this.random.nextInt(100) == 0) {
				this.attackTarget = null;
			}

			if(this.attackTarget != null) {
				float distance = (float) Math.sqrt(sqDistance);
				this.mob.pos.setYaw((float) (Math.atan2(zDistance, xDistance) * MathHelper.DRAD_TO_DEG) - 90);
				this.mob.pos.setPitch(-((float) (Math.atan2(yDistance, (float) Math.sqrt(distance)) * MathHelper.DRAD_TO_DEG)));
				if((float) Math.sqrt(sqDistance) < 2 && this.attackDelay == 0) {
					this.attack(this.attackTarget);
				}
			}

		}
	}

	public boolean attack(Entity entity) {
		if(RayTracer.rayTrace(entity.getClientLevel(), new Vector(this.mob.pos.getZ(), this.mob.pos.getY(), this.mob.pos.getZ()), new Vector(entity.pos.getX(), entity.pos.getY(), entity.pos.getZ()), false) != null) {
			return false;
		} else {
			this.mob.attackTime = 5;
			this.attackDelay = this.random.nextInt(20) + 10;
			int damage = (int) ((this.random.nextFloat() + this.random.nextFloat()) / 2 * this.damage + 1);
			entity.hurt(this.mob, damage);
			this.noActionTime = 0;
			return true;
		}
	}

	public void hurt(Entity cause, int damage) {
		super.hurt(cause, damage);
		if(cause instanceof Arrow) {
			cause = ((Arrow) cause).getOwner();
		}

		if(cause != null && !cause.getClass().equals(this.mob.getClass())) {
			this.attackTarget = cause;
		}
	}
	
}
