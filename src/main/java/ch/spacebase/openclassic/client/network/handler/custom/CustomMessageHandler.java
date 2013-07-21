package ch.spacebase.openclassic.client.network.handler.custom;

import ch.spacebase.openclassic.api.event.player.CustomMessageEvent;
import ch.spacebase.openclassic.api.network.msg.custom.CustomMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

import com.zachsthings.onevent.EventManager;

public class CustomMessageHandler extends MessageHandler<CustomMessage> {

	@Override
	public void handle(ClassicSession session, Player player, CustomMessage message) {
		EventManager.callEvent(new CustomMessageEvent(player, message));
	}

}
