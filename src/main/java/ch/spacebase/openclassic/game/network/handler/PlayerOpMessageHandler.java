package ch.spacebase.openclassic.game.network.handler;

import ch.spacebase.openclassic.api.network.msg.PlayerOpMessage;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;

public class PlayerOpMessageHandler extends MessageHandler<PlayerOpMessage> {

	@Override
	public void handleClient(ClientSession session, ClientPlayer player, PlayerOpMessage message) {
		player.setOp(message.getOp() == Constants.OP);
	}

}
