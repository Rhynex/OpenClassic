package ch.spacebase.openclassic.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;

import org.apache.commons.io.IOUtils;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Server;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.QuadFactory;
import ch.spacebase.openclassic.api.block.model.TextureFactory;
import ch.spacebase.openclassic.api.command.Console;
import ch.spacebase.openclassic.api.event.level.LevelCreateEvent;
import ch.spacebase.openclassic.api.event.level.LevelLoadEvent;
import ch.spacebase.openclassic.api.event.level.LevelSaveEvent;
import ch.spacebase.openclassic.api.event.level.LevelUnloadEvent;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.level.generator.FlatLandGenerator;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.api.level.generator.NormalGenerator;
import ch.spacebase.openclassic.api.permissions.PermissionManager;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.plugin.Plugin;
import ch.spacebase.openclassic.api.plugin.PluginManager.LoadOrder;
import ch.spacebase.openclassic.api.sound.AudioManager;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.game.ClassicGame;
import ch.spacebase.openclassic.game.io.OpenClassicLevelFormat;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.msg.Message;
import ch.spacebase.openclassic.game.network.msg.PlayerDisconnectMessage;
import ch.spacebase.openclassic.game.scheduler.ClassicScheduler;
import ch.spacebase.openclassic.game.util.InternalConstants;
import ch.spacebase.openclassic.server.command.ServerCommands;
import ch.spacebase.openclassic.server.level.ServerLevel;
import ch.spacebase.openclassic.server.network.ServerPipelineFactory;
import ch.spacebase.openclassic.server.network.SessionRegistry;
import ch.spacebase.openclassic.server.player.ServerPlayer;
import ch.spacebase.openclassic.server.sound.ServerAudioManager;
import ch.spacebase.openclassic.server.ui.ConsoleManager;
import ch.spacebase.openclassic.server.ui.GuiConsoleManager;
import ch.spacebase.openclassic.server.ui.TextConsoleManager;

import com.zachsthings.onevent.EventManager;

// TODO: Server-side GUIs
public class ClassicServer extends ClassicGame implements Server {

	/**
	 * The server's executor service.
	 */
	private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
		private int nextId = 0;

