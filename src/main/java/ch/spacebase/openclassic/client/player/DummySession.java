package ch.spacebase.openclassic.client.player;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session;

public class DummySession implements Session {

	private ClientPlayer player;
	
	public DummySession(ClientPlayer player) {
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

}
