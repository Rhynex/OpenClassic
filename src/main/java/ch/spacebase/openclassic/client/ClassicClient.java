package ch.spacebase.openclassic.client;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.lwjgl.input.Mouse;

import ch.spacebase.openclassic.api.Client;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.ProgressBar;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.QuadFactory;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.event.level.LevelCreateEvent;
import ch.spacebase.openclassic.api.event.level.LevelLoadEvent;
import ch.spacebase.openclassic.api.event.level.LevelSaveEvent;
import ch.spacebase.openclassic.api.gui.GuiComponent;
import ch.spacebase.openclassic.api.gui.HUDComponent;
import ch.spacebase.openclassic.api.gui.base.ComponentHelper;
import ch.spacebase.openclassic.api.input.InputHelper;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.level.generator.FlatLandGenerator;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.api.level.generator.NormalGenerator;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.plugin.PluginManager.LoadOrder;
import ch.spacebase.openclassic.api.settings.Settings;
import ch.spacebase.openclassic.api.settings.bindings.Bindings;
import ch.spacebase.openclassic.api.sound.AudioManager;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.block.physics.TNTPhysics;
import ch.spacebase.openclassic.client.command.ClientCommands;
import ch.spacebase.openclassic.client.gui.ErrorScreen;
import ch.spacebase.openclassic.client.gui.GameOverScreen;
import ch.spacebase.openclassic.client.gui.base.ClientComponentHelper;
import ch.spacebase.openclassic.client.input.ClientInputHelper;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.render.ClientQuadFactory;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.client.util.HTTPUtil;
import ch.spacebase.openclassic.client.util.ServerDataStore;
import ch.spacebase.openclassic.game.ClassicGame;
import ch.spacebase.openclassic.game.block.physics.StationaryWaterPhysics;
import ch.spacebase.openclassic.game.block.physics.WaterPhysics;
import ch.spacebase.openclassic.game.io.OpenClassicLevelFormat;
import ch.spacebase.openclassic.game.util.DateOutputFormatter;
import ch.spacebase.openclassic.game.util.EmptyMessageFormatter;
import ch.spacebase.openclassic.game.util.InternalConstants;
import ch.spacebase.openclassic.game.util.LoggerOutputStream;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.gamemode.SurvivalGameMode;
import com.zachsthings.onevent.EventManager;

public class ClassicClient extends ClassicGame implements Client {

	private final Minecraft mc;
	
	public static void start(String[] args) {
		new Thread(new Minecraft(null, 854, 480), "Client-Main").start();
	}

