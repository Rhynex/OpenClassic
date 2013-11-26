package ch.spacebase.openclassic.client.network.handler;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;
import ch.spacebase.openclassic.game.network.msg.LevelInitializeMessage;

public class LevelInitializeMessageHandler extends MessageHandler<LevelInitializeMessage> {

	@Override
	public void handle(ClassicSession session, Player player, LevelInitializeMessage message) {
		if(!OpenClassic.getClient().isConnectedToOpenClassic()) {
			VanillaBlock.registerAll();
		}

		GeneralUtils.getMinecraft().setLevel(null);
		OpenClassic.getClient().setCurrentScreen(null);
		((ClientSession) session).prepareForLevel();
	}

}
