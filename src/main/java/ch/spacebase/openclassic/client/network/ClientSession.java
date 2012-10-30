package ch.spacebase.openclassic.client.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.event.player.PlayerConnectEvent;
import ch.spacebase.openclassic.api.event.player.PlayerConnectEvent.Result;
import ch.spacebase.openclassic.api.network.msg.IdentificationMessage;
import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.gui.ErrorScreen;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.mode.Multiplayer;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.util.ServerFlags;
import ch.spacebase.openclassic.client.util.Storage;
import ch.spacebase.openclassic.game.network.HandlerLookupService;
import ch.spacebase.openclassic.game.network.handler.MessageHandler;

public class ClientSession implements Session {
	
	private ClientBootstrap bootstrap = new ClientBootstrap();
	private ChannelGroup group = new DefaultChannelGroup("ClientSession");
	private Channel channel;
	private final Queue<Message> messageQueue = new ArrayDeque<Message>();
	private State state = State.IDENTIFYING;
	
	private ServerFlags flags = new ServerFlags();
	private Multiplayer mode;
	private ByteArrayOutputStream leveldata;

	public ClientSession(Multiplayer mode, String username, String key, String host, int port) {
		this.mode = mode;
		
		ExecutorService boss = Executors.newCachedThreadPool();
		ExecutorService worker = Executors.newCachedThreadPool();
		ChannelFactory factory = new NioClientSocketChannelFactory(boss, worker);
		this.bootstrap.setFactory(factory);
		this.bootstrap.setPipelineFactory(new ClassicPipelineFactory(this));
		this.bootstrap.setOption("connectTimeoutMillis", 40000);
		Channel channel = this.bootstrap.connect(new InetSocketAddress(host, port)).awaitUninterruptibly().getChannel();
		if(channel != null) {
			this.channel = channel;
			this.group.add(channel);
			
			PlayerConnectEvent event = OpenClassic.getGame().getEventManager().dispatch(new PlayerConnectEvent(username, this.getAddress()));
			if(event.getResult() != Result.ALLOWED) {
				this.disconnect(String.format(OpenClassic.getGame().getTranslator().translate("disconnect.plugin-disallow"), event.getKickMessage()));
				return;
			}
			
			this.send(new IdentificationMessage(Constants.PROTOCOL_VERSION, username, key, Constants.OPENCLASSIC_PROTOCOL_VERSION));
		}
	}
	
	public void acceptLevel() {
		if(this.leveldata != null) {
			try {
				this.leveldata.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		this.leveldata = new ByteArrayOutputStream();
	}
	
	public void writeToLevel(short length, byte data[]) {
		if(this.leveldata == null) return;
		this.leveldata.write(data, 0, length);
	}
	
	public ClientLevel finalizeLevel(short width, short height, short depth) {
		if(this.leveldata == null) return this.mode.getLevel();
		
		/* TODO: rework for inf worlds
		try {
			byte blocks[] = this.decompressLevel();
			this.leveldata = null;
			
			ClientLevel level = new ClientLevel("serverlevel", false);
			level.setWorldData(width, height, depth, blocks);
			return level;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} */
		
		return null;
	}
	
	/* private byte[] decompressLevel() throws IOException {
		DataInputStream in = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(this.leveldata.toByteArray())));
		byte[] data = new byte[in.readInt()];
		in.readFully(data);
		in.close();
		return data;
	} */
	
	public Multiplayer getMode() {
		return this.mode;
	}
	
	public ServerFlags getFlags() {
		return this.flags;
	}

	public State getState() {
		return this.state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Player getPlayer() {
		return OpenClassic.getClient().getPlayer();
	}

	@SuppressWarnings("unchecked")
	public void update() {
		Message message;
		
		while ((message = this.messageQueue.poll()) != null) {
			MessageHandler<Message> handler = (MessageHandler<Message>) HandlerLookupService.find(message.getClass());
			
			if (handler != null) {
				handler.handleClient(this, (ClientPlayer) OpenClassic.getClient().getPlayer(), message);
			}
		}
	}

	public void send(Message message) {
		if(message.getClass().getPackage().getName().contains("custom") && !OpenClassic.getClient().isConnectedToOpenClassic()) return;
		this.channel.write(message);
	}

	public void disconnect(String reason) {
		if(reason != null) {
			System.out.println("Disconnected: " + reason);
			OpenClassic.getClient().setCurrentScreen(new ErrorScreen("Disconnected!", reason));
		}
		
		this.channel.close();
		this.group.remove(this.channel);
		this.bootstrap.releaseExternalResources();
		
		((ClassicClient) OpenClassic.getClient()).setOpenClassicServer(false, "");
		if(this.state != State.IDENTIFYING) {
			for(BlockType block : Blocks.getBlocks()) {
				if(block != null && !VanillaBlock.is(block)) {
					Blocks.unregister(block.getId());
				}
			}
		}
		
		for(BlockType block : Storage.getClientBlocks()) {
			Blocks.register(block);
		}
		
		Storage.getClientBlocks().clear();
		if(((ClassicClient) OpenClassic.getClient()).getMode() != null) {
			((ClassicClient) OpenClassic.getClient()).setMode(null);
		}
	}
	
	public boolean isConnected() {
		return this.channel != null && this.channel.isConnected();
	}
	
	public SocketAddress getAddress() {
		return this.channel.getRemoteAddress();
	}

	@Override
	public String toString() {
		return "ClientSession{address=" + this.channel.getRemoteAddress() + "}";
	}

	public <T extends Message> void messageReceived(T message) {
		this.messageQueue.add(message);
	}
	
}
