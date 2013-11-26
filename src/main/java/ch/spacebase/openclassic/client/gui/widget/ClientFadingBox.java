package ch.spacebase.openclassic.client.gui.widget;

import ch.spacebase.openclassic.api.gui.Screen;
import ch.spacebase.openclassic.api.gui.widget.FadingBox;
import ch.spacebase.openclassic.client.render.RenderHelper;

public class ClientFadingBox extends FadingBox {

	public ClientFadingBox(int id, int x, int y, int width, int height, Screen parent, int color, int fadeTo) {
		super(id, x, y, width, height, parent, color, fadeTo);
	}

	@Override
	public void render() {
		RenderHelper.getHelper().color(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), this.getColor(), this.getFadeColor());
	}

}
