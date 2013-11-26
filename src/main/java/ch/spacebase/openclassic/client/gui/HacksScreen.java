package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonCallback;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.gui.widget.ButtonListCallback;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.settings.Settings;

public class HacksScreen extends GuiScreen {

	private GuiScreen parent;
	private Settings settings;

	public HacksScreen(GuiScreen parent, Settings settings) {
		this.parent = parent;
		this.settings = settings;
	}

	@Override
	public void onOpen(Player viewer) {
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
		this.attachWidget(WidgetFactory.getFactory().newButton(100, this.getWidth() / 2 - 100, this.getHeight() / 6 + 172, this, OpenClassic.getGame().getTranslator().translate("gui.done")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(parent);
			}
		}));
		this.attachWidget(WidgetFactory.getFactory().newLabel(2, this.getWidth() / 2, 20, this, OpenClassic.getGame().getTranslator().translate("gui.hacks"), true));
	
		this.getWidget(1, ButtonList.class).setContents(this.buildContents());
	}
	
	private List<String> buildContents() {
		List<String> contents = new ArrayList<String>();
		for(int count = 0; count < this.settings.getSettings().size(); count++) {
			contents.add(OpenClassic.getGame().getTranslator().translate(this.settings.getSetting(count).getName()) + ": " + this.settings.getSetting(count).getStringValue());
		}
		
		return contents;
	}
	
}
