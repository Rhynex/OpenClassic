package ch.spacebase.openclassic.client.network.handler;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.network.msg.PlayerChatMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

public class PlayerChatMessageHandler extends MessageHandler<PlayerChatMessage> {

	@Override
	public void handle(ClassicSession session, Player player, PlayerChatMessage message) {
		if(message.getPlayerId() < 0) {
			player.sendMessage(Color.YELLOW + message.getMessage());
		} else {
			player.sendMessage(message.getMessage());
		}
	}

}
