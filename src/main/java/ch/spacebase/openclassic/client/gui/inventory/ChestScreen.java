package ch.spacebase.openclassic.client.gui.inventory;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.complex.vanilla.Chest;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.GuiTextures;

public class ChestScreen extends InventoryScreen {

	public ChestScreen(Chest chest) {
		super(GuiTextures.CHEST.getSubTexture(0, GuiTextures.CHEST.getWidth(), GuiTextures.CHEST.getHeight()));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(chest.getInventory(), col + row * 9, 272 + col * 36, 125 + row * 36));
            }
        }
        
		for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(OpenClassic.getClient().getPlayer().getInventory(), col + (row + 1) * 9, 272 + col * 36, 259 + row * 36));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(OpenClassic.getClient().getPlayer().getInventory(), col, 272 + col * 36, 375));
        }
	}
	
	@Override
	protected void renderExtra(float x, float y) {
		RenderHelper.getHelper().renderText(Color.GRAY + OpenClassic.getGame().getTranslator().translate("gui.inventory.chest"), this.getWidth() / 2 - 136, 84);
		RenderHelper.getHelper().renderText(Color.GRAY + OpenClassic.getGame().getTranslator().translate("gui.inventory.title"), this.getWidth() / 2 - 121, 219);
	}
	
}
