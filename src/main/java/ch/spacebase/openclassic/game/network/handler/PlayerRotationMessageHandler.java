package ch.spacebase.openclassic.game.network.handler;


import ch.spacebase.openclassic.api.network.msg.PlayerRotationMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session.State;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.player.OtherPlayer;

public class PlayerRotationMessageHandler extends MessageHandler<PlayerRotationMessage> {

	@Override
	public void handleClient(ClientSession session, ClientPlayer player, PlayerRotationMessage message) {
		if(player == null || session == null) return;
		if(session.getState() != State.GAME) return;
		
		for(Player p : player.getPosition().getLevel().getPlayers()) {
			if(p.getPlayerId() == message.getPlayerId()) {
				((OtherPlayer) p).rotateY(message.getYaw());
				((OtherPlayer) p).rotateX(message.getPitch());
			}
		}
	}

}
