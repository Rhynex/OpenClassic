package ch.spacebase.openclassic.client.network.handler;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.player.PlayerKickEvent;
import ch.spacebase.openclassic.api.network.msg.PlayerDisconnectMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

public class PlayerDisconnectMessageHandler extends MessageHandler<PlayerDisconnectMessage> {

	@Override
	public void handle(ClassicSession session, Player player, PlayerDisconnectMessage message) {
		PlayerKickEvent event = EventFactory.callEvent(new PlayerKickEvent(OpenClassic.getClient().getPlayer(), message.getMessage(), ""));
		session.disconnect(event.getReason());
	}

}
