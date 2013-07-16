package com.mojang.minecraft;

import java.awt.Canvas;

public class MinecraftCanvas extends Canvas {
	
	private static final long serialVersionUID = 1L;
	private Thread thread;
	private Minecraft mc;
	
	public synchronized void setMinecraft(Minecraft mc) {
		this.mc = mc;
	}
	
	public synchronized void addNotify() {
		super.addNotify();
		
		if (this.thread == null) {
			this.thread = new Thread(this.mc, "Client");
			this.thread.start();
		}
	}

	public synchronized void removeNotify() {
		this.stopThread();
		super.removeNotify();
	}
	
	public synchronized void stopThread() {
		if (this.thread != null) {
			this.mc.running = false;

			try {
				this.thread.join(15000);
			} catch (InterruptedException e) {
				System.exit(0);
			}

			this.thread = null;
		}
	}
	
}
