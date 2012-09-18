package ch.spacebase.openclassic.client.util;

public class ServerFlags {

	private boolean hacks = false;
	private boolean ctf = false;
	
	public boolean hacks() {
		return this.hacks;
	}
	
	public boolean ctf() {
		return this.ctf;
	}
	
	public void setHacks(boolean hacks) {
		this.hacks = hacks;
	}
	
	public void setCTF(boolean ctf) {
		this.ctf = ctf;
	}
	
	public void clear() {
		this.hacks = false;
		this.ctf = false;
	}
	
}
