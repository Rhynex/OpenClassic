package ch.spacebase.openclassic.client.network.handler;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.LevelDataMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

public class LevelDataMessageHandler extends MessageHandler<LevelDataMessage> {

	@Override
	public void handle(ClassicSession session, Player player, LevelDataMessage message) {
		OpenClassic.getClient().getProgressBar().setProgress(message.getPercent());
		((ClientSession) session).appendLevelData(message.getData(), message.getLength());
	}

}
