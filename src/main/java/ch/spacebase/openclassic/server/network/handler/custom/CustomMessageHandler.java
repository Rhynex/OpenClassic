package ch.spacebase.openclassic.server.network.handler.custom;

import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.player.CustomMessageEvent;
import ch.spacebase.openclassic.api.network.msg.custom.CustomMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

public class CustomMessageHandler extends MessageHandler<CustomMessage> {

	@Override
	public void handle(ClassicSession session, Player player, CustomMessage message) {
		EventFactory.callEvent(new CustomMessageEvent(player, message));
	}

}
