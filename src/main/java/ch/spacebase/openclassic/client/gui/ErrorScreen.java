package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonCallback;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.api.input.Keyboard;

public class ErrorScreen extends GuiScreen {

	private final String title;
	private final String message;

	public ErrorScreen(String title, String message) {
		this.title = title;
		this.message = message;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		this.attachWidget(WidgetFactory.getFactory().newButton(1, this.getWidth() / 2 - 100, this.getHeight() / 6 + 120 + 12, this, OpenClassic.getGame().getTranslator().translate("gui.error.main-menu")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().exitGameSession();
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newLabel(2, this.getWidth() / 2, 90, this, this.title, true));
		this.attachWidget(WidgetFactory.getFactory().newLabel(3, this.getWidth() / 2, 110, this, this.message, true));
	}

	public void onKeyPress(char c, int key) {
		if(key != Keyboard.KEY_ESCAPE) {
			super.onKeyPress(c, key);
		}
	}
	
}
