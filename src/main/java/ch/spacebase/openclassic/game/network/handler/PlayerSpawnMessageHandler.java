package ch.spacebase.openclassic.game.network.handler;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.network.msg.PlayerSpawnMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.player.OtherPlayer;

public class PlayerSpawnMessageHandler extends MessageHandler<PlayerSpawnMessage> {

	@Override
	public void handleClient(ClientSession session, ClientPlayer player, PlayerSpawnMessage message) {
		Position pos = new Position(player.getPosition().getLevel(), (float) message.getX(), (float) message.getY(), (float) message.getZ(), message.getYaw(), message.getPitch());
		if(message.getPlayerId() < 0) {
			player.getPosition().getLevel().setSpawn(pos.clone());
			player.moveTo(pos);
		} else {
			player.getPosition().getLevel().addPlayer(new OtherPlayer(message.getPlayerId(), message.getName(), pos));
		}
	}

}
