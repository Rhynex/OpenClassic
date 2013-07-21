package ch.spacebase.openclassic.client.network.handler;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.LevelFinalizeMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session.State;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

import com.mojang.minecraft.level.Level;

public class LevelFinalizeMessageHandler extends MessageHandler<LevelFinalizeMessage> {

	@Override
	public void handle(ClassicSession session, Player player, LevelFinalizeMessage message) {
		byte data[] = ((ClientSession) session).finishLevel();
		Level level = new Level();
		level.name = "A Nice World";
		level.creator = GeneralUtils.getMinecraft().server;
		level.createTime = System.currentTimeMillis();
		level.setNetworkMode(true);
		level.setData(message.getWidth(), message.getHeight(), message.getDepth(), data);
		GeneralUtils.getMinecraft().setLevel(level);
		session.setState(State.GAME);
		OpenClassic.getClient().getProgressBar().setVisible(false);
	}

}
