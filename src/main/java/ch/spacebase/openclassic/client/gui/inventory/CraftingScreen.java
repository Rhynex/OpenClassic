package ch.spacebase.openclassic.client.gui.inventory;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.inventory.CraftingInventory;
import ch.spacebase.openclassic.api.inventory.CraftingListener;
import ch.spacebase.openclassic.api.inventory.recipe.CraftingRecipe;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.GuiTextures;

public class CraftingScreen extends InventoryScreen implements CraftingListener {

	private CraftingInventory crafting;
	private CraftResultSlot result;
	
	public CraftingScreen() {
		super(GuiTextures.CRAFTING.getSubTexture(0, GuiTextures.CRAFTING.getWidth(), GuiTextures.CRAFTING.getHeight()));
        this.crafting = new CraftingInventory(this, 3, 3);
        this.addSlot(this.result = new CraftResultSlot(this.crafting, 0, 504, 156));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(this.crafting, col + row * 3, 316 + col * 36, 123 + row * 36));
            }
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
		CraftingRecipe recipe = OpenClassic.getClient().getRecipeManager().getCraftingRecipe(this.crafting.getContents());
		if(recipe != null) {
			this.result.setItem(recipe.getResult());
		} else {
			this.result.setItem(null);
		}
	}
	
	@Override
	protected void renderExtra(float x, float y) {
		RenderHelper.getHelper().renderText(Color.GRAY + OpenClassic.getGame().getTranslator().translate("gui.inventory.crafting"), this.getWidth() / 2 - 64, 79);
	}
	
}
