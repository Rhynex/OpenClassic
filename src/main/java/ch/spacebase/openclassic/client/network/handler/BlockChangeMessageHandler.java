package ch.spacebase.openclassic.client.network.handler;

import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;
import ch.spacebase.openclassic.game.network.msg.BlockChangeMessage;

public class BlockChangeMessageHandler extends MessageHandler<BlockChangeMessage> {

	@Override
	public void handle(ClassicSession session, Player player, BlockChangeMessage message) {
		if(GeneralUtils.getMinecraft().level != null) {
			GeneralUtils.getMinecraft().level.setBlockAt(message.getX(), message.getY(), message.getZ(), Blocks.fromId(message.getBlock()));
		}
	}

}
