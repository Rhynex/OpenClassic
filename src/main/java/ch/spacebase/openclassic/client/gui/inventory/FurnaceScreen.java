package ch.spacebase.openclassic.client.gui.inventory;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.asset.texture.SubTexture;
import ch.spacebase.openclassic.api.block.complex.vanilla.Furnace;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.GuiTextures;

public class FurnaceScreen extends InventoryScreen {

	private Furnace furnace;
	
	public FurnaceScreen(Furnace furnace) {
		super(GuiTextures.FURNACE);
        this.furnace = furnace;
        this.addSlot(new Slot(furnace.getInventory(), 0, 368, 123));
        this.addSlot(new Slot(furnace.getInventory(), 1, 368, 195));
        this.addSlot(new Slot(furnace.getInventory(), 2, 488, 159));
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
	protected void renderExtra(float x, float y) {
		RenderHelper.getHelper().renderText(Color.GRAY + OpenClassic.getGame().getTranslator().translate("gui.inventory.furnace"), this.getWidth() / 2 - 24, 84);
		RenderHelper.getHelper().renderText(Color.GRAY + OpenClassic.getGame().getTranslator().translate("gui.inventory.title"), this.getWidth() / 2 - 121, 217);
		if(this.furnace.hasFuel()) {
			int burn = this.furnace.getRemainingFuel(24);
			SubTexture tex = GuiTextures.FURNACE_TEXTURES.getSubTexture(352, 24 - burn, 28, burn + 4);
			RenderHelper.getHelper().drawSubTex(tex, x + 112, (y + 96) - burn, 1);
		}

		int cook = this.furnace.getRemainingSmeltTime(48);
		SubTexture tex = GuiTextures.FURNACE_TEXTURES.getSubTexture(352, 28, cook + 2, 32);
		RenderHelper.getHelper().drawSubTex(tex, x + 158, y + 68, 1);
	}
	
}
