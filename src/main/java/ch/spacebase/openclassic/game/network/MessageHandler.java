package ch.spacebase.openclassic.game.network;

import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.api.player.Player;

public abstract class MessageHandler<T extends Message> {

	public abstract void handle(ClassicSession session, Player player, T message);
	
}
