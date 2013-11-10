package ch.spacebase.openclassic.client.gui;

public final class ChatLine {

	private String message;
	private int time;

	public ChatLine(String message) {
		this.message = message;
		this.time = 0;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public int getTime() {
		return this.time;
	}
	
	public void incrementTime() {
		this.time++;
	}
	
}
