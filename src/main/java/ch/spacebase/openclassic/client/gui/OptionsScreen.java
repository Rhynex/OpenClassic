package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.gui.widget.ButtonListCallback;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.api.settings.Setting;
import ch.spacebase.openclassic.api.settings.Settings;

public class OptionsScreen extends GuiScreen {

	private GuiScreen parent;
	private Settings settings;

	public OptionsScreen(GuiScreen parent, Settings settings) {
		this.parent = parent;
		this.settings = settings;
	}

	@Override
	public void onOpen() {
		this.clearWidgets();
		if(OpenClassic.getClient().isInGame()) {
			this.attachWidget(WidgetFactory.getFactory().newTranslucentBackground(0, this));
		} else {
			this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		}
		
		ButtonList list = new ButtonList(1, this);
		list.setCallback(new ButtonListCallback() {
			@Override
			public void onButtonListClick(ButtonList list, Button button) {
				int page = list.getCurrentPage();
				settings.getSetting((list.getCurrentPage() * 5) + button.getId()).toggle();
				getWidget(1, ButtonList.class).setContents(buildContents());
				list.setCurrentPage(page);
			}
		});
		
		this.attachWidget(list);
		this.attachWidget(WidgetFactory.getFactory().newButton(75, this.getWidth() / 2 - 100, this.getHeight() / 6 + 148, 98, 20, this, OpenClassic.getGame().getTranslator().translate("gui.options.hacks")));
		this.attachWidget(WidgetFactory.getFactory().newButton(100, this.getWidth() / 2 + 2, this.getHeight() / 6 + 148, 98, 20, this, OpenClassic.getGame().getTranslator().translate("gui.options.controls")));
		this.attachWidget(WidgetFactory.getFactory().newButton(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 172, this, OpenClassic.getGame().getTranslator().translate("gui.done")));
		this.attachWidget(WidgetFactory.getFactory().newLabel(300, this.getWidth() / 2, 20, this, OpenClassic.getGame().getTranslator().translate("gui.options.title"), true));
	
		this.getWidget(1, ButtonList.class).setContents(this.buildContents());
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

	@Override
	public void onButtonClick(Button button) {
		if(button.getId() == 75) {
			OpenClassic.getClient().setCurrentScreen(new HacksScreen(this, OpenClassic.getClient().getHackSettings()));
		}

		if(button.getId() == 100) {
			OpenClassic.getClient().setCurrentScreen(new ControlsScreen(this, OpenClassic.getClient().getBindings()));
		}

		if(button.getId() == 200) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}
	
}
