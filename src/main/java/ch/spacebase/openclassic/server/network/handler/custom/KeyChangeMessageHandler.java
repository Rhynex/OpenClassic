package ch.spacebase.openclassic.server.network.handler.custom;

import ch.spacebase.openclassic.api.event.player.PlayerKeyChangeEvent;
import ch.spacebase.openclassic.api.network.msg.custom.KeyChangeMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

import com.zachsthings.onevent.EventManager;

public class KeyChangeMessageHandler extends MessageHandler<KeyChangeMessage> {

	@Override
	public void handle(ClassicSession session, Player player, KeyChangeMessage message) {
		EventManager.callEvent(new PlayerKeyChangeEvent(player, message.getKey(), message.isPressed()));

		// TODO: Add handling if player has gui open
	}

}
