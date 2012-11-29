package ch.spacebase.openclassic.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import ch.spacebase.openclassic.api.Client;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.ProgressBar;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.physics.CactusPhysics;
import ch.spacebase.openclassic.api.block.physics.FallingBlockPhysics;
import ch.spacebase.openclassic.api.block.physics.FlowerPhysics;
import ch.spacebase.openclassic.api.block.physics.GrassPhysics;
import ch.spacebase.openclassic.api.block.physics.HalfStepPhysics;
import ch.spacebase.openclassic.api.block.physics.LiquidPhysics;
import ch.spacebase.openclassic.api.block.physics.MushroomPhysics;
import ch.spacebase.openclassic.api.block.physics.SaplingPhysics;
import ch.spacebase.openclassic.api.block.physics.SpongePhysics;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.MainScreen;
import ch.spacebase.openclassic.api.input.InputHelper;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.level.generator.FlatLandGenerator;
import ch.spacebase.openclassic.api.level.generator.normal.NormalGenerator;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.plugin.PluginManager.LoadOrder;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.sound.AudioManager;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.command.ClientCommands;
import ch.spacebase.openclassic.client.gui.LoginScreen;
import ch.spacebase.openclassic.client.gui.MainMenuScreen;
import ch.spacebase.openclassic.client.input.ClientInputHelper;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.mode.Mode;
import ch.spacebase.openclassic.client.mode.Singleplayer;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;
import ch.spacebase.openclassic.client.sound.ClientAudioManager;
import ch.spacebase.openclassic.client.util.Directories;
import ch.spacebase.openclassic.client.util.LWJGLNatives;
import ch.spacebase.openclassic.client.util.Projection;
import ch.spacebase.openclassic.client.util.ShaderManager;
import ch.spacebase.openclassic.client.util.Storage;
import ch.spacebase.openclassic.game.ClassicGame;
import ch.spacebase.openclassic.game.level.ClassicLevel;
import de.matthiasmann.twl.utils.PNGDecoder;

import static org.lwjgl.opengl.GL11.*;

public class ClassicClient extends ClassicGame implements Client {

	private boolean running = false;
	private ClientProgressBar progress = new ClientProgressBar();
	private boolean openclassicServer = false;
	private String openclassicVersion = "";

	private ClientAudioManager audio;
	private GuiScreen currentScreen;
	private Mode mode;
	
	private int fps = 0;
	private int columns = 0;
	private String memory = "N/A";
	
	public ClassicClient() {
		super(Directories.getWorkingDirectory());
	}
	
