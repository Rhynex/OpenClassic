package ch.spacebase.openclassic.client.network.handler.custom;

import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;
import ch.spacebase.openclassic.game.network.msg.custom.PluginMessage;

public class PluginMessageHandler extends MessageHandler<PluginMessage> {

	@Override
	public void handle(ClassicSession session, Player player, PluginMessage message) {
		GeneralUtils.getMinecraft().serverPlugins.add(new RemotePluginInfo(message.getName(), message.getVersion()));
	}

}
