package com.mojang.minecraft.entity.player;

import com.mojang.minecraft.settings.Bindings;

public final class InputHandler {

	public float xxa = 0;
	public float yya = 0;
	public boolean jumping = false;
	public boolean speed = false;
	public boolean flyDown = false;
	private int flyDelay = 0;
	public boolean toggleFly = false;

	private boolean[] keyStates = new boolean[10];
	private Bindings bindings;

	public InputHandler(Bindings bindings) {
		this.bindings = bindings;
	}

	public final void setKeyState(int key, boolean pressed) {
		byte index = -1;

		if(key == this.bindings.forwardKey.key) {
			index = 0;
		}

		if(key == this.bindings.backKey.key) {
			index = 1;
		}

		if(key == this.bindings.leftKey.key) {
			index = 2;
		}

		if(key == this.bindings.rightKey.key) {
			index = 3;
		}

		if(key == this.bindings.jumpKey.key) {
			index = 4;
		}

		if(key == this.bindings.speedHackKey.key) {
			index = 5;
		}

		if(key == this.bindings.flyDownKey.key) {
			index = 6;
		}

		if(index >= 0) {
			this.keyStates[index] = pressed;
		}
	}

	public final void resetKeys() {
		for(int index = 0; index < 10; index++) {
			this.keyStates[index] = false;
		}
	}

	public final void updateMovement() {
		if(this.flyDelay > 0) {
			this.flyDelay--;
		}

		this.xxa = 0;
		this.yya = 0;

		if(this.keyStates[0]) {
			this.yya--;
		}

		if(this.keyStates[1]) {
			this.yya++;
		}

		if(this.keyStates[2]) {
			this.xxa--;
		}

		if(this.keyStates[3]) {
			this.xxa++;
		}

		this.jumping = this.keyStates[4];
		this.speed = this.keyStates[5];
		this.flyDown = this.keyStates[6];
	}

	public final void keyPress(int key) {
		if(key == this.bindings.jumpKey.key) {
			if(this.flyDelay == 0) {
				this.flyDelay = 10;
			} else {
				this.toggleFly = true;
				this.flyDelay = 0;
			}
		}
	}
}
