package ch.spacebase.openclassic.client;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.LevelIO;
import com.mojang.minecraft.level.generator.LevelGenerator;

import ch.spacebase.openclassic.api.Client;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.ProgressBar;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.level.LevelCreateEvent;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.MainScreen;
import ch.spacebase.openclassic.api.input.InputHelper;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.level.generator.FlatLandGenerator;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.plugin.PluginManager.LoadOrder;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.sound.AudioManager;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.block.physics.TNTPhysics;
import ch.spacebase.openclassic.client.command.ClientCommands;
import ch.spacebase.openclassic.client.input.ClientInputHelper;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.ClassicGame;
import ch.spacebase.openclassic.game.util.DateOutputFormatter;
import ch.spacebase.openclassic.game.util.EmptyMessageFormatter;
import ch.spacebase.openclassic.game.util.LoggerOutputStream;

public class ClassicClient extends ClassicGame implements Client {
	
	private final Minecraft mc;
	
	public ClassicClient(Minecraft mc) {
		super(GeneralUtils.getMinecraftDirectory());
		RenderHelper.setHelper(new ClientRenderHelper());
		InputHelper.setHelper(new ClientInputHelper());
		this.mc = mc;
		
		// Init logger
		ConsoleHandler console = new ConsoleHandler();
		console.setFormatter(new DateOutputFormatter(new SimpleDateFormat("HH:mm:ss"), new EmptyMessageFormatter()));

		Logger logger = Logger.getLogger("");
		for (Handler handler : logger.getHandlers()) {
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
		
		this.registerExecutor(null, new ClientCommands());
		this.registerGenerator("normal", new LevelGenerator());
		this.registerGenerator("flat", new FlatLandGenerator());
		

		VanillaBlock.TNT.setPhysics(new TNTPhysics());
		
		this.getPluginManager().loadPlugins(LoadOrder.PREWORLD);
		this.getPluginManager().loadPlugins(LoadOrder.POSTWORLD);
	}

	@Override
	public void shutdown() {
		this.mc.running = false;
	}

	@Override
	public Level createLevel(LevelInfo info, Generator generator) {
		if(generator instanceof LevelGenerator) {
			((LevelGenerator) generator).setInfo(info.getName(), this.mc.data != null ? this.mc.data.username : "unknown", info.getWidth(), info.getHeight(), info.getDepth());
		}
		
		com.mojang.minecraft.level.Level level = new com.mojang.minecraft.level.Level();
		level.name = info.getName();
		level.creator = this.mc.data != null ? this.mc.data.username : "unknown";
		level.createTime = System.currentTimeMillis();
		byte[] data = new byte[info.getWidth() * info.getHeight() * info.getDepth()];
		level.setData(info.getWidth(), info.getHeight(), info.getDepth(), data);
		generator.generate(level.openclassic, data);
		level.setData(info.getWidth(), info.getHeight(), info.getDepth(), data);
		level.openclassic.setSpawn(generator.findSpawn(level.openclassic));
		if(info.getSpawn() != null) {
			level.openclassic.setSpawn(info.getSpawn());
		}
		
		level.openclassic.data = new NBTData(level.name);
		level.openclassic.data.load(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.name + ".nbt");
		this.mc.mode.prepareLevel(level);
		this.mc.setLevel(level);
		EventFactory.callEvent(new LevelCreateEvent(level.openclassic));
		return level.openclassic;
	}

	@Override
	public boolean isRunning() {
		return this.mc.running;
	}

	@Override
	public Player getPlayer() {
		return this.mc.player.openclassic;
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
		
		VanillaBlock.registerAll();
		com.mojang.minecraft.level.Level level = LevelIO.load(name);
		if(level != null) {
			this.mc.setLevel(level);
			this.mc.initGame();
			this.mc.setCurrentScreen(null);
			return level.openclassic;
		} else {
			VanillaBlock.unregisterAll();
		}
		
		return null;
	}

	@Override
	public void saveLevel() {
		if(this.mc.level == null) return;
		LevelIO.save(this.mc.level);
	}
	
	@Override
	public void exitLevel() {
		this.exitLevel(true);
	}
	
	@Override
	public void exitLevel(boolean save) {
		if(this.mc.level == null) return;
		
		if(save) this.saveLevel();
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
	public void setCurrentScreen(GuiScreen screen) {
		this.mc.setCurrentScreen(screen);
	}

	@Override
	public GuiScreen getCurrentScreen() {
		return this.mc.currentScreen;
	}

	@Override
	public boolean isInGame() {
		return this.mc.ingame;
	}

	@Override
	public MainScreen getMainScreen() {
		return this.mc.hud;
	}

	@Override
	public boolean isConnectedToOpenClassic() {
		return this.mc.openclassicServer;
	}

	@Override
	public ProgressBar getProgressBar() {
		return this.mc.progressBar;
	}

	@Override
	public String getServerVersion() {
		return this.mc.openclassicVersion;
	}

}
