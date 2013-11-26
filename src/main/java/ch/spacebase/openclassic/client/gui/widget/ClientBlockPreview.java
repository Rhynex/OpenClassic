package ch.spacebase.openclassic.client.gui.widget;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.gui.Screen;
import ch.spacebase.openclassic.api.gui.widget.BlockPreview;
import ch.spacebase.openclassic.client.render.RenderHelper;

public class ClientBlockPreview extends BlockPreview {
	
	public ClientBlockPreview(int id, int x, int y, Screen parent, BlockType type) {
		super(id, x, y, parent, type);
	}
	
	public ClientBlockPreview(int id, int x, int y, Screen parent, BlockType type, float scale) {
		super(id, x, y, parent, type, scale);
	}

	@Override
	public void render() {
		if(this.getBlock() != null) {
			RenderHelper.getHelper().drawRotatedBlock(this.getX(), this.getY(), this.getBlock(), this.getScale());
		}
	}

}
