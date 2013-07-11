package ch.spacebase.openclassic.client.gui.inventory;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.inventory.CraftingInventory;
import ch.spacebase.openclassic.api.inventory.ItemStack;

public class CraftResultSlot extends Slot {

	private CraftingInventory crafting;
	private ItemStack item = null;
	
	public CraftResultSlot(CraftingInventory inv, int slot, int x, int y) {
		super(null, slot, x, y);
		this.crafting = inv;
	}
	
	@Override
	public ItemStack getItem() {
		return this.item;
	}
	
	@Override
	public void setItem(ItemStack item) {
		this.item = item;
	}

	@Override
	public boolean canPutItem() {
		return false;
	}

	@Override
	public void onTakeItem() {
		ItemStack items[] = new ItemStack[9];
		ItemStack contents[] = this.crafting.getContents();
		for(int row = 0; row < 3; row++) {
			for(int col = 0; col < 3; col++) {
				if(row < this.crafting.getWidth() && col < this.crafting.getHeight()) {
					items[row + col * 3] = contents[row + col * this.crafting.getHeight()];
				}
			}
		}
		
		OpenClassic.getClient().getRecipeManager().getCraftingRecipe(items).craft(items);
		for(int row = 0; row < 3; row++) {
			for(int col = 0; col < 3; col++) {
				if(row < this.crafting.getWidth() && col < this.crafting.getHeight()) {
					contents[row + col * this.crafting.getHeight()] = items[row + col * 3];
				}
			}
		}
		
		this.crafting.setContents(contents);
	}

}
