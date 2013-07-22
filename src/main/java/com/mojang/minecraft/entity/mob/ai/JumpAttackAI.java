package com.mojang.minecraft.entity.mob.ai;


public class JumpAttackAI extends BasicAttackAI {

	public JumpAttackAI() {
		this.runSpeed *= 0.8F;
	}

	protected void jumpFromGround() {
		if (this.attackTarget == null) {
			super.jumpFromGround();
		} else {
			this.mob.xd = 0.0F;
			this.mob.zd = 0.0F;
			this.mob.moveRelative(0.0F, 1.0F, 0.6F);
			this.mob.yd = 0.5F;
		}
	}
}
