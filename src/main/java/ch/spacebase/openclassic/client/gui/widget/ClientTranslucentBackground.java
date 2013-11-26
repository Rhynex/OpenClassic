package ch.spacebase.openclassic.client.gui.widget;

import ch.spacebase.openclassic.api.gui.Screen;
import ch.spacebase.openclassic.api.gui.widget.TranslucentBackground;
import ch.spacebase.openclassic.client.render.RenderHelper;

public class ClientTranslucentBackground extends TranslucentBackground {

	public ClientTranslucentBackground(int id, Screen parent) {
		super(id, parent);
	}

	@Override
	public void render() {
		RenderHelper.getHelper().color(0, 0, this.getParent().getWidth(), this.getParent().getHeight(), 1610941696, -1607454624);
	}

}
