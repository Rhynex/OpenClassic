package ch.spacebase.openclassic.game.network.handler;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.LevelInitializeMessage;
import ch.spacebase.openclassic.api.player.Session.State;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;

public class LevelInitializeMessageHandler extends MessageHandler<LevelInitializeMessage> {

	@Override
	public void handleClient(ClientSession session, ClientPlayer player, LevelInitializeMessage message) {
		session.getMode().setLevel(null);
		session.acceptLevel();
		OpenClassic.getClient().getProgressBar().setProgress(0);
		OpenClassic.getClient().getProgressBar().setVisible(true);
		session.setState(State.PREPARING);
	}

}
