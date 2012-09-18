package ch.spacebase.openclassic.game.network.handler;

import ch.spacebase.openclassic.api.network.msg.PlayerDespawnMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;

public class PlayerDespawnMessageHandler extends MessageHandler<PlayerDespawnMessage> {

	@Override
	public void handleClient(ClientSession session, ClientPlayer player, PlayerDespawnMessage message) {
		player.getPosition().getLevel().removePlayer(message.getPlayerId());
	}

}
