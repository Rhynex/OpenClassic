package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.event.player.PlayerChatEvent;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.api.input.Keyboard;

import com.zachsthings.onevent.EventManager;

public class ChatInputScreen extends GuiScreen {

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newTextBox(0, 2, this.getHeight() - 14, this.getWidth() - 4, 12, this, true));
		this.getWidget(0, TextBox.class).setFocus(true);
	}

	public void onKeyPress(char c, int key) {
		if(key == Keyboard.KEY_RETURN) {
			String message = this.getWidget(0, TextBox.class).getText().trim();
			if(message.length() > 0) {
				if(OpenClassic.getClient().isInMultiplayer()) {
					PlayerChatEvent event = EventManager.callEvent(new PlayerChatEvent(OpenClassic.getClient().getPlayer(), message));
					if(event.isCancelled()) {
						return;
					}
					
					OpenClassic.getClient().getPlayer().chat(event.getMessage());
				} else if(message.startsWith("/")) {
					OpenClassic.getClient().processCommand(OpenClassic.getClient().getPlayer(), message.substring(1));
				}
			}

			OpenClassic.getClient().setCurrentScreen(null);
		}

		super.onKeyPress(c, key);
	}

	public void onMouseClick(int x, int y, int button) {
		TextBox text = this.getWidget(0, TextBox.class);
		String clickedPlayer = OpenClassic.getClient().getMainScreen().getClickedPlayer();
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
