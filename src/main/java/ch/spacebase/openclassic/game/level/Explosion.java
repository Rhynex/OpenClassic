package ch.spacebase.openclassic.game.level;

public class Explosion {

	private int time = 20;
	private int x;
	private int y;
	private int z;
	private int power;
	
	public Explosion(int x, int y, int z, int power) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.power = power;
	}
	
	public int getTime() {
		return this.time;
	}
	
	public void decrementTime() {
		this.time--;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public int getZ() {
		return this.z;
	}
	
	public int getPower() {
		return this.power;
	}
	
}
