package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiComponent;
import ch.spacebase.openclassic.api.gui.base.Button;
import ch.spacebase.openclassic.api.gui.base.ButtonCallback;
import ch.spacebase.openclassic.api.gui.base.TranslucentBackground;
import ch.spacebase.openclassic.api.gui.base.Label;
import ch.spacebase.openclassic.api.gui.base.TextBox;

public class LevelDumpScreen extends GuiComponent {

	private GuiComponent parent;

	public LevelDumpScreen(GuiComponent parent) {
		super("leveldumpscreen");
		this.parent = parent;
	}

	@Override
	public void onAttached(GuiComponent oparent) {
		this.setSize(parent.getWidth(), parent.getHeight());
		this.attachComponent(new TranslucentBackground("bg"));
		this.attachComponent(new TextBox("name", this.getWidth() / 2 - 200, this.getHeight() / 2 - 60, 30));
		this.attachComponent(new Button("save", this.getWidth() / 2 - 200, this.getHeight() / 4 + 240, OpenClassic.getGame().getTranslator().translate("gui.level-dump.dump")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().saveLevel(getComponent("name", TextBox.class).getText());
				OpenClassic.getClient().setActiveComponent(parent);
			}
		}));
		
		this.attachComponent(new Button("back", this.getWidth() / 2 - 200, this.getHeight() / 4 + 288, OpenClassic.getGame().getTranslator().translate("gui.cancel")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setActiveComponent(parent);
			}
		}));
		
		this.attachComponent(new Label("title", this.getWidth() / 2, 80, OpenClassic.getGame().getTranslator().translate("gui.level-dump.name"), true));
		this.getComponent("save", Button.class).setActive(false);
	}

	@Override
	public void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		this.getComponent("save", Button.class).setActive(this.getComponent("name", TextBox.class).getText().trim().length() > 0);
	}
	
}
