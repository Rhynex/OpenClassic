package ch.spacebase.openclassic.client.gui.widget;

import ch.spacebase.openclassic.api.gui.Screen;
import ch.spacebase.openclassic.api.gui.widget.Label;
import ch.spacebase.openclassic.client.render.RenderHelper;

public class ClientLabel extends Label {

	public ClientLabel(int id, int x, int y, Screen parent, String text) {
		super(id, x, y, parent, text);
	}
	
	public ClientLabel(int id, int x, int y, Screen parent, String text, boolean xCenter) {
		super(id, x, y, parent, text, xCenter);
	}

	public ClientLabel(int id, int x, int y, Screen parent, String text, boolean xCenter, boolean scaled) {
		super(id, x, y, parent, text, xCenter, scaled);
	}

	@Override
	public void render() {
		if(this.isScaled()) {
			RenderHelper.getHelper().renderScaledText(this.getText(), this.getX() + (this.useXCenter() ? (int) RenderHelper.getHelper().getStringWidth(this.getText()) / 2 : 0), this.getY(), this.useXCenter());
		} else {
			RenderHelper.getHelper().renderText(this.getText(), this.getX(), this.getY(), this.useXCenter());
		}
	}

}
