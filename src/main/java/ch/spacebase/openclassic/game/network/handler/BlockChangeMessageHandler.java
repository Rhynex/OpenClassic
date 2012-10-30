package ch.spacebase.openclassic.game.network.handler;

import ch.spacebase.openclassic.api.network.msg.BlockChangeMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;

public class BlockChangeMessageHandler extends MessageHandler<BlockChangeMessage> {

	@Override
	public void handleClient(ClientSession session, ClientPlayer player, BlockChangeMessage message) {
		if(session.getMode().getLevel() != null) {
			// TODO: adjust for data session.getMode().getLevel().setBlockIdAt(message.getX(), message.getY(), message.getZ(), message.getBlock(), false);
		}
	}

}
