package ch.spacebase.openclassic.game.network.handler;


import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.LevelDataMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;

public class LevelDataMessageHandler extends MessageHandler<LevelDataMessage> {

	@Override
	public void handleClient(ClientSession session, ClientPlayer player, LevelDataMessage message) {
		OpenClassic.getClient().getProgressBar().setProgress(message.getPercent());
		session.writeToLevel(message.getLength(), message.getData());
	}

}
