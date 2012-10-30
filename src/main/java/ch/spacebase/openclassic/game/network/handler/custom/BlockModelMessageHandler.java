package ch.spacebase.openclassic.game.network.handler.custom;

import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.network.msg.custom.block.BlockModelMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.network.handler.MessageHandler;

public class BlockModelMessageHandler extends MessageHandler<BlockModelMessage> {

	// TODO: adjust for data
	@Override
	public void handleClient(ClientSession session, final ClientPlayer player, BlockModelMessage message) {
		if(session == null || player == null) return;
		if(Blocks.get(message.getBlock()) == null) return;
		Blocks.get(message.getBlock()).setModel(message.getModel());
	}

}