	public void start() {
		this.running = true;
		OpenClassic.setClient(this);
		RenderHelper.setHelper(new ClientRenderHelper());
		InputHelper.setHelper(new ClientInputHelper());
		Storage.loadFavorites(this.getDirectory());
		ThreadRenamingRunnable.setThreadNameDeterminer(new ThreadNameDeterminer() {
			@Override
			public String determineThreadName(String current, String proposed) throws Exception {
				return "Client-" + proposed;
			}
		});
		
		this.setupLogger();
		OpenClassic.getLogger().info(String.format(this.getTranslator().translate("core.startup.client"), Constants.CLIENT_VERSION));
		LWJGLNatives.load(new File(Directories.getWorkingDirectory(), "bin"));
		this.audio = new ClientAudioManager();
		
		this.registerExecutor(null, new ClientCommands());
		this.registerGenerator(new NormalGenerator());
		this.registerGenerator(new FlatLandGenerator());
		
		this.getConfig().applyDefault("options.music", true);
		this.getConfig().applyDefault("options.sound", true);
		this.getConfig().applyDefault("options.show-info", false);
		this.getConfig().applyDefault("options.view-bobbing", true);
		this.getConfig().applyDefault("options.invert-mouse", false);
		this.getConfig().applyDefault("options.view-distance", 0);
		this.getConfig().applyDefault("options.smoothing", false);
		this.getConfig().applyDefault("options.particles", true);
		
		this.getConfig().applyDefault("keys.playerlist", Keyboard.KEY_TAB);
		this.getConfig().applyDefault("keys.forward", Keyboard.KEY_W);
		this.getConfig().applyDefault("keys.back", Keyboard.KEY_S);
		this.getConfig().applyDefault("keys.left", Keyboard.KEY_A);
		this.getConfig().applyDefault("keys.right", Keyboard.KEY_D);
		this.getConfig().applyDefault("keys.jump", Keyboard.KEY_SPACE);
		this.getConfig().applyDefault("keys.select-block", Keyboard.KEY_B);
		this.getConfig().applyDefault("keys.chat", Keyboard.KEY_T);
		if(this.getConfig().contains("options.language")) {
			this.getConfig().remove("options.language");
		}
		
		VanillaBlock.CACTUS.setPhysics(new CactusPhysics());
		VanillaBlock.SAND.setPhysics(new FallingBlockPhysics(VanillaBlock.SAND));
		VanillaBlock.GRAVEL.setPhysics(new FallingBlockPhysics(VanillaBlock.GRAVEL));
		VanillaBlock.ROSE.setPhysics(new FlowerPhysics());
		VanillaBlock.DANDELION.setPhysics(new FlowerPhysics());
		VanillaBlock.GRASS.setPhysics(new GrassPhysics());
		VanillaBlock.WATER.setPhysics(new LiquidPhysics(VanillaBlock.WATER));
		VanillaBlock.LAVA.setPhysics(new LiquidPhysics(VanillaBlock.LAVA));
		VanillaBlock.RED_MUSHROOM.setPhysics(new MushroomPhysics());
		VanillaBlock.BROWN_MUSHROOM.setPhysics(new MushroomPhysics());
		VanillaBlock.SAPLING.setPhysics(new SaplingPhysics());
		VanillaBlock.SPONGE.setPhysics(new SpongePhysics());
		VanillaBlock.SLAB.setPhysics(new HalfStepPhysics());
		
		this.setupGL();
		ClientRenderHelper.getHelper().setup();
		ClientRenderHelper.getHelper().getTextureManager().pickMipmaps();
		this.getPluginManager().loadPlugins(LoadOrder.PREWORLD);
		this.getPluginManager().loadPlugins(LoadOrder.POSTWORLD);
		this.setCurrentScreen(new LoginScreen());

		int frames = 0;
		long lastfps = System.nanoTime() / 1000000;
		long previousTime = System.nanoTime() / 1000000;
		long passedTime = 0;
		try {
			while(this.running && !Display.isCloseRequested()) {
				float delta = passedTime / (float) Constants.TICK_MILLISECONDS;
				this.render(delta);
	
				long time = System.nanoTime() / 1000000; 
				passedTime += time - previousTime; 
				previousTime = time;
				if(time - lastfps > 1000) {
					long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;
					long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;
					this.memory = (max - free) + "/" + max;
					this.fps = frames;
					if(this.getLevel() != null) {
						this.columns = ((ClassicLevel) this.getLevel()).getColumns().size();
					} else {
						this.columns = 0;
					}
					
					frames = 0;
					lastfps += 1000;
				}
	
				frames++;
				while(passedTime > Constants.TICK_MILLISECONDS) { 
					this.update();
					passedTime -= Constants.TICK_MILLISECONDS;
				}
			}
		} catch(Throwable t) {
			t.printStackTrace(); // TODO: good exception handling
		}

		this.shutdown();
	}
	
