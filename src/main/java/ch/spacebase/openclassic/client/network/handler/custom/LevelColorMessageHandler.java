package ch.spacebase.openclassic.client.network.handler.custom;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.custom.LevelColorMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

public class LevelColorMessageHandler extends MessageHandler<LevelColorMessage> {

	@Override
	public void handle(ClassicSession session, Player player, LevelColorMessage message) {
		if (message.getType().equals("sky")) {
			OpenClassic.getClient().getLevel().setSkyColor(message.getValue());
		} else if (message.getType().equals("fog")) {
			OpenClassic.getClient().getLevel().setFogColor(message.getValue());
		} else if (message.getType().equals("cloud")) {
			OpenClassic.getClient().getLevel().setCloudColor(message.getValue());
		}
	}

}
