package ch.spacebase.openclassic.game.network.handler.custom;

import ch.spacebase.openclassic.api.network.msg.custom.PluginMessage;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.network.handler.MessageHandler;
import ch.spacebase.openclassic.server.network.ServerSession;
import ch.spacebase.openclassic.server.player.ServerPlayer;

public class PluginMessageHandler extends MessageHandler<PluginMessage> {

	@Override
	public void handleClient(ClientSession session, ClientPlayer player, PluginMessage message) {
		if(session == null || player == null) return;
		session.getMode().addServerPlugin(new RemotePluginInfo(message.getName(), message.getVersion()));
	}
	
	@Override
	public void handleServer(ServerSession session, ServerPlayer player, PluginMessage message) {
		if(session == null || player == null) return;
		player.getClientInfo().addPlugin(new RemotePluginInfo(message.getName(), message.getVersion()));
	}

}
