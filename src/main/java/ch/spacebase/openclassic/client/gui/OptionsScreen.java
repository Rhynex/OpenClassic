package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiComponent;
import ch.spacebase.openclassic.api.gui.base.Button;
import ch.spacebase.openclassic.api.gui.base.ButtonCallback;
import ch.spacebase.openclassic.api.gui.base.ButtonList;
import ch.spacebase.openclassic.api.gui.base.ButtonListCallback;
import ch.spacebase.openclassic.api.gui.base.DefaultBackground;
import ch.spacebase.openclassic.api.gui.base.Label;
import ch.spacebase.openclassic.api.gui.base.TranslucentBackground;
import ch.spacebase.openclassic.api.settings.Setting;
import ch.spacebase.openclassic.api.settings.Settings;

public class OptionsScreen extends GuiComponent {

	private GuiComponent parent;
	private Settings settings;

	public OptionsScreen(GuiComponent parent, Settings settings) {
		super("optionsscreen");
		this.parent = parent;
		this.settings = settings;
	}

	@Override
	public void onAttached(GuiComponent oparent) {
		this.setSize(parent.getWidth(), parent.getHeight());
		if(OpenClassic.getClient().isInGame()) {
			this.attachComponent(new TranslucentBackground("bg"));
		} else {
			this.attachComponent(new DefaultBackground("bg"));
		}
		
		ButtonList list = new ButtonList("options", 0, 0, this.getWidth(), this.getHeight());
		list.setCallback(new ButtonListCallback() {
			@Override
			public void onButtonListClick(ButtonList list, Button button) {
				int page = list.getCurrentPage();
				settings.getSetting((list.getCurrentPage() * 5) + Integer.parseInt(button.getName().replace("button", ""))).toggle();
				getComponent("options", ButtonList.class).setContents(buildContents());
				list.setCurrentPage(page);
			}
		});
		
		this.attachComponent(list);
		this.attachComponent(new Button("hacks", this.getWidth() / 2 - 200, this.getHeight() / 6 + 296, 196, 40, OpenClassic.getGame().getTranslator().translate("gui.options.hacks")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setActiveComponent(new HacksScreen(OptionsScreen.this, OpenClassic.getClient().getHackSettings()));
			}
		}));
		
		this.attachComponent(new Button("controls", this.getWidth() / 2 + 4, this.getHeight() / 6 + 296, 196, 40, OpenClassic.getGame().getTranslator().translate("gui.options.controls")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setActiveComponent(new ControlsScreen(OptionsScreen.this, OpenClassic.getClient().getBindings()));
			}
		}));
		
		this.attachComponent(new Button("back", this.getWidth() / 2 - 200, this.getHeight() / 6 + 344, OpenClassic.getGame().getTranslator().translate("gui.done")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setActiveComponent(parent);
			}
		}));
		
		this.attachComponent(new Label("title", this.getWidth() / 2, 40, OpenClassic.getGame().getTranslator().translate("gui.options.title"), true));
		this.getComponent("options", ButtonList.class).setContents(this.buildContents());
	}
	
	private List<String> buildContents() {
		List<String> contents = new ArrayList<String>();
		for(int count = 0; count < this.settings.getSettings().size(); count++) {
			Setting setting = this.settings.getSetting(count);
			if(setting.isVisible()) {
				contents.add(OpenClassic.getGame().getTranslator().translate(setting.getName()) + ": " + setting.getStringValue());
			}
		}
		
		return contents;
	}
	
}
