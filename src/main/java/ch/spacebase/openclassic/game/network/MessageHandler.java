package ch.spacebase.openclassic.game.network;

import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.network.msg.Message;

public abstract class MessageHandler<T extends Message> {

	public abstract void handle(ClassicSession session, Player player, T message);

}
