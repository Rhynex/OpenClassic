package ch.spacebase.openclassic.client.gui;

public class Chat {

	private String message;
	private int time;
	
	public Chat(String message) {
		this.message = message;
	}
	
	public void incrementTime() {
		this.time++;
	}
	
	public int getTime() {
		return this.time;
	}
	
	public String getMessage() {
		return this.message;
	}
	
}
