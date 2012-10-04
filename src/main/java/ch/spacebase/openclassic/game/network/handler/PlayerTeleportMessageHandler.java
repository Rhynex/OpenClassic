package ch.spacebase.openclassic.game.network.handler;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.event.player.PlayerMoveEvent;
import ch.spacebase.openclassic.api.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session.State;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.server.network.ServerSession;
import ch.spacebase.openclassic.server.player.ServerPlayer;

public class PlayerTeleportMessageHandler extends MessageHandler<PlayerTeleportMessage> {

	@Override
	public void handleClient(ClientSession session, ClientPlayer player, PlayerTeleportMessage message) {
		if(player == null || session == null) return;
		if(session.getState() != State.GAME) return;
		
		if(message.getPlayerId() < 0) {
			player.moveTo((float) message.getX(), (float) message.getY() - Constants.FOOT_EYE_DISTANCE, (float) message.getZ(), message.getYaw(), message.getPitch());
		} else {
			for(Player p : player.getPosition().getLevel().getPlayers()) {
				if(p.getPlayerId() == message.getPlayerId()) {
					p.moveTo((float) message.getX(), (float) message.getY(), (float) message.getZ(), message.getYaw(), message.getPitch());
				}
			}
		}
	}
	
	@Override
	public void handleServer(ServerSession session, ServerPlayer player, PlayerTeleportMessage message) {
		if(player == null || session == null) return;
		if(session.getState() != State.GAME) return;
		
		Position to = new Position(player.getPosition().getLevel(), (float) message.getX(), (float) message.getY(), (float) message.getZ(), message.getYaw(), message.getPitch());
		/* TODO: detect teleports properly if(!player.teleported && (to.getX() - player.getPosition().getX() >= 3 || to.getX() - player.getPosition().getX() <= -3 || to.getZ() - player.getPosition().getZ() >= 3 || to.getZ() - player.getPosition().getZ() <= -3)) {
			PlayerRespawnEvent event = EventFactory.callEvent(new PlayerRespawnEvent(player, to));
			if(event.isCancelled()) {
				session.send(new PlayerTeleportMessage((byte) -1, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), player.getPosition().getYaw(), player.getPosition().getPitch()));
				return;
			}
			
			to = event.getPosition();
		} */
		
		player.teleported = false;
		PlayerMoveEvent event = OpenClassic.getGame().getEventManager().dispatch(new PlayerMoveEvent(player, player.getPosition(), to));
		Position old = to;
		to = event.getTo();
		
		if(event.isCancelled()) {
			session.send(new PlayerTeleportMessage((byte) -1, event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ(), (byte) event.getFrom().getYaw(), (byte) event.getFrom().getPitch()));
			return;
		}
		
		if(!to.equals(old)) {
			session.send(new PlayerTeleportMessage((byte) -1, to.getX(), to.getY(), to.getZ(), (byte) to.getYaw(), (byte) to.getPitch()));
		}
		
		player.getPosition().setX(to.getX());
		player.getPosition().setY(to.getY());
		player.getPosition().setZ(to.getZ());
		player.getPosition().setYaw(to.getYaw());
		player.getPosition().setPitch(to.getPitch());
		
		player.getPosition().getLevel().sendToAllExcept(player, new PlayerTeleportMessage(player.getPlayerId(), to.getX(), to.getY() + 0.59375, to.getZ(), (byte) to.getYaw(), (byte) to.getPitch()));
	}

}
