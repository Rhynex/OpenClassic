package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.render.RenderHelper;

public class LevelDumpScreen extends GuiScreen {

	private GuiScreen parent;
	private TextBox widget;

	public LevelDumpScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.widget = new TextBox(0, this.getWidth() / 2 - 100, this.getHeight() / 2 - 30, this, 30);

		this.clearWidgets();
		this.attachWidget(this.widget);
		this.attachWidget(new Button(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, this, OpenClassic.getGame().getTranslator().translate("gui.level-dump.dump")));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, OpenClassic.getGame().getTranslator().translate("gui.cancel")));

		this.getWidget(1, Button.class).setActive(false);
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 1 && this.widget.getText().trim().length() > 0) {
			OpenClassic.getClient().saveLevel(this.widget.getText());
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}

		if(button.getId() == 2) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}

	public void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		this.getWidget(1, Button.class).setActive(this.widget.getText().trim().length() > 0);
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.level-dump.name"), this.getWidth() / 2, 40);

		super.render();
	}
}