	public ClassicClient(Minecraft mc) {
		super(GeneralUtils.getMinecraftDirectory());
		InputHelper.setHelper(new ClientInputHelper());
		QuadFactory.setFactory(new ClientQuadFactory());
		ComponentHelper.setHelper(new ClientComponentHelper());
		this.mc = mc;

		// Init logger
		ConsoleHandler console = new ConsoleHandler();
		console.setFormatter(new DateOutputFormatter(new SimpleDateFormat("HH:mm:ss"), new EmptyMessageFormatter()));

		Logger logger = Logger.getLogger("");
		for(Handler handler : logger.getHandlers()) {
			logger.removeHandler(handler);
		}

		logger.addHandler(console);

		try {
			FileHandler handler = new FileHandler(this.getDirectory().getPath() + "/client.log");
			handler.setFormatter(new DateOutputFormatter(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"), new EmptyMessageFormatter()));
			OpenClassic.getLogger().addHandler(handler);
		} catch(IOException e) {
			OpenClassic.getLogger().severe(this.getTranslator().translate("log.create-fail"));
			e.printStackTrace();
		}

		System.setOut(new PrintStream(new LoggerOutputStream(java.util.logging.Level.INFO), true));
		System.setErr(new PrintStream(new LoggerOutputStream(java.util.logging.Level.SEVERE), true));
	}

	public void init() {
		OpenClassic.getLogger().info(String.format(this.getTranslator().translate("core.startup.client"), Constants.VERSION));

		ServerDataStore.loadFavorites(this.getDirectory());
		this.registerExecutor(this, new ClientCommands());
		this.registerGenerator("normal", new NormalGenerator());
		this.registerGenerator("flat", new FlatLandGenerator());

		VanillaBlock.TNT.setPhysics(new TNTPhysics());
		VanillaBlock.WATER.setPhysics(new WaterPhysics(VanillaBlock.WATER, true, true));
		VanillaBlock.STATIONARY_WATER.setPhysics(new StationaryWaterPhysics());

		this.getPluginManager().loadPlugins(LoadOrder.PREWORLD);
		this.getPluginManager().loadPlugins(LoadOrder.POSTWORLD);
	}

	@Override
	public void shutdown() {
		this.mc.running = false;
	}

	@Override
	public Level createLevel(LevelInfo info, Generator generator) {
		if(this.isInGame()) {
			this.exitGameSession();
		}
		
		com.mojang.minecraft.level.Level level = new com.mojang.minecraft.level.Level();
		level.name = info.getName();
		level.creator = this.getPlayer().getName() != null ? this.getPlayer().getName() : "unknown";
		level.createTime = System.currentTimeMillis();
		byte[] data = new byte[info.getWidth() * info.getHeight() * info.getDepth()];
		level.setData(info.getWidth(), info.getHeight(), info.getDepth(), data);
		level.openclassic.setGenerating(true);
		generator.generate(level.openclassic, data);
		level.openclassic.setGenerating(false);
		level.setData(info.getWidth(), info.getHeight(), info.getDepth(), data);
		level.openclassic.setSpawn(generator.findSpawn(level.openclassic));
		if(info.getSpawn() != null) {
			level.openclassic.setSpawn(info.getSpawn());
		}

		level.openclassic.data = new NBTData(level.name);
		level.openclassic.data.load(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.name + ".nbt");
		this.openLevel(level);
		this.mc.mode.prepareLevel(level);
		EventManager.callEvent(new LevelCreateEvent(level.openclassic));
		return level.openclassic;
	}

	@Override
	public boolean isRunning() {
		return this.mc.running;
	}

	@Override
	public Player getPlayer() {
		return this.mc.ocPlayer;
	}

	@Override
	public Level getLevel() {
		if(this.mc.level == null) return null;
		return this.mc.level.openclassic;
	}

	@Override
	public Level openLevel(String name) {
		if(this.mc.level != null && this.mc.level.name.equals(name)) {
			return this.mc.level.openclassic;
		}

		OpenClassic.getClient().getProgressBar().setVisible(true);
		OpenClassic.getClient().getProgressBar().setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.singleplayer"));
		OpenClassic.getClient().getProgressBar().setSubtitle(OpenClassic.getGame().getTranslator().translate("level.loading"));
		OpenClassic.getClient().getProgressBar().setText(OpenClassic.getGame().getTranslator().translate("level.reading"));
		OpenClassic.getClient().getProgressBar().setProgress(-1);
		OpenClassic.getClient().getProgressBar().render();
		com.mojang.minecraft.level.Level level = null;
		try {
			level = new com.mojang.minecraft.level.Level();
			level = ((ClientLevel) OpenClassicLevelFormat.load(level.openclassic, name, false)).getHandle();
			level.openclassic.data = new NBTData(level.name);
			level.openclassic.data.load(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.name + ".nbt");
			EventManager.callEvent(new LevelLoadEvent(level.openclassic));
			this.openLevel(level);
			OpenClassic.getClient().getProgressBar().setVisible(false);
			return level.openclassic;
		} catch(IOException e) {
			OpenClassic.getClient().getProgressBar().setText(String.format(OpenClassic.getGame().getTranslator().translate("level.load-fail"), name));
			e.printStackTrace();
			try {
				Thread.sleep(1000L);
			} catch(InterruptedException e1) {
			}

			OpenClassic.getClient().getProgressBar().setVisible(false);
			return null;
		}
	}
	
	private void openLevel(com.mojang.minecraft.level.Level level) {
		if(this.mc.level != null && this.mc.level == level) {
			return;
		}
		
		if(this.isInGame()) {
			this.exitGameSession();
		}

		VanillaBlock.registerAll();
		this.mc.setLevel(level);
		this.mc.initGame();
		this.setActiveComponent(null);
	}

	@Override
	public boolean saveLevel() {
		if(this.mc.level == null) return false;
		if(EventManager.callEvent(new LevelSaveEvent(this.mc.level.openclassic)).isCancelled()) {
			return false;
		}

		try {
			OpenClassicLevelFormat.save(this.mc.level.openclassic);
			if(this.mc.level.openclassic.getData() != null) {
				this.mc.level.openclassic.getData().save(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + this.mc.level.name + ".nbt");
			}
			
			return true;
		} catch(IOException e) {
			OpenClassic.getLogger().severe("Failed to save level \"" + this.mc.level.name + "\"");
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean saveLevel(String name) {
		if(this.mc.level == null) return false;
		String old = this.mc.level.name;
		this.mc.level.name = name;
		boolean ret = this.saveLevel();
		this.mc.level.name = old;
		return ret;
	}
	
	@Override
	public void exitGameSession() {
		this.mc.stopGame(true);
	}

	@Override
	public AudioManager getAudioManager() {
		return this.mc.audio;
	}

	public Minecraft getMinecraft() {
		return this.mc;
	}

	@Override
	public GuiComponent getActiveComponent() {
		if(this.mc.baseGUI.getComponents().isEmpty()) {
			return null;
		}
		
		return this.mc.baseGUI.getComponents().get(0);
	}
	
	@Override
	public void setActiveComponent(GuiComponent component) {
		this.mc.baseGUI.clearComponents();
		if(component == null && this.mc.player != null && this.mc.mode instanceof SurvivalGameMode && this.mc.player.health <= 0) {
			component = new GameOverScreen();
		}
		
		if(component != null) {
			this.mc.baseGUI.attachComponent(component);
			component.setFocused(true);
			if(this.mc.player != null) {
                this.mc.player.input.resetKeys();
			}
			
			Mouse.setGrabbed(false);
		} else {
			this.mc.grabMouse();
		}
	}

	@Override
	public boolean isInGame() {
		return this.mc.ingame;
	}

	@Override
	public HUDComponent getHUD() {
		return this.mc.hud;
	}
	
	@Override
	public boolean isInMultiplayer() {
		return this.mc.isInMultiplayer();
	}

	@Override
	public boolean isConnectedToOpenClassic() {
		return this.mc.openclassicServer;
	}
	
	@Override
	public String getServerVersion() {
		return this.mc.openclassicVersion;
	}

	@Override
	public ProgressBar getProgressBar() {
		return this.mc.progressBar;
	}

	@Override
	public Settings getSettings() {
		return this.mc.settings;
	}
	
	@Override
	public Bindings getBindings() {
		return this.mc.bindings;
	}
	
	@Override
	public boolean isInSurvival() {
		return this.mc.mode instanceof SurvivalGameMode && !this.isInMultiplayer();
	}
	
	public void joinServer(String url) {
		if(this.isInGame()) {
			this.exitGameSession();
		}
		
		this.getProgressBar().setVisible(true);
		this.getProgressBar().setSubtitleScaled(false);
		this.getProgressBar().setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.multiplayer"));
		this.getProgressBar().setSubtitle(OpenClassic.getGame().getTranslator().translate("connecting.connect"));
		this.getProgressBar().setText(OpenClassic.getGame().getTranslator().translate("connecting.getting-info"));
		this.getProgressBar().setProgress(-1);
		this.getProgressBar().render();
		String play = HTTPUtil.fetchUrl(url, "", InternalConstants.MINECRAFT_URL_HTTPS + "classic/list");
		String mppass = HTTPUtil.getParameterOffPage(play, "mppass");

		if(mppass.length() > 0) {
			this.mc.tempKey = mppass;
			this.mc.server = HTTPUtil.getParameterOffPage(play, "server");
			try {
				this.mc.port = Integer.parseInt(HTTPUtil.getParameterOffPage(play, "port"));
			} catch(NumberFormatException e) {
				this.setActiveComponent(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("connecting.fail-connect"), OpenClassic.getGame().getTranslator().translate("connecting.invalid-page")));
				this.mc.server = null;
				this.getProgressBar().setVisible(false);
				this.getProgressBar().setSubtitleScaled(true);
				return;
			}
		} else {
			this.setActiveComponent(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("connecting.fail-connect"), OpenClassic.getGame().getTranslator().translate("connecting.check")));
			this.getProgressBar().setVisible(false);
			this.getProgressBar().setSubtitleScaled(true);
			return;
		}

		this.getProgressBar().setVisible(false);
		this.mc.initGame();
		this.setActiveComponent(null);
	}

	@Override
	public Settings getHackSettings() {
		return this.mc.hackSettings;
	}

	@Override
	public List<Player> getPlayers() {
		return this.getLevel().getPlayers();
	}

	@Override
	public Player getPlayer(String name) {
		for(Player player : this.getPlayers()) {
			if(player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}

		return null;
	}

	@Override
	public List<Player> matchPlayer(String name) {
		List<Player> result = new ArrayList<Player>();
		for(Player player : this.getPlayers()) {
			if(player.getName().toLowerCase().contains(name.toLowerCase()) && !result.contains(player)) {
				result.add(player);
			}
		}

		return result;
	}

}
