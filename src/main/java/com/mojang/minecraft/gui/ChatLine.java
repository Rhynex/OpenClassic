package com.mojang.minecraft.gui;

public final class ChatLine {

	public String message;
	public int time;

	public ChatLine(String message) {
		this.message = message;
		this.time = 0;
	}
}
