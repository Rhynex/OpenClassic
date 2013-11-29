package ch.spacebase.openclassic.client.level;

import com.mojang.minecraft.entity.Entity;

public class BlockMapSlot {

	private BlockMap parent;
	private int x;
	private int y;
	private int z;

	public BlockMapSlot init(BlockMap parent, float x, float y, float z) {
		this.parent = parent;
		this.x = (int) (x / 16);
		this.y = (int) (y / 16);
		this.z = (int) (z / 16);
		if(this.x < 0) {
			this.x = 0;
		}

		if(this.y < 0) {
			this.y = 0;
		}

		if(this.z < 0) {
			this.z = 0;
		}

		if(this.x >= parent.getWidth()) {
			this.x = parent.getWidth() - 1;
		}

		if(this.y >= parent.getHeight()) {
			this.y = parent.getHeight() - 1;
		}

		if(this.z >= parent.getDepth()) {
			this.z = parent.getDepth() - 1;
		}

		return this;
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

	public void add(Entity entity) {
		if(this.x >= 0 && this.y >= 0 && this.z >= 0) {
			this.parent.entityGrid[(this.z * this.parent.getHeight() + this.y) * this.parent.getWidth() + this.x].add(entity);
		}

	}

	public void remove(Entity entity) {
		if(this.x >= 0 && this.y >= 0 && this.z >= 0) {
			this.parent.entityGrid[(this.z * this.parent.getHeight() + this.y) * this.parent.getWidth() + this.x].remove(entity);
		}
	}
	
}
