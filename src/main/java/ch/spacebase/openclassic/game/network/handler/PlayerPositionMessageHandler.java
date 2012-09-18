package ch.spacebase.openclassic.game.network.handler;


import ch.spacebase.openclassic.api.network.msg.PlayerPositionMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session.State;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.player.OtherPlayer;

public class PlayerPositionMessageHandler extends MessageHandler<PlayerPositionMessage> {

	@Override
	public void handleClient(ClientSession session, ClientPlayer player, PlayerPositionMessage message) {
		if(player == null || session == null) return;
		if(session.getState() != State.GAME) return;
		
		for(Player p : player.getPosition().getLevel().getPlayers()) {
			if(p.getPlayerId() == message.getPlayerId()) {
				((OtherPlayer) p).move((float) message.getXChange(), (float) message.getYChange(), (float) message.getZChange());
			}
		}
	}

}
