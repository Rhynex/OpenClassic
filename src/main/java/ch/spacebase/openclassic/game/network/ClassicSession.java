package ch.spacebase.openclassic.game.network;

import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;

import org.jboss.netty.channel.Channel;
import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session;

public abstract class ClassicSession implements Session {

	protected Channel channel;
	private final Queue<Message> messageQueue = new ArrayDeque<Message>();
	private State state = State.IDENTIFYING;
	private Player player;
	private HandlerLookup lookup;

	public ClassicSession(HandlerLookup lookup) {
		this.lookup = lookup;
	}

	public State getState() {
		return this.state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Player getPlayer() {
		return this.player;
	}
	
	public void setPlayer(Player player) {
		if (this.player != null) {
			throw new IllegalStateException();
		}

		this.player = player;
	}

	@SuppressWarnings("unchecked")
	public boolean tick() {
		Message message;
		while ((message = this.messageQueue.poll()) != null) {
			MessageHandler<Message> handler = (MessageHandler<Message>) this.lookup.find(message.getClass());
			if (handler != null) {
				handler.handle(this, this.player, message);
			}
		}

		return true;
	}

	public void send(Message message) {
		if(!this.isConnected()) {
			return;
		}
		
		if(message.getClass().getPackage().getName().contains("custom") && !this.sendCustomMessages()) {
			return;
		}
		
		this.channel.write(message);
	}
	
	public abstract boolean sendCustomMessages();

	public boolean isConnected() {
		return this.channel != null && this.channel.isOpen();
	}
	
	public void disconnect(String reason) {
		this.channel.close();
	}
	
	public SocketAddress getAddress() {
		return this.channel.getRemoteAddress();
	}

	@Override
	public String toString() {
		return this.getClass().getName() + " [address=" + this.channel.getRemoteAddress() + "]";
	}

	public <T extends Message> void messageReceived(T message) {
		this.messageQueue.add(message);
	}

	public void dispose() {
		if (this.player != null) {
			this.player = null;
		}
	}
	
}
