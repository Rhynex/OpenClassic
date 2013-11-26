package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.StateButton;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.api.level.LevelInfo;

public class LevelCreateScreen extends GuiScreen {

	private GuiScreen parent;
	private int type = 0;

	public LevelCreateScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		this.attachWidget(WidgetFactory.getFactory().newStateButton(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 48, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.type")));
		this.getWidget(1, StateButton.class).setState("normal");
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 - 100, this.getHeight() / 4 + 72, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.small")));
		this.attachWidget(WidgetFactory.getFactory().newButton(3, this.getWidth() / 2 - 100, this.getHeight() / 4 + 96, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.normal")));
		this.attachWidget(WidgetFactory.getFactory().newButton(4, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.huge")));
		this.attachWidget(WidgetFactory.getFactory().newButton(5, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, OpenClassic.getGame().getTranslator().translate("gui.cancel")));
		this.attachWidget(WidgetFactory.getFactory().newTextBox(6, this.getWidth() / 2 - 100, this.getHeight() / 2 - 45, this, 30));
		this.attachWidget(WidgetFactory.getFactory().newLabel(7, this.getWidth() / 2, 40, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.name"), true));
		
		this.getWidget(2, Button.class).setActive(false);
		this.getWidget(3, Button.class).setActive(false);
		this.getWidget(4, Button.class).setActive(false);
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 1) {
			this.type++;
			if(this.type >= OpenClassic.getGame().getGenerators().size()) {
				this.type = 0;
			}

			((StateButton) button).setState(OpenClassic.getGame().getGenerators().keySet().toArray(new String[OpenClassic.getGame().getGenerators().keySet().size()])[this.type]);
		}

		TextBox text = this.getWidget(6, TextBox.class);
		if((button.getId() == 2 || button.getId() == 3 || button.getId() == 4) && text.getText().trim().length() > 0) {
			short size = (short) (128 << (button.getId() - 2));
			OpenClassic.getClient().getProgressBar().setVisible(true);
			OpenClassic.getClient().getProgressBar().setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.singleplayer"));
			OpenClassic.getClient().getProgressBar().setSubtitle(OpenClassic.getGame().getTranslator().translate("level.generating"));
			OpenClassic.getClient().getProgressBar().setText("");
			OpenClassic.getClient().getProgressBar().setProgress(-1);
			OpenClassic.getClient().getProgressBar().render();
			OpenClassic.getClient().createLevel(new LevelInfo(text.getText(), null, size, (short) 128, size), OpenClassic.getGame().getGenerator(this.getWidget(1, StateButton.class).getState()));
			OpenClassic.getClient().saveLevel();
			OpenClassic.getClient().getProgressBar().setVisible(false);
			OpenClassic.getClient().setCurrentScreen(null);
		}

		if(button.getId() == 5) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}

	public void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		TextBox text = this.getWidget(6, TextBox.class);
		this.getWidget(2, Button.class).setActive(text.getText().trim().length() > 0);
		this.getWidget(3, Button.class).setActive(text.getText().trim().length() > 0);
		this.getWidget(4, Button.class).setActive(text.getText().trim().length() > 0);
	}
	
}
