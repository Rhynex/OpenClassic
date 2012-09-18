package ch.spacebase.openclassic.game.network.handler;

import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.server.network.ServerSession;
import ch.spacebase.openclassic.server.player.ServerPlayer;

public abstract class MessageHandler<T extends Message> {

	public void handleClient(ClientSession session, ClientPlayer player, T message) {
	}
	
	public void handleServer(ServerSession session, ServerPlayer player, T message) {
	}
	
}