	private void setupLogger() {
		ConsoleHandler console = new ConsoleHandler();
		console.setFormatter(new DateOutputFormatter(new SimpleDateFormat("HH:mm:ss")));

		Logger logger = Logger.getLogger("");
		for (Handler handler : logger.getHandlers()) {
			logger.removeHandler(handler);
		}

		logger.addHandler(console);
		
		try {
			FileHandler handler = new FileHandler(this.getDirectory().getPath() + "/client.log");
			handler.setFormatter(new DateOutputFormatter(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")));
			OpenClassic.getLogger().addHandler(handler);
		} catch(IOException e) {
			OpenClassic.getLogger().severe(this.getTranslator().translate("log.create-fail"));
			e.printStackTrace();
		}
	}

	private void setupGL() {
		try {
			Display.setDisplayMode(new DisplayMode(854, 480));
			Display.setTitle("OpenClassic");
			try {
				Display.setIcon(new ByteBuffer[] { this.loadIcon() });
			} catch(IOException e) {
				System.out.println("Failed to load icon!");
				e.printStackTrace();
			}
			
			Display.create();
			Mouse.create();
			Keyboard.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		glEnable(GL_TEXTURE_2D);
		glShadeModel(GL_SMOOTH);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_ALPHA_TEST);
		glAlphaFunc(GL_GREATER, 0);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glEnable(GL_FOG);
		
		ShaderManager.setup();
		this.updateFog();
	}

	private ByteBuffer loadIcon() throws IOException {
		InputStream in = this.getClass().getResourceAsStream("/icon.png");

		try {
			PNGDecoder decoder = new PNGDecoder(in);
			ByteBuffer buffer = ByteBuffer.allocateDirect(decoder.getWidth() * decoder.getHeight() * 4);
			decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
			buffer.flip();
			return buffer;
		} finally {
			in.close();
		}
	}

	private void updateFog() { // TODO: Water/lava fog
		float r = 255;
		float g = 255;
		float b = 255;
		if(this.getLevel() != null) {
			r = ((this.getLevel().getFogColor() >> 16) & 255) / 255f;
			g = ((this.getLevel().getFogColor()) & 255) / 255f;
			b = (this.getLevel().getFogColor() & 255) / 255f;
		}

		glClearColor(r, g, b, 255);
		FloatBuffer fogColours = BufferUtils.createFloatBuffer(4);
		fogColours.put(new float[] { r, g, b, 255 });
		fogColours.flip();
		glFog(GL_FOG_COLOR, fogColours);
		glFogi(GL_FOG_MODE, GL_LINEAR);
		glHint(GL_FOG_HINT, GL_NICEST);
		glFogf(GL_FOG_START, 0);
		glFogf(GL_FOG_END, 512 >> this.getConfig().getInteger("options.view-distance", 0));
		glFogf(GL_FOG_DENSITY, 0.005f);
	}

	public void input() {
		if(!Keyboard.isCreated()) return;	
		if(this.mode != null && this.currentScreen == null) {
			this.mode.pollInput();
		}
		
		while(Keyboard.next()) {
			if(Keyboard.getEventKeyState()) {
				if(this.currentScreen != null) {
					this.getCurrentScreen().onKeyPress(Keyboard.getEventCharacter(), Keyboard.getEventKey());
				} else if(this.mode != null) {
					this.mode.onKeyboard(Keyboard.getEventKey());
				}
			}
		}

		if(!Mouse.isCreated()) return;
		while(Mouse.next()) {
			if(Mouse.getEventDWheel() != 0 && this.mode != null && this.currentScreen == null) {
				this.mode.onScroll(Mouse.getEventDWheel());
			}

			if(Mouse.getEventButtonState()) {
				if(this.currentScreen != null) {
					this.getCurrentScreen().onMouseClick(RenderHelper.getHelper().getRenderMouseX(), RenderHelper.getHelper().getRenderMouseY(), Mouse.getEventButton());
					if(this.currentScreen == null) {
						this.mode.setLastChange();
					}
				} else if(this.mode != null) {
					this.mode.onClick(Mouse.getEventX(), Mouse.getEventY(), Mouse.getEventButton());
				}
			}
		}
		
		if(this.currentScreen == null) {
			Mouse.setGrabbed(true);
			Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
		} else {
			Mouse.setGrabbed(false);
		}
	}

	public void update() {
		this.input();
		if(this.mode != null) this.mode.update();
		this.audio.update((ClientPlayer) this.getPlayer());	
		if(this.getCurrentScreen() != null) this.getCurrentScreen().update();
	}

	public void render(float delta) {
		if(this.mode != null) this.mode.renderUpdate(delta);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glLoadIdentity();

		this.updateFog();
		glPushMatrix();
		Projection.perspective();
		if(this.mode != null) this.mode.renderPerspective(delta);
		
		glPopMatrix();
		glPushMatrix();
		Projection.ortho();

		int width = Display.getWidth();// * 240 / Display.getHeight();
		int height = Display.getHeight();// * 240 / Display.getHeight();
		if(this.mode != null) this.mode.renderOrtho(width, height);
		if(this.getCurrentScreen() != null) this.getCurrentScreen().render();
		
		this.progress.render();
		glPopMatrix();
		Display.update();
		Display.sync(Display.getDesktopDisplayMode().getFrequency());
	}

	public int getFps() {
		return this.fps;
	}

	@Override
	public void shutdown() {
		this.running = false;
		if(this.mode != null) this.mode.unload();
		this.audio.cleanup();
		Storage.saveFavorites();
		ShaderManager.cleanup();
		Display.destroy();
		Mouse.destroy();
		Keyboard.destroy();
		System.exit(0);
	}

	@Override
	public Level createLevel(LevelInfo info) {
		ClientLevel level = new ClientLevel(info);
		this.setMode(new Singleplayer(level));
		return level;
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public AudioManager getAudioManager() {
		return this.audio;
	}

	@Override
	public Player getPlayer() {
		return this.mode == null ? null : this.mode.getPlayer();
	}

	@Override
	public Level getLevel() {
		return this.mode == null ? null : this.mode.getLevel();
	}

	@Override
	public Level openLevel(String name) {
		/* LevelFormat format = null;
		File dir = new File(this.getDirectory(), "levels/" + name);
		if(!dir.exists() || !dir.isDirectory()) {
			File file = new File(this.getDirectory(), "levels/" + name + ".mine");
			if(file.exists()) {
				format = new MinecraftClassicLevelFormat(file);
			}
			
			file = new File(this.getDirectory(), "levels/" + name + ".mclevel");
			if(file.exists()) {
				format = new MinecraftClassicLevelFormat(file);
			}
			
			file = new File(this.getDirectory(), "levels/" + name + ".dat");
			if(file.exists()) {
				format = new MinecraftClassicLevelFormat(file);
			}
			
			file = new File(this.getDirectory(), "levels/" + name + ".lvl");
			if(file.exists()) {
				format = new MCSharpLevelFormat(file);
			}
			
			file = new File(this.getDirectory(), "levels/" + name + ".oclvl");
			if(file.exists()) {
				format = new OpenClassicOldLevelFormat(file);
			}
			
			file = new File(this.getDirectory(), "levels/" + name + ".map");
			if(file.exists()) {
				format = new OpenClassicLegacyLevelFormat(file);
			}
		} TODO: someday */
		
		ClientLevel level = new ClientLevel(name, false);
		this.setMode(new Singleplayer(level));
		return level;
	}

	@Override
	@Deprecated
	public void saveLevel() {
		if(this.mode == null || this.mode.getLevel() == null) return;
		this.mode.getLevel().save();
	}

	@Override
	public void setCurrentScreen(GuiScreen screen) {
		if(this.currentScreen != null) {
			this.currentScreen.onClose();
			this.currentScreen.clearWidgets();
		}

		this.currentScreen = screen;
		if(this.currentScreen != null) {
			this.currentScreen.open(Display.getWidth(), Display.getHeight());
		}
	}

	@Override
	public GuiScreen getCurrentScreen() {
		return this.currentScreen;
	}

	@Override
	public boolean isInGame() {
		return this.mode != null && this.mode.isInGame();
	}

	@Override
	public MainScreen getMainScreen() {
		return this.mode == null ? null : this.mode.getMainScreen();
	}

	@Override
	public void exitLevel() {
		this.exitLevel(true);
	}

	@Override
	public void exitLevel(boolean save) {
		if(this.mode == null) return;
		if(this.getLevel() != null) {
			((ClassicLevel) this.getLevel()).dispose();
			if(save && this.mode instanceof Singleplayer) {
				this.getLevel().save();
			}
		}
		
		this.setMode(null);
	}

	@Override
	public boolean isConnectedToOpenClassic() {
		return this.openclassicServer;
	}

	public void setOpenClassicServer(boolean openclassic, String version) {
		this.openclassicServer = openclassic;
		this.openclassicVersion = version;
	}

	@Override
	public String getServerVersion() {
		return this.openclassicVersion;
	}

	@Override
	public ProgressBar getProgressBar() {
		return this.progress;
	}

	public Mode getMode() {
		return this.mode;
	}

	public void setMode(Mode mode) {
		if(this.mode != null) this.mode.unload();
		this.mode = mode;
		if(this.mode == null) {
			this.setCurrentScreen(new MainMenuScreen());
		}
	}
	
	private static class DateOutputFormatter extends Formatter {
		private final SimpleDateFormat date;

		public DateOutputFormatter(SimpleDateFormat date) {
			this.date = date;
		}

		@Override
		public String format(LogRecord record) {
			StringBuilder builder = new StringBuilder();

			builder.append(date.format(record.getMillis()));
			builder.append(" [");
			builder.append(record.getLevel().getLocalizedName().toUpperCase());
			builder.append("] ");
			builder.append(formatMessage(record));
			builder.append('\n');

			if (record.getThrown() != null) {
				StringWriter writer = new StringWriter();
				record.getThrown().printStackTrace(new PrintWriter(writer));
				builder.append(writer.toString());
			}

			return builder.toString();
		}
	}

	public String getMemoryDisplay() {
		return this.memory;
	}
	
	public int getColumns() {
		return this.columns;
	}

}
