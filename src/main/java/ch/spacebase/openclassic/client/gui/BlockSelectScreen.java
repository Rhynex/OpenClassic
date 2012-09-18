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

	private static int xmap[] = new int[256];
	private static int ymap[] = new int[256];
	
	public BlockSelectScreen() {
		Arrays.fill(xmap, -1);
		Arrays.fill(ymap, -1);
		this.setGrabsInput(false);
	}

	private int getBlockOnScreen(int x, int y) {
		int count = 0;
		for(BlockType block : Blocks.getBlocks()) {
			if(block != null && block.isSelectable()) {
				int blockX = this.getWidth() / 2 + count % 9 * 24 - 108;
				int blockY = this.getHeight() / 2 + count / 9 * 24 - 60;
				if(xmap[block.getId()] != blockX) xmap[block.getId()] = blockX;
				if(ymap[block.getId()] != blockY) ymap[block.getId()] = blockY;
				if(x >= blockX && x <= blockX + 24 && y >= blockY - 12 && y <= blockY + 12) {
					return block.getId();
				}
				
				count++;
			}
		}

		return -1;
	}

	public void render() {
		int mouseX = RenderHelper.getHelper().getRenderMouseX();
		int mouseY = RenderHelper.getHelper().getRenderMouseY();
		
		int block = this.getBlockOnScreen(mouseX, mouseY);
		RenderHelper.getHelper().color(this.getWidth() / 2 - 120, 30, this.getWidth() / 2 + 120, 180, -1878719232, -1070583712);
		if(block >= 0) {
			int selectX = xmap[block];
			int selectY = ymap[block];
			RenderHelper.getHelper().color(selectX - 3, selectY - 8, selectX + 23, selectY + 24 - 6, -1862270977, -1056964609);
		}

		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.blocks.select"), this.getWidth() / 2, 40);

		int count = 0;
		for(BlockType b : Blocks.getBlocks()) {
			if(b != null && b.isSelectable()) {
				RenderHelper.getHelper().drawRotatedBlock(this.getWidth() / 2 + count % 9 * 24 - 108, this.getHeight() / 2 + count / 9 * 24 - 57, b);
				count++;
			}
		}
	}

	public void onMouseClick(int x, int y, int button) {
		if(button == 0) {
			QuickBar bar = ((ClientPlayer) OpenClassic.getClient().getPlayer()).getQuickBar();
			byte block = (byte) this.getBlockOnScreen(x, y);
			if(bar.contains(block)) {
				byte current = bar.getBlock(bar.getSelected());
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
