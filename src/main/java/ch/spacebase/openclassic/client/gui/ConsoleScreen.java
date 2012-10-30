package ch.spacebase.openclassic.client.gui;

import org.lwjgl.input.Keyboard;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.server.ui.EmbeddedConsoleManager;

public class ConsoleScreen extends GuiScreen {

	private static final ConsoleScreen screen = new ConsoleScreen();
	
	public static ConsoleScreen get() {
		return screen;
	}
	
	private EmbeddedConsoleManager handler;
	private GuiScreen parent;
	
	public void setHandler(EmbeddedConsoleManager handler) {
		this.handler = handler;
	}
	
	public void setParent(GuiScreen parent) {
		this.parent = parent;
	}
	
	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new TextBox(0, 4, this.getHeight() - 44, this.getWidth() - 116, 40, this));
		this.attachWidget(new Button(1, this.getWidth() - 108, this.getHeight() - 44, 108, 40, this, OpenClassic.getGame().getTranslator().translate("gui.back")));
		this.getWidget(0, TextBox.class).setFocus(true);
	}
	
	public void update() {
		if(this.handler == null) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
			this.setParent(null);
		}
	}
	
	public final void onButtonClick(Button button) {
		if (button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
			this.setParent(null);
		}
	}

	public void onKeyPress(char c, int key) {
		if (key == Keyboard.KEY_RETURN) {
			String message = this.getWidget(0, TextBox.class).getText().trim();
			if (message.length() > 0) {
				this.handler.send(message);
				this.getWidget(0, TextBox.class).setText("");
			}
		}
		
		super.onKeyPress(c, key);
	}
	
	@Override
	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().drawBox(2, 2, this.getWidth() - 2, this.getHeight() - 50, -6250336);
		RenderHelper.getHelper().drawBox(4, 4, this.getWidth() - 4, this.getHeight() - 52, -16777216);
		
		if(this.handler != null) {
			int count = 0;
			for (String line : this.handler.getLog()) {
				RenderHelper.getHelper().renderText(line, 8, 8 + count * 18, false);
				count++;
			}
		}
		
		super.render();
	}
	
}
