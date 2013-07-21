package ch.spacebase.openclassic.server.network.handler.custom;

import ch.spacebase.openclassic.api.network.msg.custom.PluginMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;
import ch.spacebase.openclassic.server.player.ServerPlayer;

public class PluginMessageHandler extends MessageHandler<PluginMessage> {

	@Override
	public void handle(ClassicSession session, Player player, PluginMessage message) {
		((ServerPlayer) player).getClientInfo().addPlugin(new RemotePluginInfo(message.getName(), message.getVersion()));
	}

}
