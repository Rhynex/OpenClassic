package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.gui.GuiComponent;
import ch.spacebase.openclassic.api.gui.base.BlockPreview;
import ch.spacebase.openclassic.api.gui.base.Button;
import ch.spacebase.openclassic.api.gui.base.ButtonCallback;
import ch.spacebase.openclassic.api.gui.base.FadingBox;
import ch.spacebase.openclassic.api.gui.base.Label;
import ch.spacebase.openclassic.api.input.Mouse;

public class BlockSelectScreen extends GuiComponent {

	private Map<Integer, List<ScreenBlock>> blocks = new LinkedHashMap<Integer, List<ScreenBlock>>();
	private int page = 0;
	
	public BlockSelectScreen() {
		super("blockselectscreen");
	}

	@Override
	public void onAttached(GuiComponent parent) {
		this.setSize(parent.getWidth(), parent.getHeight());
		int count = 0;
		for(BlockType block : Blocks.getBlocks()) {
			if(block != null && block.isSelectable()) {
				int page = (int) Math.floor(((float) count) / 36);
				if(!this.blocks.containsKey(page)) {
					this.blocks.put(page, new ArrayList<ScreenBlock>());
				}
				
				int ind = count - (page * 36);
				int blockX = this.getWidth() / 2 + ind % 9 * 48 - 202;
				int blockY = this.getHeight() / 2 + ind / 9 * 48 - 108;
				this.blocks.get(page).add(new ScreenBlock(block, blockX, blockY));
				count++;
			}
		}
		
		this.attachComponent(new FadingBox("bg", this.getWidth() / 2 - 240, 60, 480, 300, -1878719232, -1070583712));
		this.attachComponent(new Button("back", this.getWidth() / 2 - 230, 310, 50, 40, "<<").setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				if(page > 0) {
					page--;
				}
				
				updateWidgets();
			}
		}));
		
		this.attachComponent(new Button("next", this.getWidth() / 2 + 182, 310, 50, 40, ">>").setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				if(page < blocks.size() - 1) {
					page++;
				}
				
				updateWidgets();
			}
		}));
		
		this.attachComponent(new Label("title", this.getWidth() / 2, 80, OpenClassic.getGame().getTranslator().translate("gui.blocks.select"), true));
		this.attachComponent(new FadingBox("selector", -60, -60, 52, 52, -1862270977, -1056964609));
		if(this.blocks.size() == 0) {
			OpenClassic.getClient().setActiveComponent(null);
		}
		
		this.updateWidgets();
	}

	@Override
	public void onMouseClick(int x, int y, int button) {
		if(button == Mouse.LEFT_BUTTON) {
			ScreenBlock block = this.getBlockOnScreen(x, y);
			if(block != null) {
				OpenClassic.getClient().getPlayer().replaceSelected(block.getBlock());
				OpenClassic.getClient().setActiveComponent(null);
				return;
			}
		}
		
		super.onMouseClick(x, y, button);
	}
	
	@Override
	public void update(int mouseX, int mouseY) {
		ScreenBlock block = this.getBlockOnScreen(mouseX, mouseY);
		if(block != null) {
			this.getComponent("selector", FadingBox.class).setPos(block.getX() - 15, block.getY() - 24);
		} else {
			this.getComponent("selector", FadingBox.class).setPos(-60, -60);
		}
		
		super.update(mouseX, mouseY);
	}
	
	private void updateWidgets() {
		for(int count = 0; count < 36; count++) {
			BlockPreview block = this.getComponent("block" + count, BlockPreview.class);
			boolean filled = false;
			if(this.blocks.get(this.page).size() > count) {
				ScreenBlock b = this.blocks.get(this.page).get(count);
				if(b != null) {
					if(block == null) {
						block = new BlockPreview("block" + count, b.getX(), b.getY(), null);
						this.attachComponent(block);
					}
					
					block.setBlock(b.getBlock());
					filled = true;
				}
			}
			
			if(!filled && block != null) {
				block.setBlock(null);
			}
		}
	}
	
	private ScreenBlock getBlockOnScreen(int x, int y) {
		for(ScreenBlock block : this.blocks.get(this.page)) {
			if(x >= block.getX() && x <= block.getX() + 48 && y >= block.getY() - 24 && y <= block.getY() + 24) {
				return block;
			}
		}

		return null;
	}
}
