package com.mojang.minecraft;

public final class Timer {

	public float tps;
	public double lastHR;
	public int elapsedTicks;
	public float delta;
	public float speed = 1;
	public float elapsedPartialTicks = 0;
	public long lastSysClock;
	public long lastHRClock;
	public double adjustment = 1;

	public Timer(float tps) {
		this.tps = tps;
		this.lastSysClock = System.currentTimeMillis();
		this.lastHRClock = System.nanoTime() / 1000000L;
	}
	
	public void update() {
		long sysClock = System.currentTimeMillis() - this.lastSysClock;
		long hrClock = System.nanoTime() / 1000000L;
		if(sysClock > 1000) {
			long diff = hrClock - this.lastHRClock;
			double adj = (double) sysClock / (double) diff;
			this.adjustment += (adj - this.adjustment) * 0.20000000298023224D;
			this.lastSysClock = System.currentTimeMillis();
			this.lastHRClock = hrClock;
		}

		if(sysClock < 0L) {
			this.lastSysClock = System.currentTimeMillis();
			this.lastHRClock = hrClock;
		}

		double sec = hrClock / 1000D;
		double add = (sec - this.lastHR) * this.adjustment;
		this.lastHR = sec;
		if(add < 0) {
			add = 0;
		}

		if(add > 1) {
			add = 1;
		}

		this.elapsedPartialTicks = (float) (this.elapsedPartialTicks + add * this.speed * this.tps);
		this.elapsedTicks = (int) this.elapsedPartialTicks;
		if(this.elapsedTicks > 100) {
			this.elapsedTicks = 100;
		}

		this.elapsedPartialTicks -= this.elapsedTicks;
		this.delta = this.elapsedPartialTicks;
	}
	
}
