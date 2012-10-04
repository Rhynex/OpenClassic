package ch.spacebase.openclassic.game.network.handler;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.event.player.PlayerKickEvent;
import ch.spacebase.openclassic.api.network.msg.PlayerDisconnectMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;

public class PlayerDisconnectMessageHandler extends MessageHandler<PlayerDisconnectMessage> {

	@Override
	public void handleClient(ClientSession session, ClientPlayer player, PlayerDisconnectMessage message) {
		OpenClassic.getGame().getEventManager().dispatch(new PlayerKickEvent(OpenClassic.getClient().getPlayer(), message.getMessage(), ""));
		System.out.println(message.getMessage());
		session.disconnect(String.format(OpenClassic.getGame().getTranslator().translate("disconnect.by-server"), message.getMessage()));
	}

}
