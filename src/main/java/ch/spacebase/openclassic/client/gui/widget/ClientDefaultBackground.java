package ch.spacebase.openclassic.client.gui.widget;

import ch.spacebase.openclassic.api.gui.Screen;
import ch.spacebase.openclassic.api.gui.widget.DefaultBackground;
import ch.spacebase.openclassic.client.render.RenderHelper;

public class ClientDefaultBackground extends DefaultBackground {

	public ClientDefaultBackground(int id, Screen parent) {
		super(id, parent);
	}

	@Override
	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
	}

}
