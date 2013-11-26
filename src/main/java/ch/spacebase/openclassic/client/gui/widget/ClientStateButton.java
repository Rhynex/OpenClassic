package ch.spacebase.openclassic.client.gui.widget;

import ch.spacebase.openclassic.api.gui.Screen;
import ch.spacebase.openclassic.api.gui.widget.StateButton;

public class ClientStateButton extends StateButton {

	public ClientStateButton(int id, int x, int y, Screen parent, String text) {
		super(id, x, y, parent, text);
	}
	
	public ClientStateButton(int id, int x, int y, int width, int height, Screen parent, String text) {
		super(id, x, y, width, height, parent, text);
	}
	
	@Override
	public void render() {
		String text = this.getText();
		this.setText(text + ": " + this.getState());
		ClientButton.renderButton(this);
		this.setText(text);
	}
	
}
