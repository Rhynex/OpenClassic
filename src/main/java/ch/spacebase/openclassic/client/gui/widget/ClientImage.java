package ch.spacebase.openclassic.client.gui.widget;

import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.gui.Screen;
import ch.spacebase.openclassic.api.gui.widget.Image;
import ch.spacebase.openclassic.client.render.RenderHelper;

public class ClientImage extends Image {

	public ClientImage(int id, int x, int y, Screen parent, SubTexture tex) {
		super(id, x, y, parent, tex);
	}

	@Override
	public void render() {
		RenderHelper.getHelper().enableBlend();
		RenderHelper.getHelper().drawSubTex(this.getTexture(), this.getX(), this.getY(), 1);
		RenderHelper.getHelper().disableBlend();
	}

}
