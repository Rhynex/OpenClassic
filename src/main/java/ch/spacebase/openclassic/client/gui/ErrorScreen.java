package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.input.Keyboard;
import ch.spacebase.openclassic.api.render.RenderHelper;

public class ErrorScreen extends GuiScreen {

	private String title;
	private String message;

	public ErrorScreen(String title, String message) {
		this.title = title;
		this.message = message;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 6 + 120 + 12, this, OpenClassic.getGame().getTranslator().translate("gui.error.main-menu")));
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 0) {
			OpenClassic.getClient().exitGameSession();
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 90);
		RenderHelper.getHelper().renderText(this.message, this.getWidth() / 2, 110);
		super.render();
	}

	public void onKeyPress(char c, int key) {
		if(key != Keyboard.KEY_ESCAPE) {
			super.onKeyPress(c, key);
		}
	}
	
}
