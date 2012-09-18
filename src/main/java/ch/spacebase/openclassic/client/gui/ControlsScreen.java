package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.StateButton;
import ch.spacebase.openclassic.api.render.RenderHelper;

public final class ControlsScreen extends GuiScreen {

	private GuiScreen parent;
	private String title = "Controls";
	private int binding = -1;

	public ControlsScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		List<String> keys = new ArrayList<String>();
		keys.addAll(OpenClassic.getClient().getConfig().getKeys("keys"));
		for(int count = 0; count < keys.size(); count++) {
			this.attachWidget(new StateButton(count, this.getWidth() / 2 - 155 + count % 2 * 160, this.getHeight() / 6 + 24 * (count >> 1), 155, 20, this, this.getName("keys." + keys.get(count))));
			this.getWidget(count, StateButton.class).setState(Keyboard.getKeyName(OpenClassic.getClient().getConfig().getInteger("keys." + keys.get(count))));
		}

		this.attachWidget(new Button(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, this, OpenClassic.getGame().getTranslator().translate("gui.done")));
	}
	
	private String getName(String path) {
		if(path.equals("keys.playerlist")) return "Show Playerlist";
		if(path.equals("keys.forward")) return "Forward";
		if(path.equals("keys.back")) return "Backward";
		if(path.equals("keys.left")) return "Left";
		if(path.equals("keys.right")) return "Right";
		if(path.equals("keys.jump")) return "Jump";
		if(path.equals("keys.select-block")) return "Select Block";
		if(path.equals("keys.chat")) return "Chat";
		return path;
	}
	
	private String getPath(String name) {
		if(name.equals("Show Playerlist")) return "keys.playerlist";
		if(name.equals("Forward")) return "keys.forward";
		if(name.equals("Backward")) return "keys.back";
		if(name.equals("Left")) return "keys.left";
		if(name.equals("Right")) return "keys.right";
		if(name.equals("Jump")) return "keys.jump";
		if(name.equals("Select Block")) return "keys.select-block";
		if(name.equals("Chat")) return "keys.chat";
		return name;
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 200) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		} else {
			this.binding = button.getId();
			button.setText("> " + button.getText());
			this.getWidget(this.binding, StateButton.class).setState(Keyboard.getKeyName(OpenClassic.getClient().getConfig().getInteger(this.getPath(button.getText().replace("> ", "")))) + " <");
		}
	}

	public void onKeyPress(char c, int key) {
		if(this.binding >= 0) {
			this.getWidget(this.binding, Button.class).setText(this.getWidget(this.binding, Button.class).getText().replace("> ", ""));
			OpenClassic.getClient().getConfig().setValue(this.getPath(this.getWidget(this.binding, Button.class).getText()), key);
			this.getWidget(this.binding, StateButton.class).setState(Keyboard.getKeyName(OpenClassic.getClient().getConfig().getInteger(this.getPath(this.getWidget(this.binding, Button.class).getText()))));
			this.binding = -1;
			
			OpenClassic.getClient().getConfig().save();
		} else {
			super.onKeyPress(c, key);
		}
	}

	public void render() {
		if(OpenClassic.getClient().isInGame()) {
			RenderHelper.getHelper().color(0, 0, this.getWidth(), this.getHeight(), 1610941696, -1607454624);
		} else {
			RenderHelper.getHelper().drawDefaultBG();
		}
		
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 20);
		super.render();
	}
}
