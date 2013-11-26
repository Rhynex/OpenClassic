package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.api.input.Keyboard;
import ch.spacebase.openclassic.api.player.Player;

public class ChatInputScreen extends GuiScreen {

	@Override
	public void onOpen(Player viewer) {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newTextBox(0, 2, this.getHeight() - 14, this.getWidth() - 4, 12, this, true));
		this.getWidget(0, TextBox.class).setFocus(true);
	}

	@Override
	public void onKeyPress(char c, int key) {
		if(key == Keyboard.KEY_RETURN) {
			String message = this.getWidget(0, TextBox.class).getText().trim();
			if(message.length() > 0) {
				if(OpenClassic.getClient().isInMultiplayer()) {
					OpenClassic.getClient().getPlayer().chat(message);
				} else if(message.startsWith("/")) {
					OpenClassic.getClient().processCommand(OpenClassic.getClient().getPlayer(), message.substring(1));
				}
			}

			OpenClassic.getClient().setCurrentScreen(null);
		}

		super.onKeyPress(c, key);
	}

	@Override
	public void onMouseClick(int x, int y, int button) {
		TextBox text = this.getWidget(0, TextBox.class);
		String clickedPlayer = OpenClassic.getClient().getMainScreen().getHoveredPlayer();
		if(button == 0 && clickedPlayer != null) {
			if(text.getText().length() > 0 && !text.getText().endsWith(" ")) {
				text.setText(text.getText() + " ");
			}

			text.setText(text.getText() + clickedPlayer);
			int length = OpenClassic.getClient().getPlayer().getName().length();
			if(text.getText().length() > 62 - length) {
				text.setText(text.getText().substring(0, 62 - length));
			}
		}

		super.onMouseClick(x, y, button);
	}
	
}
