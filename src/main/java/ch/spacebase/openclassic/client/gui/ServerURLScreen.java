package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonCallback;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;

public class ServerURLScreen extends GuiScreen {

	private GuiScreen parent;

	public ServerURLScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		this.attachWidget(WidgetFactory.getFactory().newButton(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, this, OpenClassic.getGame().getTranslator().translate("gui.servers.connect")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().joinServer(getWidget(2, TextBox.class).getText());
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, OpenClassic.getGame().getTranslator().translate("gui.cancel")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(parent);
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newTextBox(3, this.getWidth() / 2 - 100, this.getHeight() / 2 - 10, this));
		this.attachWidget(WidgetFactory.getFactory().newLabel(4, this.getWidth() / 2, 40, this, OpenClassic.getGame().getTranslator().translate("gui.add-favorite.enter-url"), true));
		this.getWidget(3, TextBox.class).setFocus(true);
		this.getWidget(1, Button.class).setActive(false);
	}

	public void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		this.getWidget(1, Button.class).setActive(this.getWidget(3, TextBox.class).getText().length() > 0);
	}
	
}
