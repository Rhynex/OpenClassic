package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;

public class LevelDumpScreen extends GuiScreen {

	private GuiScreen parent;

	public LevelDumpScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		this.attachWidget(WidgetFactory.getFactory().newTextBox(1, this.getWidth() / 2 - 100, this.getHeight() / 2 - 30, this, 30));
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, this, OpenClassic.getGame().getTranslator().translate("gui.level-dump.dump")));
		this.attachWidget(WidgetFactory.getFactory().newButton(3, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, OpenClassic.getGame().getTranslator().translate("gui.cancel")));
		this.attachWidget(WidgetFactory.getFactory().newLabel(4, this.getWidth() / 2, 40, this, OpenClassic.getGame().getTranslator().translate("gui.level-dump.name"), true));
		
		this.getWidget(2, Button.class).setActive(false);
	}

	public void onButtonClick(Button button) {
		TextBox text = this.getWidget(1, TextBox.class);
		if(button.getId() == 2 && text.getText().trim().length() > 0) {
			OpenClassic.getClient().saveLevel(text.getText());
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}

		if(button.getId() == 3) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}

	public void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		this.getWidget(2, Button.class).setActive(this.getWidget(1, TextBox.class).getText().trim().length() > 0);
	}
	
}
