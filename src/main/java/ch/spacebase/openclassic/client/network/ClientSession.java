package ch.spacebase.openclassic.client.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.event.player.PlayerConnectEvent;
import ch.spacebase.openclassic.api.event.player.PlayerConnectEvent.Result;
import ch.spacebase.openclassic.api.network.msg.IdentificationMessage;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.network.ClassicSession;

import com.mojang.minecraft.gui.ErrorScreen;
import com.mojang.minecraft.level.LevelIO;
import com.zachsthings.onevent.EventManager;

public class ClientSession extends ClassicSession {

	private ClientBootstrap bootstrap = new ClientBootstrap();
	private ChannelGroup group = new DefaultChannelGroup("ClientSession");
	private boolean successful = false;
	private boolean disconnected = false;

	private ByteArrayOutputStream levelData = null;

	public ClientSession(final ClientPlayer player, final String sessionId, final String host, final int port) {
		super(new ClientHandlerLookup());
		this.setPlayer(player);
		OpenClassic.getClient().getScheduler().scheduleAsyncTask(this, new Runnable() {
			@Override
			public void run() {
				try {
					ExecutorService boss = Executors.newCachedThreadPool();
					ExecutorService worker = Executors.newCachedThreadPool();
					ChannelFactory factory = new NioClientSocketChannelFactory(boss, worker);
					bootstrap.setFactory(factory);
					bootstrap.setPipelineFactory(new ClientPipelineFactory(ClientSession.this));
					bootstrap.setOption("connectTimeoutMillis", 40000);
					Channel channel = bootstrap.connect(new InetSocketAddress(host, port)).awaitUninterruptibly().getChannel();
					if(channel != null) {
						ClientSession.this.channel = channel;
						group.add(channel);

						PlayerConnectEvent event = EventManager.callEvent(new PlayerConnectEvent(player.getName(), getAddress()));
						if(event.getResult() != Result.ALLOWED) {
							disconnect(String.format(OpenClassic.getGame().getTranslator().translate("disconnect.plugin-disallow"), event.getKickMessage()));
							return;
						}

						send(new IdentificationMessage(Constants.PROTOCOL_VERSION, player.getName(), sessionId, Constants.OPENCLASSIC_PROTOCOL_VERSION));
						successful = true;
					} else {
						OpenClassic.getClient().getProgressBar().setVisible(false);
					}
				} catch(Exception e) {
					OpenClassic.getClient().getProgressBar().setVisible(false);
					OpenClassic.getClient().setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("connecting.fail-connect"), OpenClassic.getGame().getTranslator().translate("connecting.probably-down")));
					e.printStackTrace();
					successful = false;
				}
			}
		});
	}

	@Override
	public boolean sendCustomMessages() {
		return OpenClassic.getClient().isConnectedToOpenClassic();
	}

	@Override
	public void disconnect(String reason) {
		this.disconnected = true;
		if(reason != null) {
			System.out.println("Disconnected: " + reason);
			OpenClassic.getClient().setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("disconnect.generic"), reason));
		}

		if(this.isConnected()) {
			this.channel.close();
			this.group.remove(this.channel);
		}

		this.dispose();
	}

	@Override
	public void dispose() {
		if(this.bootstrap != null) {
			this.bootstrap.releaseExternalResources();
		}

		this.channel = null;
		this.group = null;
		this.bootstrap = null;
	}

	public boolean connectSuccess() {
		return this.isConnected() && this.successful;
	}

	public boolean isDisconnected() {
		return this.disconnected;
	}

	public void prepareForLevel() {
		this.levelData = new ByteArrayOutputStream();
	}

	public void appendLevelData(byte data[], short length) {
		if(this.levelData == null) {
			return;
		}

		this.levelData.write(data, 0, length);
	}

	public byte[] finishLevel() {
		if(this.levelData == null) {
			return new byte[0];
		}

		IOUtils.closeQuietly(this.levelData);
		byte processed[] = LevelIO.processData(new ByteArrayInputStream(this.levelData.toByteArray()));
		this.levelData = null;
		return processed;
	}

}
