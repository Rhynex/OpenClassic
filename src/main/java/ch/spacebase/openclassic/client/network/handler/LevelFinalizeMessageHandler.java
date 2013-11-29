package ch.spacebase.openclassic.client.network.handler;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.ClassicSession.State;
import ch.spacebase.openclassic.game.network.msg.LevelFinalizeMessage;
import ch.spacebase.openclassic.game.network.MessageHandler;

public class LevelFinalizeMessageHandler extends MessageHandler<LevelFinalizeMessage> {

	@Override
	public void handle(ClassicSession session, Player player, LevelFinalizeMessage message) {
		byte data[] = ((ClientSession) session).finishLevel();
		ClientLevel level = new ClientLevel();
		level.setName("ServerLevel");
		level.setAuthor(GeneralUtils.getMinecraft().server);
		level.setCreationTime(System.currentTimeMillis());
		level.setData(message.getWidth(), message.getHeight(), message.getDepth(), data);
		GeneralUtils.getMinecraft().setLevel(level);
		session.setState(State.GAME);
		OpenClassic.getClient().getProgressBar().setSubtitleScaled(true);
		OpenClassic.getClient().getProgressBar().setVisible(false);
	}

}
