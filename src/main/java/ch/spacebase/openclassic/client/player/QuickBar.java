package ch.spacebase.openclassic.client.player;

import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;

public class QuickBar {

	private byte slots[] = new byte[9];
	private int selected = 0;
	
	public QuickBar() {
		int slot = 0;
		for(VanillaBlock type : VanillaBlock.values()) {
			if(slot == 9) break;
			if(type.isSelectable()) {
				this.setBlock(slot, type.getId());
				slot++;
			}
		}
	}
	
	public int getSelected() {
		return this.selected;
	}
	
	public void setSelected(int selected) {
		this.selected = selected;
	}
	
	public byte getBlock(int slot) {
		return this.slots[slot];
	}
	
	public void setBlock(int slot, byte block) {
		if(Blocks.fromId(block) != null && Blocks.fromId(block).isSelectable()) {
			this.slots[slot] = block;
		}
	}

	public boolean contains(byte block) {
		return this.getSlot(block) != -1;
	}
	
	public int getSlot(byte block) {
		for(int slot = 0; slot < this.slots.length; slot++) {
			if(this.getBlock(slot) == block) return slot;
		}
		
		return -1;
	}

	public void scroll(int mod) {
		this.selected -= mod;
		while(this.selected < 0) {
			this.selected += this.slots.length;
		}

		while(this.selected >= this.slots.length) {
			this.selected -= this.slots.length;
		}
	}
	
}
