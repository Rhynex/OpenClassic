package ch.spacebase.openclassic.game.network.handler.custom;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.custom.GameInfoMessage;
import ch.spacebase.openclassic.api.network.msg.custom.PluginMessage;
import ch.spacebase.openclassic.api.plugin.Plugin;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.network.handler.MessageHandler;
import ch.spacebase.openclassic.server.network.ServerSession;
import ch.spacebase.openclassic.server.player.ServerPlayer;

public class ClientInfoMessageHandler extends MessageHandler<GameInfoMessage> {

	@Override
	public void handleClient(ClientSession session, final ClientPlayer player, GameInfoMessage message) {
		if(session == null || player == null) return;
		((ClassicClient) OpenClassic.getClient()).setOpenClassicServer(true, message.getVersion());
		session.send(new GameInfoMessage(Constants.CLIENT_VERSION, OpenClassic.getGame().getLanguage()));
		for(Plugin plugin : OpenClassic.getClient().getPluginManager().getPlugins()) {
			session.send(new PluginMessage(plugin.getDescription().getName(), plugin.getDescription().getVersion()));
		}
	}
	
	@Override
	public void handleServer(ServerSession session, final ServerPlayer player, GameInfoMessage message) {
		if(session == null || player == null) return;
		player.getClientInfo().setVersion(message.getVersion());
		player.getClientInfo().setLanguage(message.getLanguage());
	}

}
