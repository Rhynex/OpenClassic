package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.input.InputHelper;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.settings.bindings.Bindings;
import ch.spacebase.openclassic.api.settings.bindings.KeyBinding;

public final class ControlsScreen extends GuiScreen {

	private GuiScreen parent;
	private Bindings bindings;
	private KeyBinding binding = null;

	public ControlsScreen(GuiScreen parent, Bindings bindings) {
		this.parent = parent;
		this.bindings = bindings;
	}

	@Override
	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new ButtonList(0, this.getWidth(), this.getHeight(), this));
		this.getWidget(0, ButtonList.class).setContents(this.buildContents());
		this.attachWidget(new Button(100, this.getWidth() / 2 - 100, this.getHeight() / 6 + 172, this, OpenClassic.getGame().getTranslator().translate("gui.done")));
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
	public void onButtonClick(Button button) {
		if(button.getId() == 100) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}
	
	@Override
	public void onButtonListClick(ButtonList list, Button button) {
		int page = list.getCurrentPage();
		this.getWidget(0, ButtonList.class).setContents(this.buildContents());
		list.setCurrentPage(page);
		this.binding = this.bindings.getBinding((list.getCurrentPage() * 5) + button.getId());
		button.setText("> " + OpenClassic.getGame().getTranslator().translate(this.binding.getName()) + ": " + InputHelper.getHelper().getKeyName(this.binding.getKey()) + " <");
	}
	
	@Override
	public void onKeyPress(char c, int key) {
		if(this.binding != null) {
			this.binding.setKey(key);
			int page = this.getWidget(0, ButtonList.class).getCurrentPage();
			this.getWidget(0, ButtonList.class).setContents(this.buildContents());
			this.getWidget(0, ButtonList.class).setCurrentPage(page);
			this.binding = null;
		} else {
			super.onKeyPress(c, key);
		}
	}

	@Override
	public void render() {
		if(OpenClassic.getClient().isInGame()) {
			RenderHelper.getHelper().color(0, 0, this.getWidth(), this.getHeight(), 1610941696, -1607454624);
		} else {
			RenderHelper.getHelper().drawDefaultBG();
		}

		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.controls"), this.getWidth() / 2, 20);
		super.render();
	}
}
