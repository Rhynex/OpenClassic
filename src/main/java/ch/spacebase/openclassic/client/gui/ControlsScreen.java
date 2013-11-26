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
import ch.spacebase.openclassic.api.input.InputHelper;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.settings.bindings.Bindings;
import ch.spacebase.openclassic.api.settings.bindings.KeyBinding;

public class ControlsScreen extends GuiScreen {

	private GuiScreen parent;
	private Bindings bindings;
	private KeyBinding binding = null;

	public ControlsScreen(GuiScreen parent, Bindings bindings) {
		this.parent = parent;
		this.bindings = bindings;
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
				getWidget(1, ButtonList.class).setContents(buildContents());
				list.setCurrentPage(page);
				binding = bindings.getBinding((list.getCurrentPage() * 5) + button.getId());
				button.setText("> " + OpenClassic.getGame().getTranslator().translate(binding.getName()) + ": " + InputHelper.getHelper().getKeyName(binding.getKey()) + " <");
			}
		});
		
		this.attachWidget(list);
		this.attachWidget(WidgetFactory.getFactory().newButton(100, this.getWidth() / 2 - 100, this.getHeight() / 6 + 172, this, OpenClassic.getGame().getTranslator().translate("gui.done")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(parent);
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newLabel(2, this.getWidth() / 2, 20, this, OpenClassic.getGame().getTranslator().translate("gui.controls"), true));
		this.getWidget(1, ButtonList.class).setContents(this.buildContents());
	}
	
	private List<String> buildContents() {
		List<String> contents = new ArrayList<String>();
		for(int index = 0; index < this.bindings.getBindings().size(); index++) {
			KeyBinding binding = this.bindings.getBinding(index);
			contents.add(OpenClassic.getGame().getTranslator().translate(binding.getName()) + ": " + InputHelper.getHelper().getKeyName(binding.getKey()));
		}
		
		return contents;
	}
	
	@Override
	public void onKeyPress(char c, int key) {
		if(this.binding != null) {
			this.binding.setKey(key);
			int page = this.getWidget(1, ButtonList.class).getCurrentPage();
			this.getWidget(1, ButtonList.class).setContents(this.buildContents());
			this.getWidget(1, ButtonList.class).setCurrentPage(page);
			this.binding = null;
		} else {
			super.onKeyPress(c, key);
		}
	}
	
}
