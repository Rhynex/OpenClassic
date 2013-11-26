package ch.spacebase.openclassic.server.network.handler.custom;

import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;
import ch.spacebase.openclassic.game.network.msg.custom.GameInfoMessage;
import ch.spacebase.openclassic.server.player.ServerPlayer;

public class GameInfoMessageHandler extends MessageHandler<GameInfoMessage> {

	@Override
	public void handle(ClassicSession session, final Player player, GameInfoMessage message) {
		((ServerPlayer) player).getClientInfo().setVersion(message.getVersion());
		((ServerPlayer) player).getClientInfo().setLanguage(message.getLanguage());
	}

}
