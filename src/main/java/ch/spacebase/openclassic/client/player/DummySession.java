package ch.spacebase.openclassic.client.player;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.msg.Message;

public class DummySession extends ClassicSession {

	private ClientPlayer player;

	public DummySession(ClientPlayer player) {
		super(null);
		this.player = player;
	}

	@Override
	public State getState() {
		return State.GAME;
	}

	@Override
	public Player getPlayer() {
		return this.player;
	}

	@Override
	public void send(Message message) {
	}

	@Override
	public void disconnect(String reason) {
	}

	@Override
	public SocketAddress getAddress() {
		return InetSocketAddress.createUnresolved("localhost", 25565);
	}

	@Override
	public boolean sendCustomMessages() {
		return false;
	}

}
