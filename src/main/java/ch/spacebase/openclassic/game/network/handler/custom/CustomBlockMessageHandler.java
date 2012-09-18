package ch.spacebase.openclassic.game.network.handler.custom;

import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.network.msg.custom.block.CustomBlockMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.network.handler.MessageHandler;

public class CustomBlockMessageHandler extends MessageHandler<CustomBlockMessage> {

	@Override
	public void handleClient(ClientSession session, final ClientPlayer player, CustomBlockMessage message) {
		if(session == null || player == null) return;
		Blocks.register(message.getBlock());
	}

}
