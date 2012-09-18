package ch.spacebase.openclassic.game.network.handler.custom;

import ch.spacebase.openclassic.api.network.msg.custom.LevelColorMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.network.handler.MessageHandler;

public class LevelColorMessageHandler extends MessageHandler<LevelColorMessage> {

	@Override
	public void handleClient(ClientSession session, final ClientPlayer player, LevelColorMessage message) {
		if(session == null || player == null) return;
		if(message.getType().equals("sky")) {
			player.getPosition().getLevel().setSkyColor(message.getValue());
		} else if (message.getType().equals("fog")) {
			player.getPosition().getLevel().setFogColor(message.getValue());
		} else if (message.getType().equals("cloud")) {
			player.getPosition().getLevel().setCloudColor(message.getValue());
		}
	}

}
