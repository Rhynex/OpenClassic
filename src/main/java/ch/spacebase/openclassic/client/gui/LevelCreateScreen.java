package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonCallback;
import ch.spacebase.openclassic.api.gui.widget.StateButton;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.player.Player;

public class LevelCreateScreen extends GuiScreen {

	private GuiScreen parent;
	private int type = 0;

	public LevelCreateScreen(GuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void onOpen(Player viewer) {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		this.attachWidget(WidgetFactory.getFactory().newStateButton(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 48, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.type")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				type++;
				if(type >= OpenClassic.getGame().getGenerators().size()) {
					type = 0;
				}

				String generators[] = OpenClassic.getGame().getGenerators().keySet().toArray(new String[OpenClassic.getGame().getGenerators().keySet().size()]);
				((StateButton) button).setState(generators[type]);
			}
		}));
		
		this.getWidget(1, StateButton.class).setState("normal");
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 - 100, this.getHeight() / 4 + 72, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.small")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				generate(getWidget(6, TextBox.class).getText(), (short) 0);
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(3, this.getWidth() / 2 - 100, this.getHeight() / 4 + 96, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.normal")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				generate(getWidget(6, TextBox.class).getText(), (short) 1);
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(4, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.huge")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				generate(getWidget(6, TextBox.class).getText(), (short) 2);
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(5, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, OpenClassic.getGame().getTranslator().translate("gui.cancel")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(parent);
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newTextBox(6, this.getWidth() / 2 - 100, this.getHeight() / 2 - 45, this, 30));
		this.attachWidget(WidgetFactory.getFactory().newLabel(7, this.getWidth() / 2, 40, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.name"), true));
		
		this.getWidget(2, Button.class).setActive(false);
		this.getWidget(3, Button.class).setActive(false);
		this.getWidget(4, Button.class).setActive(false);
	}

	@Override
	public void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		TextBox text = this.getWidget(6, TextBox.class);
		this.getWidget(2, Button.class).setActive(text.getText().trim().length() > 0);
		this.getWidget(3, Button.class).setActive(text.getText().trim().length() > 0);
		this.getWidget(4, Button.class).setActive(text.getText().trim().length() > 0);
	}
	
	private void generate(String name, short sizeId) {
		short size = (short) (128 << (sizeId - 2));
		OpenClassic.getClient().getProgressBar().setVisible(true);
		OpenClassic.getClient().getProgressBar().setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.singleplayer"));
		OpenClassic.getClient().getProgressBar().setSubtitle(OpenClassic.getGame().getTranslator().translate("level.generating"));
		OpenClassic.getClient().getProgressBar().setText("");
		OpenClassic.getClient().getProgressBar().setProgress(-1);
		OpenClassic.getClient().getProgressBar().render();
		OpenClassic.getClient().createLevel(new LevelInfo(name, null, size, (short) 128, size), OpenClassic.getGame().getGenerator(this.getWidget(1, StateButton.class).getState()));
		OpenClassic.getClient().saveLevel();
		OpenClassic.getClient().getProgressBar().setVisible(false);
		OpenClassic.getClient().setCurrentScreen(null);
	}
	
}
