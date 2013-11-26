package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.BlockPreview;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonCallback;
import ch.spacebase.openclassic.api.gui.widget.FadingBox;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.api.input.Mouse;
import ch.spacebase.openclassic.api.player.Player;

public class BlockSelectScreen extends GuiScreen {

	private Map<Integer, List<ScreenBlock>> blocks = new LinkedHashMap<Integer, List<ScreenBlock>>();
	private int page = 0;

	@Override
	public void onOpen(Player viewer) {
		int count = 0;
		for(BlockType block : Blocks.getBlocks()) {
			if(block != null && block.isSelectable()) {
				int page = (int) Math.floor(((float) count) / 36);
				if(!this.blocks.containsKey(page)) {
					this.blocks.put(page, new ArrayList<ScreenBlock>());
				}
				
				int ind = count - (page * 36);
				int blockX = this.getWidth() / 2 + ind % 9 * 24 + -108 - 3;
				int blockY = this.getHeight() / 2 + ind / 9 * 24 + -60 + 3;
				this.blocks.get(page).add(new ScreenBlock(block, blockX, blockY));
				count++;
			}
		}
		
		this.attachWidget(WidgetFactory.getFactory().newFadingBox(0, this.getWidth() / 2 - 120, 30, 240, 150, this, -1878719232, -1070583712));
		this.attachWidget(WidgetFactory.getFactory().newButton(1, this.getWidth() / 2 - 115, 155, 25, 20, this, "<<").setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				if(page > 0) {
					page--;
				}
				
				updateWidgets();
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 + 91, 155, 25, 20, this, ">>").setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				if(page < blocks.size() - 1) {
					page++;
				}
				
				updateWidgets();
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newLabel(3, this.getWidth() / 2, 40, this, OpenClassic.getGame().getTranslator().translate("gui.blocks.select"), true));
		this.attachWidget(WidgetFactory.getFactory().newFadingBox(200, -30, -30, 26, 26, this, -1862270977, -1056964609));
		if(this.blocks.size() == 0) {
			OpenClassic.getClient().setCurrentScreen(null);
		}
		
		this.updateWidgets();
	}

	@Override
	public void onMouseClick(int x, int y, int button) {
		if(button == Mouse.LEFT_BUTTON) {
			ScreenBlock block = this.getBlockOnScreen(x, y);
			if(block != null) {
				OpenClassic.getClient().getPlayer().replaceSelected(block.getBlock());
				OpenClassic.getClient().setCurrentScreen(null);
				return;
			}
		}
		
		super.onMouseClick(x, y, button);
	}
	
	@Override
	public void update(int mouseX, int mouseY) {
		ScreenBlock block = this.getBlockOnScreen(mouseX, mouseY);
		if(block != null) {
			this.getWidget(200, FadingBox.class).setPos(block.getX() - 3, block.getY() - 8);
		} else {
			this.getWidget(200, FadingBox.class).setPos(-30, -30);
		}
	}
	
	private void updateWidgets() {
		for(int id = 4; id < 40; id++) {
			BlockPreview block = this.getWidget(id, BlockPreview.class);
			if(block == null) {
				int ind = id - 4;
				int blockX = this.getWidth() / 2 + ind % 9 * 24 + -108 - 3;
				int blockY = this.getHeight() / 2 + ind / 9 * 24 + -60 + 6;
				block = WidgetFactory.getFactory().newBlockPreview(id, blockX, blockY, this, null);
				this.attachWidget(block);
			}
			
			boolean filled = false;
			if(this.blocks.get(this.page).size() > id - 4) {
				ScreenBlock b = this.blocks.get(this.page).get(id - 4);
				if(b != null) {
					block.setBlock(b.getBlock());
					filled = true;
				}
			}
			
			if(!filled) {
				block.setBlock(null);
			}
		}
	}
	
	private ScreenBlock getBlockOnScreen(int x, int y) {
		for(ScreenBlock block : this.blocks.get(this.page)) {
			if(x >= block.getX() && x <= block.getX() + 24 && y >= block.getY() - 12 && y <= block.getY() + 12) {
				return block;
			}
		}

		return null;
	}
}
