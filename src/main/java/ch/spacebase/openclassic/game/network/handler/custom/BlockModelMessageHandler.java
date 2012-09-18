package ch.spacebase.openclassic.game.network.handler.custom;

import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.custom.CustomBlock;
import ch.spacebase.openclassic.api.network.msg.custom.block.BlockModelMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.network.handler.MessageHandler;

public class BlockModelMessageHandler extends MessageHandler<BlockModelMessage> {

	@Override
	public void handleClient(ClientSession session, final ClientPlayer player, BlockModelMessage message) {
		if(session == null || player == null) return;
		if(Blocks.fromId(message.getBlock()) == null) return;
		((CustomBlock) Blocks.fromId(message.getBlock())).setModel(message.getModel());
	}

}