		@Override
		public Thread newThread(Runnable r) {
			this.nextId++;
			return new Thread(r, "Server-" + this.nextId);
		}
	});

	/**
	 * Console manager
	 */
	private ConsoleManager console;

	/**
	 * The {@link ServerBootstrap} used to initialize Netty.
	 */
	private final ServerBootstrap bootstrap = new ServerBootstrap();

	/**
	 * A group containing all of the channels.
	 */
	private final ChannelGroup group = new DefaultChannelGroup();

	/**
	 * The network executor service - Netty dispatches events to this thread
	 * pool.
	 */
	private final ExecutorService executor = Executors.newCachedThreadPool();

	/**
	 * A list of all the active {@link ClassicSession}s.
	 */
	private final SessionRegistry sessions = new SessionRegistry();

	/**
	 * The server's persistence manager.
	 */
	private final PersistanceManager persistenceManager = new PersistanceManager();

	/**
	 * The server's audio manager.
	 */
	private final AudioManager audio = new ServerAudioManager();

	/**
	 * The server's permissions manager.
	 */
	private PermissionManager permManager = new PermissionManager();

	/**
	 * Whether the server is running or not.
	 */
	private boolean running = false;

	/**
	 * The list of levels currently loaded on the server.
	 */
	private List<Level> levels = new ArrayList<Level>();

	/**
	 * The server's tick count.
	 */
	private int ticks = 0;
	
	/**
	 * The server's connection URL.
	 */
	private String url = "";
	
	/**
	 * The server's unique salt.
	 */
	private long salt = new SecureRandom().nextLong();

	public ClassicServer() {
		this(new File("."));
	}

	public ClassicServer(File directory) {
		super(directory);
	}

	public void start(String[] args) {
		if(this.isRunning()) return;
		this.running = true;

		OpenClassic.getLogger().info(String.format(this.getTranslator().translate("core.startup.server"), Constants.VERSION));

		ChannelFactory factory = new NioServerSocketChannelFactory(executor, executor);
		this.bootstrap.setFactory(factory);

		ChannelPipelineFactory pipelineFactory = new ServerPipelineFactory();
		this.bootstrap.setPipelineFactory(pipelineFactory);
		this.setupConfig();

		if(Arrays.asList(args).contains("gui")) {
			this.console = new GuiConsoleManager();
		} else {
			this.console = new TextConsoleManager();
		}

		this.console.setup();

		if(!this.bind(new InetSocketAddress(this.getPort()))) {
			return;
		}

		QuadFactory.setFactory(new ServerQuadFactory());
		TextureFactory.setFactory(new ServerTextureFactory());
		this.persistenceManager.load();
		this.permManager.load();

		this.registerExecutor(this, new ServerCommands());

		this.registerGenerator("normal", new NormalGenerator());
		this.registerGenerator("flat", new FlatLandGenerator());

		VanillaBlock.registerAll();
		
		this.getPluginManager().loadPlugins(LoadOrder.PREWORLD);

		File file = new File(this.getDirectory(), "levels");
		if(!file.exists()) {
			file.mkdirs();
		}

		this.loadLevel(this.getConfig().getString("options.default-level", "main"));
		this.getPluginManager().loadPlugins(LoadOrder.POSTWORLD);

		this.exec.scheduleAtFixedRate(new Runnable() {
			public void run() {
				if(!running) {
					return;
				}
				
				try {
					tick();
				} catch(Exception e) {
					OpenClassic.getLogger().severe(String.format(getTranslator().translate("core.tick-error"), e));
					e.printStackTrace();
				}
			}
		}, 0, 1000 / InternalConstants.TICKS_PER_SECOND, TimeUnit.MILLISECONDS);
		Runnable heartbeat = new Runnable() {
			@Override
			public void run() {
				this.mineBeat();
				this.womBeat();
			}
			
			private void mineBeat() {
				URL url = null;
				
				try {
					url = new URL("https://minecraft.net/heartbeat.jsp?port=" + getPort() + "&max=" + getMaxPlayers() + "&name=" + URLEncoder.encode(Color.stripColor(getServerName()), "UTF-8") + "&public=" + isPublic() + "&version=" + InternalConstants.PROTOCOL_VERSION + "&salt=" + salt + "&users=" + getPlayers().size());
				} catch(MalformedURLException e) {
					OpenClassic.getLogger().severe("Malformed URL while attempting minecraft.net heartbeat?");
					return;
				} catch(UnsupportedEncodingException e) {
					OpenClassic.getLogger().severe("UTF-8 URL encoding is unsupported on your system.");
					return;
				}
				
				HttpURLConnection conn = null;
				
				try {
					conn = (HttpURLConnection) url.openConnection();
					
					try {
						conn.setRequestMethod("GET");
					} catch (ProtocolException e) {
						OpenClassic.getLogger().severe("Exception while performing minecraft.net heartbeat: Connection doesn't support GET...?");
						return;
					}
					
					conn.setDoOutput(false);
					conn.setDoInput(true);
					conn.setUseCaches(false);
					conn.setAllowUserInteraction(false);
					conn.setRequestProperty("Content-type", "text/xml; charset=" + "UTF-8");
					
					InputStream input = conn.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(input));
					String result = reader.readLine();
					
					IOUtils.closeQuietly(reader);
					IOUtils.closeQuietly(input);
					
					if(!ClassicServer.this.url.equals(result)) {
						ClassicServer.this.url = result;
						OpenClassic.getLogger().info(Color.GREEN + "The server's URL is now \"" + ClassicServer.this.url + "\".");
						
						if(OpenClassic.getGame() != null) {
							try {
								File file = new File(OpenClassic.getGame().getDirectory(), "server-address.txt");
								if(!file.exists()) file.createNewFile();
								
								BufferedWriter writer = new BufferedWriter(new FileWriter(file));
								writer.write(result);
								IOUtils.closeQuietly(writer);
							} catch(IOException e) {
								OpenClassic.getLogger().severe("Failed to save server address!");
								e.printStackTrace();
							}
						}
					}
				} catch (IOException e) {
					OpenClassic.getLogger().severe("Exception while performing minecraft.net heartbeat!");
					e.printStackTrace();
				} finally {
					if (conn != null) conn.disconnect();
				}
			}
			
			private void womBeat() {
				URL url = null;
				
				try {
					url = new URL("http://direct.worldofminecraft.com/hb.php?port=" + getPort() + "&max=" + getMaxPlayers() + "&name=" + URLEncoder.encode(Color.stripColor(getServerName()), "UTF-8") + "&public=" + isPublic() + "&version=" + InternalConstants.PROTOCOL_VERSION + "&salt=" + salt + "&users=" + getPlayers().size() + "&noforward=1");
				} catch(MalformedURLException e) {
					OpenClassic.getLogger().severe("Malformed URL while attempting WOM heartbeat?");
					return;
				} catch(UnsupportedEncodingException e) {
					OpenClassic.getLogger().severe("UTF-8 URL encoding is unsupported on your system.");
					return;
				}
				
				HttpURLConnection conn = null;
				
				try {
					conn = (HttpURLConnection) url.openConnection();
					
					conn.setDoOutput(false);
					conn.setDoInput(false);
					conn.setUseCaches(false);
					conn.setAllowUserInteraction(false);
					conn.setRequestProperty("Content-type", "text/xml; charset=" + "UTF-8");
				} catch (IOException e) {
					OpenClassic.getLogger().severe("Exception while performing WOM heartbeat!");
					e.printStackTrace();
				} finally {
					if (conn != null) conn.disconnect();
				}
			}
		};
		
		this.getScheduler().scheduleAsyncRepeatingTask(this, heartbeat, 450, 450);
		heartbeat.run();
		
		this.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				OpenClassic.getLogger().info(getTranslator().translate("level.save-all"));
				saveLevels();
			}
		}, 3000, 3000);
	}

	private void tick() {
		this.getSessionRegistry().tick();
		for(Level level : this.getLevels()) {
			if(level.getPlayers().size() == 0 && !this.getDefaultLevel().getName().equals(level.getName())) {
				this.unloadLevel(level.getName(), false);
			} else {
				((ServerLevel) level).tick();
			}
		}

		for(Plugin plugin : this.getPluginManager().getPlugins()) {
			plugin.tick();
		}

		((ClassicScheduler) this.getScheduler()).tick(this.ticks);
		this.ticks++;
	}

	public void shutdown() {
		if(!this.isRunning()) return;
		this.running = false;
		OpenClassic.getLogger().info("Stopping the server...");

		((ClassicScheduler) this.getScheduler()).shutdown();
		this.getPluginManager().disablePlugins();
		this.getPluginManager().clearPlugins();
		this.unregisterExecutors(this);
		this.url = "";
		this.sendToAll(new PlayerDisconnectMessage("Server shutting down."));

		OpenClassic.getLogger().info("Saving levels...");
		this.saveLevels();

		OpenClassic.getLogger().info("Saving data...");
		this.getConfig().save();
		this.persistenceManager.save();

		OpenClassic.getLogger().info("Closing connections...");
		this.group.close();
		this.bootstrap.getFactory().releaseExternalResources();

		for(BlockType block : Blocks.getBlocks()) {
			if(block != null) {
				Blocks.unregister(block.getId());
			}
		}

		OpenClassic.getLogger().info("Stopping console...");
		this.console.stop();
		this.exec.shutdown();
		OpenClassic.setGame(null);
		System.exit(0);
	}

	public ChannelGroup getChannelGroup() {
		return this.group;
	}

	public SessionRegistry getSessionRegistry() {
		return this.sessions;
	}

	public boolean bind(SocketAddress address) {
		OpenClassic.getLogger().info("Binding to address: " + address + "...");

		try {
			this.group.add(this.bootstrap.bind(address));
			return true;
		} catch(ChannelException e) {
			OpenClassic.getLogger().severe("Failed to bind to address! (is it already in use?)");
			this.shutdown();
			return false;
		}
	}

	public void broadcastMessage(String message) {
		this.getConsoleSender().sendMessage(message);
		for(Player player : this.getPlayers()) {
			player.sendMessage(message);
		}
	}
	
	public void broadcastMessage(String message, Object... args) {
		this.getConsoleSender().sendMessage(message, args);
		for(Player player : this.getPlayers()) {
			player.sendMessage(message, args);
		}
	}

	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();

		for(Level level : this.levels) {
			players.addAll(level.getPlayers());
		}

		return players;
	}

	public Player getPlayer(String name) {
		for(Player player : this.getPlayers()) {
			if(player.getName().equalsIgnoreCase(name)) return player;
		}

		return null;
	}

	public List<Player> matchPlayer(String name) {
		List<Player> result = new ArrayList<Player>();

		for(Player player : this.getPlayers()) {
			if(player.getName().toLowerCase().contains(name.toLowerCase()) && !result.contains(player)) result.add(player);
		}

		return result;
	}

	public long getURLSalt() {
		return this.salt;
	}

	public String getMotd() {
		return getConfig().getString("info.motd", "Welcome to my OpenClassic Server!");
	}

	public void setMotd(String motd) {
		getConfig().setValue("info.motd", motd);
	}

	public String getServerName() {
		return getConfig().getString("info.name", "OpenClassic Server");
	}

	public void setServerName(String name) {
		getConfig().setValue("info.name", name);
	}

	public int getMaxPlayers() {
		return getConfig().getInteger("options.max-players", 20);
	}

	public void setMaxPlayers(int max) {
		getConfig().setValue("options.max-players", max);
	}

	public int getPort() {
		return getConfig().getInteger("options.port", 25565);
	}

	public void setPort(int port) {
		getConfig().setValue("options.port", port);
	}

	public boolean isPublic() {
		return getConfig().getBoolean("options.public", true);
	}

	public void setPublic(boolean serverPublic) {
		getConfig().setValue("options.public", serverPublic);
	}

	public boolean isOnlineMode() {
		return getConfig().getBoolean("options.online-mode", true);
	}

	public void setOnlineMode(boolean online) {
		getConfig().setValue("options.online-mode", online);
	}
	
	public String getURL() {
		return this.url;
	}

	public boolean doesUseWhitelist() {
		return getConfig().getBoolean("options.whitelist", false);
	}

	public void setUseWhitelist(boolean whitelist) {
		getConfig().setValue("options.whitelist", whitelist);
	}

	public boolean isWhitelisted(String player) {
		return this.persistenceManager.isWhitelisted(player);
	}

	public boolean isBanned(String player) {
		return this.persistenceManager.isBanned(player);
	}

	public boolean isIpBanned(String address) {
		return this.persistenceManager.isIpBanned(address);
	}

	public void banPlayer(String player) {
		this.persistenceManager.banPlayer(player);
	}

	public void banPlayer(String player, String reason) {
		this.persistenceManager.banPlayer(player, reason);
	}

	public void unbanPlayer(String player) {
		this.persistenceManager.unbanPlayer(player);
	}

	public void banIp(String address) {
		this.persistenceManager.banIp(address);
	}

	public void banIp(String address, String reason) {
		this.persistenceManager.banIp(address, reason);
	}

	public void unbanIp(String address) {
		this.persistenceManager.unbanIp(address);
	}

	public void whitelist(String player) {
		this.persistenceManager.whitelist(player);
	}

	public void unwhitelist(String player) {
		this.persistenceManager.unwhitelist(player);
	}

	public String getBanReason(String player) {
		return this.persistenceManager.getBanReason(player);
	}

	public String getIpBanReason(String address) {
		return this.persistenceManager.getIpBanReason(address);
	}

	public List<String> getBannedPlayers() {
		return this.persistenceManager.getBannedPlayers();
	}

	public List<String> getBannedIps() {
		return this.persistenceManager.getBannedIps();
	}

	public PermissionManager getPermissionManager() {
		return this.permManager;
	}

	private void setupConfig() {
		this.getConfig().applyDefault("info.name", "OpenClassic Server");
		this.getConfig().applyDefault("info.motd", "Welcome to my OpenClassic Server!");
		this.getConfig().applyDefault("options.port", 25565);
		this.getConfig().applyDefault("options.public", true);
		this.getConfig().applyDefault("options.max-players", 20);
		this.getConfig().applyDefault("options.online-mode", true);
		this.getConfig().applyDefault("options.whitelist", false);
		this.getConfig().applyDefault("options.default-level", "main");
		this.getConfig().applyDefault("physics.enabled", true);
		this.getConfig().applyDefault("physics.falling", true);
		this.getConfig().applyDefault("physics.flower", true);
		this.getConfig().applyDefault("physics.mushroom", true);
		this.getConfig().applyDefault("physics.trees", true);
		this.getConfig().applyDefault("physics.sponge", true);
		this.getConfig().applyDefault("physics.liquid", true);
		this.getConfig().applyDefault("physics.grass", true);
	}

	public ServerLevel createLevel(LevelInfo info, Generator generator) {
		ServerLevel level = new ServerLevel(info);
		level.setAuthor(this.getServerName());
		byte[] data = new byte[info.getWidth() * info.getHeight() * info.getDepth()];
		level.setGenerating(true);
		generator.generate(level, data);
		level.setGenerating(false);
		level.setData(info.getWidth(), info.getHeight(), info.getDepth(), data);

		if(level.getSpawn() == null) {
			level.setSpawn(generator.findSpawn(level));
		}

		try {
			OpenClassicLevelFormat.save(level);
		} catch(IOException e) {
			OpenClassic.getLogger().severe("Failed to save newly created world!");
			e.printStackTrace();
		}

		this.levels.add(level);
		EventManager.callEvent(new LevelCreateEvent(level));
		OpenClassic.getLogger().info("Level \"" + level.getName() + "\" was successfully created!");

		return level;
	}

	public Level loadLevel(String name) {
		return this.loadLevel(name, true);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Level loadLevel(String name, boolean create) {
		if(this.getLevel(name) != null) return this.getLevel(name);

		try {
			ServerLevel level = new ServerLevel();
			level = (ServerLevel) OpenClassicLevelFormat.load(level, name, create);
			if(level == null) {
				return null;
			}

			this.levels.add(level);
			if(this.getConsoleManager() instanceof GuiConsoleManager) {
				DefaultListModel model = ((GuiConsoleManager) this.getConsoleManager()).getFrame().levels;
				model.add(model.size(), level.getName());
				if(model.capacity() == model.size()) model.setSize(model.getSize() + 1);
			}

			EventManager.callEvent(new LevelLoadEvent(level));
			this.broadcastMessage("level.load-success", name);
			return level;
		} catch(IOException e) {
			OpenClassic.getLogger().severe(String.format(this.getTranslator().translate("level.load-fail"), name));
			e.printStackTrace();
		}

		return null;
	}

	public void unloadLevel(String name) {
		this.unloadLevel(name, true);
	}

	@SuppressWarnings("rawtypes")
	public void unloadLevel(String name, boolean announce) {
		if(this.getLevel(name) != null) {
			if(this.getDefaultLevel().getName().equals(name)) {
				if(announce) this.broadcastMessage("level.unload-main");
				return;
			}

			if(EventManager.callEvent(new LevelUnloadEvent(this.getLevel(name))).isCancelled()) {
				return;
			}

			Level level = this.getLevel(name);
			for(Player player : level.getPlayers()) {
				player.moveTo(this.getDefaultLevel().getSpawn());
			}

			this.saveLevel(level);
			this.levels.remove(level);

			if(this.getConsoleManager() instanceof GuiConsoleManager) {
				DefaultListModel model = ((GuiConsoleManager) this.getConsoleManager()).getFrame().levels;
				if(model.indexOf(level.getName()) != -1) {
					model.remove(model.indexOf(level.getName()));
				}
			}

			if(announce) this.broadcastMessage("level.unload-success", name);
		}
	}

	public Level getDefaultLevel() {
		for(Level level : this.levels) {
			if(level.getName().equalsIgnoreCase(this.getConfig().getString("options.default-level", "main"))) return level;
		}

		return (this.levels.size() > 0) ? this.levels.get(0) : null;
	}

	public Level getLevel(String name) {
		for(Level level : this.levels) {
			if(level.getName().equalsIgnoreCase(name)) return level;
		}

		return null;
	}

	public void saveLevels() {
		for(Level level : this.levels) {
			this.saveLevel(level);
		}
	}

	public void saveLevel(Level level) {
		level.getData().save(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.getName() + ".nbt");

		try {
			if(EventManager.callEvent(new LevelSaveEvent(level)).isCancelled()) {
				return;
			}

			OpenClassicLevelFormat.save(level);
		} catch(IOException e) {
			OpenClassic.getLogger().severe("Failed to save level " + level.getName() + "!");
			e.printStackTrace();
		}
	}

	public void saveLevel(String name) {
		if(this.getLevel(name) != null) {
			this.saveLevel(this.getLevel(name));
		}
	}

	public List<Level> getLevels() {
		return new ArrayList<Level>(this.levels);
	}

	public void sendToAll(Message msg) {
		for(Level level : this.levels) {
			((ServerLevel) level).sendToAll(msg);
		}
	}

	public void sendToAllExcept(Player player, Message msg) {
		for(Level level : this.levels) {
			((ServerLevel) level).sendToAllExcept(player, msg);
		}
	}

	public boolean isRunning() {
		return this.running;
	}

	@Override
	public String toString() {
		return "OpenClassic{running=" + this.isRunning() + "}";
	}

	public ConsoleManager getConsoleManager() {
		return this.console;
	}

	@Override
	public AudioManager getAudioManager() {
		return this.audio;
	}

	@Override
	public List<String> getWhitelistedPlayers() {
		return this.persistenceManager.getWhitelistedPlayers();
	}

	@Override
	public Console getConsoleSender() {
		return TextConsoleManager.SENDER;
	}

	public Player getPlayer(byte id) {
		for(Player player : this.getPlayers()) {
			if(((ServerPlayer) player).getPlayerId() == id) return player;
		}

		return null;
	}

}