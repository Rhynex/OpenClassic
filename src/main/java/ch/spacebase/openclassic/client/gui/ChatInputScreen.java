package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.event.player.PlayerChatEvent;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.network.msg.PlayerChatMessage;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.mode.Multiplayer;

import org.lwjgl.input.Keyboard;

public class ChatInputScreen extends GuiScreen {

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new TextBox(0, 2, this.getHeight() - 14, this.getWidth() - 4, 12, this, true));
		this.getWidget(0, TextBox.class).setFocus(true);
	}

	public void onKeyPress(char c, int key) {
		if(key == Keyboard.KEY_RETURN) {
			String message = this.getWidget(0, TextBox.class).getText().trim();
			if(message.length() > 0) {
				if(((ClassicClient) OpenClassic.getClient()).getMode() instanceof Multiplayer) {
					PlayerChatEvent event = OpenClassic.getGame().getEventManager().dispatch(new PlayerChatEvent(OpenClassic.getClient().getPlayer(), message));
					if(event.isCancelled()) return;
					
					((Multiplayer) ((ClassicClient) OpenClassic.getClient()).getMode()).getSession().send(new PlayerChatMessage((byte) -1, event.getMessage()));
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
		if(button == 0 && OpenClassic.getClient().getMainScreen().getHoveredPlayer() != null) {
			if(text.getText().length() > 0 && !text.getText().endsWith(" ")) {
				text.setText(text.getText() + " ");
			}

			text.setText(text.getText() + OpenClassic.getClient().getMainScreen().getHoveredPlayer());
			if(text.getText().length() > 62 - OpenClassic.getClient().getPlayer().getName().length()) {
				text.setText(text.getText().substring(0, 62 - OpenClassic.getClient().getPlayer().getName().length()));
			}
		}

		super.onMouseClick(x, y, button);
	}
	
}
