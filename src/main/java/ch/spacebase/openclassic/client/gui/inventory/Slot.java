package ch.spacebase.openclassic.client.gui.inventory;

import ch.spacebase.openclassic.api.inventory.Inventory;
import ch.spacebase.openclassic.api.inventory.ItemStack;

public class Slot {

	private Inventory inv;
	private int slot;
	private int x;
	private int y;
	
	public Slot(Inventory inv, int slot, int x, int y) {
		this.inv = inv;
		this.slot = slot;
		this.x = x;
		this.y = y;
	}
	
	public Inventory getInventory() {
		return this.inv;
	}
	
	public int getSlot() {
		return this.slot;
	}
	
	public ItemStack getItem() {
		return this.inv.getItem(this.slot);
	}
	
	public void setItem(ItemStack item) {
		this.inv.setItem(this.slot, item);
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public boolean canTakeItem() {
		return true;
	}
	
	public boolean canPutItem() {
		return true;
	}
	
	public void onTakeItem() {
	}
	
	public void onPutItem() {
	}
	
	public void onSwapItem() {
	}
	
	public void onTakePartialItem() {
	}
	
}
