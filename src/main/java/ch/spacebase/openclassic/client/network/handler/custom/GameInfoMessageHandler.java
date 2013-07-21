package ch.spacebase.openclassic.client.network.handler.custom;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.custom.GameInfoMessage;
import ch.spacebase.openclassic.api.network.msg.custom.PluginMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.plugin.Plugin;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

public class GameInfoMessageHandler extends MessageHandler<GameInfoMessage> {

	@Override
	public void handle(ClassicSession session, final Player player, GameInfoMessage message) {
		OpenClassic.getLogger().info("Connected to OpenClassic v" + message.getVersion() + "!");
		GeneralUtils.getMinecraft().openclassicServer = true;
		GeneralUtils.getMinecraft().openclassicVersion = message.getVersion();
		session.send(new GameInfoMessage(Constants.VERSION, OpenClassic.getGame().getLanguage()));
		for(Plugin plugin : OpenClassic.getClient().getPluginManager().getPlugins()) {
			session.send(new PluginMessage(plugin.getDescription().getName(), plugin.getDescription().getVersion()));
		}
	}

}
