package ch.spacebase.openclassic.client.network.handler;

import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;
import ch.spacebase.openclassic.game.network.msg.PlayerOpMessage;
import ch.spacebase.openclassic.game.util.InternalConstants;

public class PlayerOpMessageHandler extends MessageHandler<PlayerOpMessage> {

	@Override
	public void handle(ClassicSession session, Player player, PlayerOpMessage message) {
		if(message.getOp() == InternalConstants.OP) {
			player.setCanBreakBedrock(true);
		} else if(message.getOp() == InternalConstants.NOT_OP) {
			player.setCanBreakBedrock(false);
		}
	}

}
