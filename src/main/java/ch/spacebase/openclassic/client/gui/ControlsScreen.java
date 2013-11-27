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
import ch.spacebase.openclassic.api.input.InputHelper;
import ch.spacebase.openclassic.api.settings.bindings.Bindings;
import ch.spacebase.openclassic.api.settings.bindings.KeyBinding;

public class ControlsScreen extends GuiComponent {

	private GuiComponent parent;
	private Bindings bindings;
	private KeyBinding binding = null;

	public ControlsScreen(GuiComponent parent, Bindings bindings) {
		super("controlsscreen");
		this.parent = parent;
		this.bindings = bindings;
	}

	@Override
	public void onAttached(GuiComponent oparent) {
		this.setSize(parent.getWidth(), parent.getHeight());
		if(OpenClassic.getClient().isInGame()) {
			this.attachComponent(new TranslucentBackground("bg"));
		} else {
			this.attachComponent(new DefaultBackground("bg"));
		}
		
		ButtonList list = new ButtonList("controls", 0, 0, this.getWidth(), this.getHeight());
		list.setCallback(new ButtonListCallback() {
			@Override
			public void onButtonListClick(ButtonList list, Button button) {
				int page = list.getCurrentPage();
				getComponent("controls", ButtonList.class).setContents(buildContents());
				list.setCurrentPage(page);
				binding = bindings.getBinding((list.getCurrentPage() * 5) + Integer.parseInt(button.getName().replace("button", "")));
				button.setText("> " + OpenClassic.getGame().getTranslator().translate(binding.getName()) + ": " + InputHelper.getHelper().getKeyName(binding.getKey()) + " <");
			}
		});
		
		this.attachComponent(list);
		this.attachComponent(new Button("done", this.getWidth() / 2 - 200, this.getHeight() / 6 + 344, OpenClassic.getGame().getTranslator().translate("gui.done")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setActiveComponent(parent);
			}
		}));
		
		this.attachComponent(new Label("title", this.getWidth() / 2, 40, OpenClassic.getGame().getTranslator().translate("gui.controls"), true));
		this.getComponent("controls", ButtonList.class).setContents(this.buildContents());
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
			int page = this.getComponent("controls", ButtonList.class).getCurrentPage();
			this.getComponent("controls", ButtonList.class).setContents(this.buildContents());
			this.getComponent("controls", ButtonList.class).setCurrentPage(page);
			this.binding = null;
		} else {
			super.onKeyPress(c, key);
		}
	}
	
}
