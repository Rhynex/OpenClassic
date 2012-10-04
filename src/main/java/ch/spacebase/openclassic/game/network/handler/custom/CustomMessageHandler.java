package ch.spacebase.openclassic.game.network.handler.custom;

import ch.spacebase.openclassic.api.OpenClassic;

import ch.spacebase.openclassic.api.event.player.CustomMessageEvent;
import ch.spacebase.openclassic.api.network.msg.custom.CustomMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.network.handler.MessageHandler;
import ch.spacebase.openclassic.server.network.ServerSession;
import ch.spacebase.openclassic.server.player.ServerPlayer;

public class CustomMessageHandler extends MessageHandler<CustomMessage> {

	@Override
	public void handleClient(ClientSession session, ClientPlayer player, CustomMessage message) {
		if(session == null || player == null) return;
		OpenClassic.getGame().getEventManager().dispatch(new CustomMessageEvent(player, message));
	}
	
	@Override
	public void handleServer(ServerSession session, ServerPlayer player, CustomMessage message) {
		if(session == null || player == null) return;
		OpenClassic.getGame().getEventManager().dispatch(new CustomMessageEvent(player, message));
	}

}
