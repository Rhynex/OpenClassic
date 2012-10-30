package ch.spacebase.openclassic.client.gui;

import java.util.Arrays;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.player.QuickBar;


public final class BlockSelectScreen extends GuiScreen {

	private static int xmap[] = new int[256 * 16];
	private static int ymap[] = new int[256 * 16];
	
	public BlockSelectScreen() {
		Arrays.fill(xmap, -1);
		Arrays.fill(ymap, -1);
		this.setGrabsInput(false);
	}

	private BlockType getBlockOnScreen(int x, int y) {
		int count = 0;
		for(BlockType block : Blocks.getBlocks()) {
			if(block != null && block.isSelectable()) {
				int blockX = this.getWidth() / 2 + count % 9 * 48 - 216;
				int blockY = this.getHeight() / 2 + count / 9 * 48 - 120;
				if(xmap[block.getId() * 16 + block.getData()] != blockX) xmap[block.getId() * 16 + block.getData()] = blockX;
				if(ymap[block.getId() * 16 + block.getData()] != blockY) ymap[block.getId() * 16 + block.getData()] = blockY;
				if(x >= blockX && x <= blockX + 48 && y >= blockY - 24 && y <= blockY + 24) {
					return block;
				}
				
				count++;
			}
		}

		return null;
	}

	public void render() {
		int mouseX = RenderHelper.getHelper().getRenderMouseX();
		int mouseY = RenderHelper.getHelper().getRenderMouseY();
		
		BlockType block = this.getBlockOnScreen(mouseX, mouseY);
		RenderHelper.getHelper().color(this.getWidth() / 2 - 240, 60, this.getWidth() / 2 + 240, 360, -1878719232, -1070583712);
		if(block != null) {
			int selectX = xmap[block.getId() * 16 + block.getData()];
			int selectY = ymap[block.getId() * 16 + block.getData()];
			RenderHelper.getHelper().color(selectX - 6, selectY - 16, selectX + 46, selectY + 36, -1862270977, -1056964609);
		}

		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.blocks.select"), this.getWidth() / 2, 80);

		int count = 0;
		for(BlockType b : Blocks.getBlocks()) {
			if(b != null && b.isSelectable()) {
				RenderHelper.getHelper().drawRotatedBlock(this.getWidth() / 2 + count % 9 * 48 - 206, this.getHeight() / 2 + count / 9 * 48 - 114, b, 2);
				count++;
			}
		}
	}

	public void onMouseClick(int x, int y, int button) {
		if(button == 0) {
			QuickBar bar = ((ClientPlayer) OpenClassic.getClient().getPlayer()).getQuickBar();
			BlockType block = this.getBlockOnScreen(x, y);
			if(bar.contains(block)) {
				BlockType current = bar.getBlock(bar.getSelected());
				int slot = bar.getSlot(block);
				bar.setBlock(bar.getSelected(), block);
				bar.setBlock(slot, current);
			} else {
				bar.setBlock(bar.getSelected(), block);
			}
			
			OpenClassic.getClient().setCurrentScreen(null);
		}
	}
	
}
