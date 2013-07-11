package ch.spacebase.openclassic.client.gui.inventory;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.inventory.CraftingInventory;
import ch.spacebase.openclassic.api.inventory.ItemStack;
import ch.spacebase.openclassic.api.inventory.recipe.CraftingRecipe;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.GuiTextures;

public class PlayerInventoryScreen extends InventoryScreen {

	private CraftingInventory crafting;
	private CraftResultSlot result;
	
	public PlayerInventoryScreen() {
		super(GuiTextures.INVENTORY.getSubTexture(0, GuiTextures.INVENTORY.getWidth(), GuiTextures.INVENTORY.getHeight()));
        this.crafting = new CraftingInventory(this, 2, 2);
		this.addSlot(this.result = new CraftResultSlot(this.crafting, 0, 544, 160));
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                this.addSlot(new Slot(this.crafting, col + row * 2, 432 + col * 36, 141 + row * 36));
            }
        }

        for (int row = 0; row < 4; row++) {
            //this.addSlot(new ArmorSlot(inv, inv.getSize() - 1 - row, 272, 305 + row * 36, row));
        }
		
		for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(OpenClassic.getClient().getPlayer().getInventory(), col + (row + 1) * 9, 272 + col * 36, 257 + row * 36));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(OpenClassic.getClient().getPlayer().getInventory(), col, 272 + col * 36, 373));
        }
	}
	
	@Override
	public void updateCrafting(CraftingInventory inv) {
		ItemStack items[] = new ItemStack[9];
		ItemStack contents[] = this.crafting.getContents();
		for(int row = 0; row < 3; row++) {
			for(int col = 0; col < 3; col++) {
				if(row < 2 && col < 2) {
					items[row + col * 3] = contents[row + col * 2];
				}
			}
		}
		
		CraftingRecipe recipe = OpenClassic.getClient().getRecipeManager().getCraftingRecipe(items);
		if(recipe != null) {
			this.result.setItem(recipe.getResult());
		} else {
			this.result.setItem(null);
		}
	}
	
	@Override
	protected void renderExtra(float x, float y) {
		RenderHelper.getHelper().renderText(Color.GRAY + OpenClassic.getGame().getTranslator().translate("gui.inventory.crafting"), this.getWidth() / 2 + 34, 99);
	}
	
}
