package ch.spacebase.openclassic.game.network.handler;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.LevelFinalizeMessage;
import ch.spacebase.openclassic.api.player.Session.State;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;

public class LevelFinalizeMessageHandler extends MessageHandler<LevelFinalizeMessage> {

	@Override
	public void handleClient(ClientSession session, ClientPlayer player, LevelFinalizeMessage message) {
		ClientLevel level = session.finalizeLevel(message.getWidth(), message.getHeight(), message.getDepth());
		session.getMode().setLevel(level);
		OpenClassic.getClient().getPlayer().getPosition().setLevel(level);
		OpenClassic.getClient().getProgressBar().setVisible(false);
		session.setState(State.GAME);
	}

}
