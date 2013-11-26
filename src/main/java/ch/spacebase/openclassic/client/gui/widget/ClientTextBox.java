package ch.spacebase.openclassic.client.gui.widget;

import ch.spacebase.openclassic.api.gui.Screen;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.client.render.RenderHelper;

/**
 * Represents a text box.
 */
public class ClientTextBox extends TextBox {
	
	public ClientTextBox(int id, int x, int y, Screen parent) {
		super(id, x, y, parent);
	}
	
	public ClientTextBox(int id, int x, int y, Screen parent, int max) {
		super(id, x, y, parent, max);
	}
	
	public ClientTextBox(int id, int x, int y, int width, int height, Screen parent) {
		super(id, x, y, width, height, parent);
	}
	
	public ClientTextBox(int id, int x, int y, int width, int height, Screen parent, int max) {
		super(id, x, y, width, height, parent, max);
	}
	
	public ClientTextBox(int id, int x, int y, Screen parent, boolean chatbox) {
		super(id, x, y, parent, chatbox);
	}
	
	public ClientTextBox(int id, int x, int y, Screen parent, int max, boolean chatbox) {
		super(id, x, y, parent, max, chatbox);
	}
	
	public ClientTextBox(int id, int x, int y, int width, int height, Screen parent, boolean chatbox) {
		super(id, x, y, width, height, parent, chatbox);
	}
	
	public ClientTextBox(int id, int x, int y, int width, int height, Screen parent, int max, boolean chatbox) {
		super(id, x, y, width, height, parent, max, chatbox);
	}
	
	@Override
	public void render() {
		if(!this.chatbox) {
			RenderHelper.getHelper().drawBox(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, -6250336);
		}
		
		RenderHelper.getHelper().drawBox(this.x, this.y, this.x + this.width, this.y + this.height, (!this.chatbox ? -16777216 : Integer.MIN_VALUE));
		RenderHelper.getHelper().renderText(this.text.substring(0, this.cursor) + (this.blink && this.focus ? "|" : "") + this.text.substring(this.cursor, this.text.length()), this.x + 4, (this.chatbox ? this.y + 2 : this.y + 6), 14737632, false);
	}

}
