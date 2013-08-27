package com.mojang.minecraft;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.event.block.BlockPlaceEvent;
import ch.spacebase.openclassic.api.event.player.PlayerKeyChangeEvent;
import ch.spacebase.openclassic.api.event.player.PlayerQuitEvent;
import ch.spacebase.openclassic.api.event.player.PlayerRespawnEvent;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.api.network.msg.PlayerSetBlockMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.api.network.msg.custom.KeyChangeMessage;
import ch.spacebase.openclassic.api.player.Session.State;
import ch.spacebase.openclassic.api.plugin.Plugin;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.settings.BooleanSetting;
import ch.spacebase.openclassic.api.settings.IntSetting;
import ch.spacebase.openclassic.api.settings.Settings;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.ClientProgressBar;
import ch.spacebase.openclassic.client.gui.LoginScreen;
import ch.spacebase.openclassic.client.gui.MainMenuScreen;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;
import ch.spacebase.openclassic.client.render.Renderer;
import ch.spacebase.openclassic.client.sound.ClientAudioManager;
import ch.spacebase.openclassic.client.util.BlockUtils;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.client.util.LWJGLNatives;
import ch.spacebase.openclassic.client.util.ShaderManager;
import ch.spacebase.openclassic.game.scheduler.ClassicScheduler;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.item.Arrow;
import com.mojang.minecraft.entity.item.Item;
import com.mojang.minecraft.entity.model.ModelPart;
import com.mojang.minecraft.entity.model.Vector;
import com.mojang.minecraft.entity.particle.ParticleManager;
import com.mojang.minecraft.entity.particle.WaterDropParticle;
import com.mojang.minecraft.entity.player.InputHandler;
import com.mojang.minecraft.entity.player.LocalPlayer;
import com.mojang.minecraft.entity.player.net.NetworkPlayer;
import com.mojang.minecraft.gamemode.CreativeGameMode;
import com.mojang.minecraft.gamemode.GameMode;
import com.mojang.minecraft.gamemode.SurvivalGameMode;
import com.mojang.minecraft.gui.ChatInputScreen;
import com.mojang.minecraft.gui.ErrorScreen;
import com.mojang.minecraft.gui.GameOverScreen;
import com.mojang.minecraft.gui.HUDScreen;
import com.mojang.minecraft.gui.MenuScreen;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.phys.Intersection;
import com.mojang.minecraft.render.Chunk;
import com.mojang.minecraft.render.ChunkVisibleAndDistanceComparator;
import com.mojang.minecraft.render.Frustum;
import com.mojang.minecraft.render.FontRenderer;
import com.mojang.minecraft.render.HeldBlock;
import com.mojang.minecraft.render.LevelRenderer;
import com.mojang.minecraft.render.FogRenderer;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.minecraft.render.animation.AnimatedTexture;
import com.mojang.minecraft.render.animation.WaterTexture;
import com.mojang.minecraft.settings.Bindings;
import com.mojang.minecraft.settings.MusicSetting;
import com.mojang.minecraft.settings.NightSetting;
import com.mojang.minecraft.settings.SurvivalSetting;
import com.mojang.minecraft.settings.TextureRefreshSetting;
import com.zachsthings.onevent.EventManager;

public final class Minecraft implements Runnable {

	private static final Random rand = new Random();

	public GameMode mode;
	public int width;
	public int height;
	private Timer timer = new Timer(Constants.TICKS_PER_SECOND);
	public Level level;
	public LevelRenderer levelRenderer;
	public LocalPlayer player;
	public ParticleManager particleManager;
	public SessionData data = null;
	public Canvas canvas;
	public volatile boolean waiting = false;
	public TextureManager textureManager;
	public FontRenderer fontRenderer;
	public GuiScreen currentScreen = null;
	public ClientProgressBar progressBar = new ClientProgressBar();
	public FogRenderer fogRenderer = new FogRenderer(this);
	public ClientAudioManager audio;
	public ResourceDownloadThread resourceThread;
	private int ticks;
	private int blockHitTime;
	public Robot robot;
	public HUDScreen hud;
	public boolean awaitingLevel;
	public Intersection selected;
	public String server;
	public int port;
	public volatile boolean running;
	public String debugInfo;
	private int lastClick;
	public boolean raining;
	public File dir;
	public boolean ingame;
	private boolean started;
	public String levelName = "";
	public int levelSize = 0;
	private boolean shutdown = false;

	public boolean hideGui = false;
	public boolean openclassicServer = false;
	public String openclassicVersion = "";
	public List<RemotePluginInfo> serverPlugins = new ArrayList<RemotePluginInfo>();
	public int mipmapMode = 0;
	public ClientSession session;
	public boolean displayActive = false;
	public int rainTicks = 0;
	public HeldBlock heldBlock = new HeldBlock();
	public HashMap<Byte, NetworkPlayer> netPlayers = new HashMap<Byte, NetworkPlayer>();
	private int schedTicks = 0;
	private long lastUpdate = System.currentTimeMillis();
	private int fps = 0;
	public Settings settings;
	public Settings hackSettings;
	public Bindings bindings;
	public boolean hacks = true;

	public Minecraft(Canvas canvas, int width, int height) {
		this.ticks = 0;
		this.blockHitTime = 0;
		this.selected = null;
		this.server = null;
		this.port = 0;
		this.running = false;
		this.debugInfo = "";
		this.lastClick = 0;
		this.raining = false;

		try {
			for(LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if(info.getName().equals("Nimbus")) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		this.canvas = canvas;
		this.width = width;
		this.height = height;
		if(canvas != null) {
			try {
				this.robot = new Robot();
			} catch(AWTException e) {
				e.printStackTrace();
			}
		}
	}

	public final void setCurrentScreen(GuiScreen screen) {
		if(this.currentScreen != null) {
			this.currentScreen.onClose();
		}

		if(screen == null && this.player != null && this.mode instanceof SurvivalGameMode && this.player.health <= 0) {
			screen = new GameOverScreen();
		}

		this.currentScreen = screen;
		if(screen != null) {
			if(this.player != null) {
				this.player.releaseAllKeys();
			}

			Mouse.setGrabbed(false);
			screen.open(this.width, this.height);
		} else {
			this.grabMouse();
		}
	}

	private static void checkGLError(String task) {
		/* int error = GL11.glGetError();
		if(error != 0) {
			String message = GLU.gluErrorString(error);
			OpenClassic.getLogger().severe("########## GL ERROR ##########");
			OpenClassic.getLogger().severe("@ " + task);
			OpenClassic.getLogger().severe(error + ": " + message);
			System.exit(0);
		} */
	}

	public final void shutdown() {
		if(this.shutdown) {
			return;
		}

		this.shutdown = true;
		this.running = false;
		if(this.ingame) this.stopGame(false);
		if(this.resourceThread != null) {
			this.resourceThread.running = false;
		}

		OpenClassic.getClient().unregisterExecutors(OpenClassic.getClient());
		((ClassicScheduler) OpenClassic.getClient().getScheduler()).shutdown();
		this.audio.cleanup();
		OpenClassic.setGame(null);
		this.destroyRender();
		System.exit(0);
	}

	public void stopGame(boolean menu) {
		this.audio.stopMusic();
		this.serverPlugins.clear();
		if(menu) this.setCurrentScreen(new MainMenuScreen());
		if(this.data != null) {
			this.data.key = "";
		}

		this.level = null;
		this.particleManager = null;
		this.hud = null;
		if(this.player != null && this.player.openclassic.getData() != null && !this.isInMultiplayer()) {
			this.player.openclassic.getData().save(OpenClassic.getClient().getDirectory().getPath() + "/player.nbt");
		}

		if(this.isInMultiplayer()) {
			if(this.player != null) {
				EventManager.callEvent(new PlayerQuitEvent(OpenClassic.getClient().getPlayer(), "Quit"));
			}

			this.session.disconnect(null);
			this.session = null;
		}

		for(BlockType block : Blocks.getBlocks()) {
			if(block != null) {
				Blocks.unregister(block.getId());
			}
		}

		this.netPlayers.clear();
		this.openclassicServer = false;
		this.server = null;
		this.port = 0;
		this.ingame = false;
		this.player = null;
		this.hideGui = false;
		this.hacks = true;
	}

	public void initGame() {
		this.initGame(OpenClassic.getGame().getGenerator("normal"));
	}

	public void initGame(Generator gen) {
		this.audio.stopMusic();
		this.audio.nextBGM = System.currentTimeMillis();
		if(this.server != null && this.data != null) {
			Level level = new Level();
			level.setData(8, 8, 8, new byte[512]);
			this.setLevel(level);
		} else {
			if(this.level == null) {
				VanillaBlock.registerAll();
				this.progressBar.setVisible(true);
				this.progressBar.setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.singleplayer"));
				this.progressBar.setSubtitle(OpenClassic.getGame().getTranslator().translate("level.generating"));
				this.progressBar.setText("");
				this.progressBar.setProgress(-1);
				this.progressBar.render();
				OpenClassic.getClient().createLevel(new LevelInfo(!this.levelName.equals("") ? this.levelName : "A Nice World", null, (short) (128 << this.levelSize), (short) 128, (short) (128 << this.levelSize)), gen);
				this.progressBar.setVisible(false);
				this.levelName = "";
			}
		}

		this.particleManager = new ParticleManager(this.level, this.textureManager);
		this.hud = new HUDScreen(this);

		if(this.server != null && this.data != null && this.player != null) {
			this.session = new ClientSession(this.player.openclassic, this.data.key, this.server, this.port);
		}

		this.mode = this.settings.getIntSetting("options.survival").getValue() > 0 && !this.isInMultiplayer() ? new SurvivalGameMode(this) : new CreativeGameMode(this);
		if(this.level != null) {
			this.mode.apply(this.level);
		}

		if(this.player != null) {
			this.mode.apply(this.player);
		}

		this.ingame = true;
	}

	private void handleException(Throwable e) {
		e.printStackTrace();
		if(!this.running) {
			return;
		} 

		if(this.started && !(e instanceof LWJGLException) && !(e instanceof RuntimeException)) {
			this.setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("core.client-error"), String.format(OpenClassic.getGame().getTranslator().translate("core.game-broke"), e)));
		} else {
			String msg = "Exception occured";
			if(OpenClassic.getGame() != null && OpenClassic.getGame().getTranslator() != null) {
				msg = OpenClassic.getGame().getTranslator().translate("core.exception");
			}

			JOptionPane.showMessageDialog(null, "See .minecraft_classic/client.log for more details.\n" + e.toString(), msg, 0);
			this.running = false;
		}
	}

	private ByteBuffer loadIcon(InputStream in) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 128 * 128);
		buffer.clear();
		byte[] data = (byte[]) ImageIO.read(in).getRaster().getDataElements(0, 0, 128, 128, null);
		buffer.put(data);
		buffer.rewind();
		return buffer;
	}

	@SuppressWarnings({ "unused" })
	public final void run() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				running = false;
			}
		});

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				OpenClassic.getLogger().severe("Uncaught exception in thread \"" + t.getName() + "\"");
				handleException(e);
			}
		});

		this.running = true;
		new ClassicClient(this);
		this.dir = GeneralUtils.getMinecraftDirectory();
		File lib = new File(this.dir, "lib");
		if(!lib.exists()) {
			try {
				lib.mkdirs();
			} catch(SecurityException e) {
				e.printStackTrace();
			}
		}
		
		LWJGLNatives.load(lib);
		File levels = new File(this.dir, "levels");
		if(!levels.exists()) {
			try {
				levels.mkdirs();
			} catch(SecurityException e) {
				e.printStackTrace();
			}
		}

		File screenshots = new File(this.dir, "screenshots");
		if(!screenshots.exists()) {
			try {
				screenshots.mkdirs();
			} catch(SecurityException e) {
				e.printStackTrace();
			}
		}

		File texturepacks = new File(this.dir, "texturepacks");
		if(!texturepacks.exists()) {
			try {
				texturepacks.mkdirs();
			} catch(SecurityException e) {
				e.printStackTrace();
			}
		}

		SessionData.loadFavorites(this.dir);
		this.audio = new ClientAudioManager(this);
		this.bindings = new Bindings(this.dir);
		this.settings = new Settings();
		this.settings.registerSetting(new MusicSetting("options.music", "options.music"));
		this.settings.getBooleanSetting("options.music").setDefault(true);
		this.settings.registerSetting(new BooleanSetting("options.sound", "options.sound"));
		this.settings.getBooleanSetting("options.sound").setDefault(true);
		this.settings.registerSetting(new BooleanSetting("options.invert-mouse", "options.invert-mouse"));
		this.settings.registerSetting(new BooleanSetting("options.show-info", "options.show-info"));
		this.settings.registerSetting(new IntSetting("options.render-distance", "options.render-distance", new String[] { "FAR", "NORMAL", "SHORT", "TINY" }));
		this.settings.registerSetting(new BooleanSetting("options.view-bobbing", "options.view-bobbing"));
		this.settings.getBooleanSetting("options.view-bobbing").setDefault(true);
		this.settings.registerSetting(new TextureRefreshSetting("options.3d-anaglyph", "options.3d-anaglyph"));
		this.settings.registerSetting(new BooleanSetting("options.limit-fps", "options.limit-fps"));
		this.settings.registerSetting(new SurvivalSetting("options.survival", "options.survival", new String[] { "OFF", "PEACEFUL", "NORMAL" }));
		this.settings.registerSetting(new TextureRefreshSetting("options.smoothing", "options.smoothing"));
		this.settings.registerSetting(new NightSetting("options.night", "options.night"));
		this.settings.registerSetting(new IntSetting("options.sensitivity", "options.sensitivity", new String[] { "SLOW", "NORMAL", "FAST", "FASTER", "FASTEST" }));
		this.settings.getIntSetting("options.sensitivity").setDefault(1);
		this.settings.registerSetting(new IntSetting("options.blockChooser", "options.blockChooser", new String[] { "DEFAULT", "MODIFIED", "FANCY" } ));
		this.settings.registerSetting(new BooleanSetting("options.minimap", "options.minimap"));
		
		this.hackSettings = new Settings();
		this.hackSettings.registerSetting(new BooleanSetting("hacks.speed", "hacks.speed"));
		this.hackSettings.registerSetting(new BooleanSetting("hacks.flying", "hacks.flying"));
		OpenClassic.getClient().getConfig().applyDefault("options.texture-pack", "none");
		OpenClassic.getClient().getConfig().save();
		
		this.mode = this.settings.getIntSetting("options.survival").getValue() > 0 ? new SurvivalGameMode(this) : new CreativeGameMode(this);
		Item.initModels();
		this.initRender();

		((ClassicClient) OpenClassic.getClient()).init();
		this.resourceThread = new ResourceDownloadThread(this.dir, this, this.progressBar);
		this.resourceThread.start();

		this.progressBar.setVisible(true);
		this.progressBar.setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.loading"));
		this.progressBar.setSubtitle(OpenClassic.getGame().getTranslator().translate("http.downloading-resources"));
		this.progressBar.setProgress(-1);
		this.lastUpdate = System.currentTimeMillis();
		this.fps = 0;
		while(this.running) {
			if(this.waiting) {
				try {
					Thread.sleep(100);
				} catch(InterruptedException e) {
				}
			} else {
				if(!this.started) {
					if(this.resourceThread.isFinished()) {
						this.progressBar.setVisible(false);
						try {
							Thread.sleep(1000);
						} catch(InterruptedException e) {
						}

						if(this.server == null || this.server.equals("") || this.port == 0) {
							this.setCurrentScreen(new LoginScreen());
						} else {
							this.initGame();
						}

						this.started = true;
					}
				}

				this.timer.update();
				for(int tick = 0; tick < this.timer.elapsedTicks; tick++) {
					this.ticks++;
					this.tick();
				}

				if(!this.render()) {
					break;
				}
			}
		}

		this.shutdown();
		return;
	}
	
	private void initRender() {
		try {
			if(this.canvas != null) {
				Display.setParent(this.canvas);
			} else {
				Display.setDisplayMode(new DisplayMode(this.width, this.height));
				Display.setResizable(true);
				try {
					Display.setIcon(new ByteBuffer[] { this.loadIcon(TextureManager.class.getResourceAsStream("/icon.png")) });
				} catch(IOException e) {
					OpenClassic.getLogger().severe("Failed to load icon!");
					e.printStackTrace();
				}
			}
		} catch(LWJGLException e) {
			this.handleException(e);
			return;
		}

		Display.setTitle("OpenClassic " + Constants.VERSION);
		try {
			Display.create();
			Keyboard.create();
			Mouse.create();
		} catch(LWJGLException e) {
			this.handleException(e);
			return;
		}

		try {
			Controllers.create();
		} catch(LWJGLException e) {
			e.printStackTrace();
		}

		checkGLError("Pre startup");
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glClearDepth(GL11.GL_CLIENT_PIXEL_STORE_BIT);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		if(GLContext.getCapabilities().OpenGL30) {
			this.mipmapMode = 1;
		} else if(GLContext.getCapabilities().GL_EXT_framebuffer_object) {
			this.mipmapMode = 2;
		} else if(GLContext.getCapabilities().OpenGL14) {
			this.mipmapMode = 3;
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		}

		this.textureManager = new TextureManager(this.settings);
		this.textureManager.addAnimatedTexture((new com.mojang.minecraft.render.animation.LavaTexture()));
		this.textureManager.addAnimatedTexture((new com.mojang.minecraft.render.animation.WaterTexture()));
		this.fontRenderer = new FontRenderer("/default.png", this.textureManager);
		this.levelRenderer = new LevelRenderer(this.textureManager);
		ShaderManager.setup();
		GL11.glViewport(0, 0, this.width, this.height);
		checkGLError("Startup");
	}
	
	private void destroyRender() {
		ShaderManager.cleanup();
		Display.destroy();
	}
	
	private boolean render() {
		if(Display.isCloseRequested()) {
			return false;
		}

		if(this.width != Display.getWidth() || this.height != Display.getHeight()) {
			this.resize();
		}
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		if(this.displayActive && !Display.isActive() && !Mouse.isButtonDown(0) && !Mouse.isButtonDown(1) && !Mouse.isButtonDown(2)) {
			this.displayMenu();
		}
		
		this.displayActive = Display.isActive();
		this.mode.applyBlockCracks(this.timer.delta);
		if(Mouse.isGrabbed()) {
			int x = Mouse.getDX();
			int y = Mouse.getDY();
			byte direction = 1;
			if(this.settings.getBooleanSetting("options.invert-mouse").getValue()) {
				direction = -1;
			}

			this.player.turn(x, (y * direction));
			Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
		}
		
		checkGLError("Pre render");
		RenderHelper.getHelper().bindTexture("/terrain.png", true);
		for(int index = 0; index < this.textureManager.animations.size(); index++) {
			AnimatedTexture animation = this.textureManager.animations.get(index);
			ByteBuffer buffer = BufferUtils.createByteBuffer(animation.textureData.length);
			buffer.put(animation.textureData);
			buffer.flip();
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, animation.textureId % 16 << 4, animation.textureId / 16 << 4, 16, 16, 6408, 5121, buffer);
			if(animation instanceof WaterTexture) {
				RenderHelper.getHelper().bindTexture("/water.png", true);
				GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 16, 16, 6408, 5121, buffer);
				RenderHelper.getHelper().bindTexture("/terrain.png", true);
			}
		}

		int width = ClientRenderHelper.getHelper().getGuiWidth();
		int height = ClientRenderHelper.getHelper().getGuiHeight();
		if(this.level != null) {
			float pitch = this.player.oPitch + (this.player.pitch - this.player.oPitch) * this.timer.delta;
			float yaw = this.player.oYaw + (this.player.yaw - this.player.oYaw) * this.timer.delta;
			Vector pVec = ClientRenderHelper.getHelper().getPlayerVector(this.player, this.timer.delta);
			float ycos = MathHelper.cos(-yaw * MathHelper.DEG_TO_RAD - MathHelper.PI);
			float ysin = MathHelper.sin(-yaw * MathHelper.DEG_TO_RAD - MathHelper.PI);
			float pcos = MathHelper.cos(-pitch * MathHelper.DEG_TO_RAD);
			float psin = MathHelper.sin(-pitch * MathHelper.DEG_TO_RAD);
			float mx = ysin * pcos;
			float mz = ycos * pcos;
			float reach = this.mode.getReachDistance();
			this.selected = this.level.clip(pVec, pVec.add(mx * reach, psin * reach, mz * reach), true);
			if(this.selected != null) {
				reach = this.selected.blockPos.distance(ClientRenderHelper.getHelper().getPlayerVector(this.player, this.timer.delta));
			}

			pVec = ClientRenderHelper.getHelper().getPlayerVector(this.player, this.timer.delta);
			if(this.mode instanceof CreativeGameMode) {
				reach = 32;
			}

			Entity selectedEntity = null;
			List<Entity> entities = this.level.blockMap.getEntities(this.player, this.player.bb.expand(mx * reach, psin * reach, mz * reach));

			float distance = 0;
			for(int count = 0; count < entities.size(); count++) {
				Entity entity = entities.get(count);
				if(entity.isPickable()) {
					Intersection pos = entity.bb.grow(0.1F, 0.1F, 0.1F).clip(pVec, pVec.add(mx * reach, psin * reach, mz * reach));
					if(pos != null && (pVec.distance(pos.blockPos) < distance || distance == 0)) {
						selectedEntity = entity;
						distance = pVec.distance(pos.blockPos);
					}
				}
			}

			if(selectedEntity != null && !(this.mode instanceof CreativeGameMode)) {
				this.selected = new Intersection(selectedEntity);
			}

			int pass = 0;
			while(true) {
				if(pass >= 2) {
					GL11.glColorMask(true, true, true, false);
					break;
				}

				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					if(pass == 0) {
						GL11.glColorMask(false, true, true, false);
					} else {
						GL11.glColorMask(true, false, false, false);
					}
				}

				GL11.glViewport(0, 0, this.width, this.height);
				float fogDensity = 1.0F - (float) Math.pow(1.0F / (4 - this.settings.getIntSetting("options.render-distance").getValue()), 0.25D);
				float skyRed = (this.level.skyColor >> 16 & 255) / 255.0F;
				float skyGreen = (this.level.skyColor >> 8 & 255) / 255.0F;
				float skyBlue = (this.level.skyColor & 255) / 255.0F;
				this.fogRenderer.fogRed = (this.level.fogColor >> 16 & 255) / 255.0F;
				this.fogRenderer.fogGreen = (this.level.fogColor >> 8 & 255) / 255.0F;
				this.fogRenderer.fogBlue = (this.level.fogColor & 255) / 255.0F;
				this.fogRenderer.fogRed += (skyRed - this.fogRenderer.fogRed) * fogDensity;
				this.fogRenderer.fogGreen += (skyGreen - this.fogRenderer.fogGreen) * fogDensity;
				this.fogRenderer.fogBlue += (skyBlue - this.fogRenderer.fogBlue) * fogDensity;
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
				GL11.glEnable(GL11.GL_CULL_FACE);
				this.fogRenderer.fogEnd = (512 >> (this.settings.getIntSetting("options.render-distance").getValue() << 1));
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					GL11.glTranslatef((-((pass << 1) - 1)) * 0.07F, 0.0F, 0.0F);
				}

				float fov = 70;
				if(this.player.health <= 0) {
					fov /= (1.0F - 500.0F / (this.player.deathTime + this.timer.delta + 500.0F)) * 2.0F + 1.0F;
				}

				GLU.gluPerspective(fov, (float) this.width / (float) this.height, 0.05F, this.fogRenderer.fogEnd);
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glLoadIdentity();
				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					GL11.glTranslatef(((pass << 1) - 1) * 0.1F, 0.0F, 0.0F);
				}

				ClientRenderHelper.getHelper().hurtEffect(this.player, this.timer.delta);
				if(this.settings.getBooleanSetting("options.view-bobbing").getValue()) {
					ClientRenderHelper.getHelper().applyBobbing(this.player, this.timer.delta);
				}

				GL11.glTranslatef(0.0F, 0.0F, -0.1F);
				GL11.glRotatef(this.player.oPitch + (this.player.pitch - this.player.oPitch) * this.timer.delta, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(this.player.oYaw + (this.player.yaw - this.player.oYaw) * this.timer.delta, 0.0F, 1.0F, 0.0F);
				float rx = this.player.xo + (this.player.x - this.player.xo) * this.timer.delta;
				float ry = this.player.yo + (this.player.y - this.player.yo) * this.timer.delta;
				float rz = this.player.zo + (this.player.z - this.player.zo) * this.timer.delta;
				GL11.glTranslatef(-rx, -ry, -rz);
				Frustum.update();
				for(int count = 0; count < this.levelRenderer.chunkCache.length; count++) {
					this.levelRenderer.chunkCache[count].clip();
				}

				try {
					Collections.sort(this.levelRenderer.chunks, new ChunkVisibleAndDistanceComparator(this.player));
				} catch(Exception e) {
				}

				int max = this.levelRenderer.chunks.size() - 1;
				int amount = this.levelRenderer.chunks.size();
				if(amount > 3) {
					amount = 3;
				}

				for(int count = 0; count < amount; count++) {
					Chunk chunk = this.levelRenderer.chunks.remove(max - count);
					chunk.update();
					chunk.loaded = false;
				}

				this.fogRenderer.updateFog();
				GL11.glEnable(GL11.GL_FOG);
				this.levelRenderer.sortChunks(this.player, 0);
				if(this.level.preventsRendering(this.player.x, this.player.y, this.player.z, 0.1F)) {
					for(int bx = (int) this.player.x - 1; bx <= (int) this.player.x + 1; bx++) {
						for(int by = (int) this.player.y - 1; by <= (int) this.player.y + 1; by++) {
							for(int bz = (int) this.player.z - 1; bz <= (int) this.player.z + 1; bz++) {
								int id = this.levelRenderer.level.getTile(bx, by, bz);
								if(id != 0 && Blocks.fromId(id) != null && Blocks.fromId(id).getPreventsRendering()) {
									GL11.glColor4f(0.2F, 0.2F, 0.2F, 1.0F);
									GL11.glDepthFunc(GL11.GL_LESS);
									Blocks.fromId(id).getModel().renderAll(bx, by, bz, 0.2F);
									GL11.glCullFace(GL11.GL_FRONT);
									Blocks.fromId(id).getModel().renderAll(bx, by, bz, 0.2F);
									GL11.glCullFace(GL11.GL_BACK);
									GL11.glDepthFunc(GL11.GL_LEQUAL);
								}
							}
						}
					}
				}

				ClientRenderHelper.getHelper().setLighting(true);
				this.levelRenderer.level.blockMap.render(ClientRenderHelper.getHelper().getPlayerVector(this.player, this.timer.delta), this.levelRenderer.textures, this.timer.delta);
				ClientRenderHelper.getHelper().setLighting(false);
				this.fogRenderer.updateFog();
				float xmod = -MathHelper.cos(this.player.yaw * MathHelper.DEG_TO_RAD);
				float zmod = -MathHelper.sin(this.player.yaw * MathHelper.DEG_TO_RAD);
				float xdir = -zmod * MathHelper.sin(this.player.pitch * MathHelper.DEG_TO_RAD);
				float zdir = xmod * MathHelper.sin(this.player.pitch * MathHelper.DEG_TO_RAD);
				float ymod = MathHelper.cos(this.player.pitch * MathHelper.DEG_TO_RAD);

				for(int particle = 0; particle < 2; particle++) {
					if(this.particleManager.particles[particle].size() != 0) {
						int textureId = 0;
						if(particle == 0) {
							textureId = this.particleManager.textureManager.bindTexture("/particles.png");
						}

						if(particle == 1) {
							textureId = this.particleManager.textureManager.bindTexture("/terrain.png");
						}

						RenderHelper.getHelper().bindTexture(textureId);
						Renderer.get().begin();

						for(int count = 0; count < this.particleManager.particles[particle].size(); count++) {
							this.particleManager.particles[particle].get(count).render(this.timer.delta, xmod, ymod, zmod, xdir, zdir);
						}

						Renderer.get().end();
					}
				}

				RenderHelper.getHelper().bindTexture("/rock.png", true);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glCallList(this.levelRenderer.listId);
				this.fogRenderer.updateFog();
				RenderHelper.getHelper().bindTexture("/clouds.png", true);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				float cloudRed = (this.levelRenderer.level.cloudColor >> 16 & 255) / 255.0F;
				float cloudBlue = (this.levelRenderer.level.cloudColor >> 8 & 255) / 255.0F;
				float cloudGreen = (this.levelRenderer.level.cloudColor & 255) / 255.0F;
				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					cloudRed = (cloudRed * 30.0F + cloudBlue * 59.0F + cloudGreen * 11.0F) / 100.0F;
					cloudBlue = (cloudRed * 30.0F + cloudBlue * 70.0F) / 100.0F;
					cloudGreen = (cloudRed * 30.0F + cloudGreen * 70.0F) / 100.0F;
				}

				float texCoordMod = 1 / 2048f;
				float cloudHeight = (this.levelRenderer.level.height + 2);
				float movement = (this.levelRenderer.ticks + this.timer.delta) * texCoordMod * 0.03F;
				Renderer.get().begin();
				Renderer.get().color(cloudRed, cloudBlue, cloudGreen);

				for(int x = -2048; x < this.levelRenderer.level.width + 2048; x += 512) {
					for(int z = -2048; z < this.levelRenderer.level.depth + 2048; z += 512) {
						Renderer.get().vertexuv(x, cloudHeight, (z + 512), x * texCoordMod + movement, (z + 512) * texCoordMod);
						Renderer.get().vertexuv((x + 512), cloudHeight, (z + 512), (x + 512) * texCoordMod + movement, (z + 512) * texCoordMod);
						Renderer.get().vertexuv((x + 512), cloudHeight, z, (x + 512) * texCoordMod + movement, z * texCoordMod);
						Renderer.get().vertexuv(x, cloudHeight, z, x * texCoordMod + movement, z * texCoordMod);
						Renderer.get().vertexuv(x, cloudHeight, z, x * texCoordMod + movement, z * texCoordMod);
						Renderer.get().vertexuv((x + 512), cloudHeight, z, (x + 512) * texCoordMod + movement, z * texCoordMod);
						Renderer.get().vertexuv((x + 512), cloudHeight, (z + 512), (x + 512) * texCoordMod + movement, (z + 512) * texCoordMod);
						Renderer.get().vertexuv(x, cloudHeight, (z + 512), x * texCoordMod + movement, (z + 512) * texCoordMod);
					}
				}

				Renderer.get().end();
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				Renderer.get().begin();
				float skRed = (this.levelRenderer.level.skyColor >> 16 & 255) / 255.0F;
				float skBlue = (this.levelRenderer.level.skyColor >> 8 & 255) / 255.0F;
				float skGreen = (this.levelRenderer.level.skyColor & 255) / 255.0F;
				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					skRed = (skRed * 30.0F + skBlue * 59.0F + skGreen * 11.0F) / 100.0F;
					skBlue = (skRed * 30.0F + skBlue * 70.0F) / 100.0F;
					skGreen = (skRed * 30.0F + skGreen * 70.0F) / 100.0F;
				}

				Renderer.get().color(skRed, skBlue, skGreen);
				float skyHeight = (this.levelRenderer.level.height + 10);

				for(int x = -2048; x < this.levelRenderer.level.width + 2048; x += 512) {
					for(int z = -2048; z < this.levelRenderer.level.depth + 2048; z += 512) {
						Renderer.get().vertex(x, skyHeight, z);
						Renderer.get().vertex((x + 512), skyHeight, z);
						Renderer.get().vertex((x + 512), skyHeight, (z + 512));
						Renderer.get().vertex(x, skyHeight, (z + 512));
					}
				}

				Renderer.get().end();
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				this.fogRenderer.updateFog();
				if(this.selected != null) {
					GL11.glDisable(GL11.GL_ALPHA_TEST);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glEnable(GL11.GL_ALPHA_TEST);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, (MathHelper.sin(System.currentTimeMillis() / 100.0F) * 0.2F + 0.4F) * 0.5F);
					if(this.levelRenderer.cracks > 0) {
						GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
						RenderHelper.getHelper().bindTexture("/terrain.png", true);
						GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
						GL11.glPushMatrix();
						int bid = this.levelRenderer.level.getTile(this.selected.x, this.selected.y, this.selected.z);
						BlockType btype = bid > 0 ? Blocks.fromId(bid) : null;
						GL11.glDepthMask(false);
						if(btype == null) {
							btype = VanillaBlock.STONE;
						}

						for(int count = 0; count < btype.getModel().getQuads().size(); count++) {
							ClientRenderHelper.getHelper().drawCracks(btype.getModel().getQuad(count), this.selected.x, this.selected.y, this.selected.z, 240 + (int) (this.levelRenderer.cracks * 10.0F));
						}

						GL11.glDepthMask(true);
						GL11.glPopMatrix();
					}

					GL11.glDisable(GL11.GL_BLEND);
					GL11.glDisable(GL11.GL_ALPHA_TEST);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
					GL11.glLineWidth(2.0F);
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					GL11.glDepthMask(false);
					int block = this.levelRenderer.level.getTile(this.selected.x, this.selected.y, this.selected.z);
					if(block > 0) {
						AABB aabb = BlockUtils.getSelectionBox(block, this.selected.x, this.selected.y, this.selected.z).grow(0.002F, 0.002F, 0.002F);
						GL11.glBegin(GL11.GL_LINE_STRIP);
						GL11.glVertex3f(aabb.x0, aabb.y0, aabb.z0);
						GL11.glVertex3f(aabb.x1, aabb.y0, aabb.z0);
						GL11.glVertex3f(aabb.x1, aabb.y0, aabb.z1);
						GL11.glVertex3f(aabb.x0, aabb.y0, aabb.z1);
						GL11.glVertex3f(aabb.x0, aabb.y0, aabb.z0);
						GL11.glEnd();
						GL11.glBegin(GL11.GL_LINE_STRIP);
						GL11.glVertex3f(aabb.x0, aabb.y1, aabb.z0);
						GL11.glVertex3f(aabb.x1, aabb.y1, aabb.z0);
						GL11.glVertex3f(aabb.x1, aabb.y1, aabb.z1);
						GL11.glVertex3f(aabb.x0, aabb.y1, aabb.z1);
						GL11.glVertex3f(aabb.x0, aabb.y1, aabb.z0);
						GL11.glEnd();
						GL11.glBegin(GL11.GL_LINES);
						GL11.glVertex3f(aabb.x0, aabb.y0, aabb.z0);
						GL11.glVertex3f(aabb.x0, aabb.y1, aabb.z0);
						GL11.glVertex3f(aabb.x1, aabb.y0, aabb.z0);
						GL11.glVertex3f(aabb.x1, aabb.y1, aabb.z0);
						GL11.glVertex3f(aabb.x1, aabb.y0, aabb.z1);
						GL11.glVertex3f(aabb.x1, aabb.y1, aabb.z1);
						GL11.glVertex3f(aabb.x0, aabb.y0, aabb.z1);
						GL11.glVertex3f(aabb.x0, aabb.y1, aabb.z1);
						GL11.glEnd();
					}

					GL11.glDepthMask(true);
					GL11.glEnable(GL11.GL_TEXTURE_2D);
					GL11.glDisable(GL11.GL_BLEND);
					GL11.glEnable(GL11.GL_ALPHA_TEST);
				}

				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				this.fogRenderer.updateFog();
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_BLEND);
				RenderHelper.getHelper().bindTexture("/water.png", true);
				GL11.glCallList(this.levelRenderer.listId + 1);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glColorMask(false, false, false, false);
				int cy = this.levelRenderer.sortChunks(this.player, 1);
				GL11.glColorMask(true, true, true, true);
				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					if(pass == 0) {
						GL11.glColorMask(false, true, true, false);
					} else {
						GL11.glColorMask(true, false, false, false);
					}
				}

				if(cy > 0) {
					RenderHelper.getHelper().bindTexture("/terrain.png", true);
					GL11.glCallLists(this.levelRenderer.buffer);
				}

				GL11.glDepthMask(true);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glDisable(GL11.GL_FOG);
				if(this.raining) {
					int x = (int) this.player.x;
					int y = (int) this.player.y;
					int z = (int) this.player.z;
					GL11.glDisable(GL11.GL_CULL_FACE);
					GL11.glNormal3f(0, 1, 0);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					RenderHelper.getHelper().bindTexture("/rain.png", true);

					for(int cx = x - 5; cx <= x + 5; cx++) {
						for(int cz = z - 5; cz <= z + 5; cz++) {
							cy = this.level.getHighestTile(cx, cz);
							int minRY = y - 5;
							int maxRY = y + 5;
							if(minRY < cy) {
								minRY = cy;
							}

							if(maxRY < cy) {
								maxRY = cy;
							}

							if(minRY != maxRY) {
								float downfall = (((this.rainTicks + cx * 3121 + cz * 418711) % 32) + this.timer.delta) / 32.0F;
								float rax = cx + 0.5F - this.player.x;
								float raz = cz + 0.5F - this.player.z;
								float visibility = (float) Math.sqrt(rax * rax + raz * raz) / 5;
								GL11.glColor4f(1.0F, 1.0F, 1.0F, (1.0F - visibility * visibility) * 0.7F);
								Renderer.get().begin();
								Renderer.get().vertexuv(cx, minRY, cz, 0.0F, minRY * 2.0F / 8.0F + downfall * 2.0F);
								Renderer.get().vertexuv((cx + 1), minRY, (cz + 1), 2.0F, minRY * 2.0F / 8.0F + downfall * 2.0F);
								Renderer.get().vertexuv((cx + 1), maxRY, (cz + 1), 2.0F, maxRY * 2.0F / 8.0F + downfall * 2.0F);
								Renderer.get().vertexuv(cx, maxRY, cz, 0.0F, maxRY * 2.0F / 8.0F + downfall * 2.0F);
								Renderer.get().vertexuv(cx, minRY, (cz + 1), 0.0F, minRY * 2.0F / 8.0F + downfall * 2.0F);
								Renderer.get().vertexuv((cx + 1), minRY, cz, 2.0F, minRY * 2.0F / 8.0F + downfall * 2.0F);
								Renderer.get().vertexuv((cx + 1), maxRY, cz, 2.0F, maxRY * 2.0F / 8.0F + downfall * 2.0F);
								Renderer.get().vertexuv(cx, maxRY, (cz + 1), 0.0F, maxRY * 2.0F / 8.0F + downfall * 2.0F);
								Renderer.get().end();
							}
						}
					}

					GL11.glEnable(GL11.GL_CULL_FACE);
					GL11.glDisable(GL11.GL_BLEND);
				}

				if(selectedEntity != null) {
					selectedEntity.renderHover(this.textureManager, this.timer.delta);
				}

				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				GL11.glLoadIdentity();
				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					GL11.glTranslatef(((pass << 1) - 1) * 0.1F, 0.0F, 0.0F);
				}

				ClientRenderHelper.getHelper().hurtEffect(this.player, this.timer.delta);
				if(this.settings.getBooleanSetting("options.view-bobbing").getValue()) {
					ClientRenderHelper.getHelper().applyBobbing(this.player, this.timer.delta);
				}

				float heldPos = this.heldBlock.lastPosition + (this.heldBlock.heldPosition - this.heldBlock.lastPosition) * this.timer.delta;
				GL11.glPushMatrix();
				GL11.glRotatef(this.player.oPitch + (this.player.pitch - this.player.oPitch) * this.timer.delta, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(this.player.oYaw + (this.player.yaw - this.player.oYaw) * this.timer.delta, 0.0F, 1.0F, 0.0F);
				ClientRenderHelper.getHelper().setLighting(true);
				GL11.glPopMatrix();
				GL11.glPushMatrix();
				if(this.heldBlock.moving) {
					float off = (this.heldBlock.heldOffset + this.timer.delta) / 7.0F;
					float offsin = MathHelper.sin(off * MathHelper.PI);
					GL11.glTranslatef(-MathHelper.sin((float) Math.sqrt(off) * MathHelper.PI) * 0.4F, MathHelper.sin((float) Math.sqrt(off) * MathHelper.TWO_PI) * 0.2F, -offsin * 0.2F);
				}

				GL11.glTranslatef(0.7F * 0.8F, -0.65F * 0.8F - (1.0F - heldPos) * 0.6F, -0.9F * 0.8F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
				GL11.glEnable(GL11.GL_NORMALIZE);
				if(this.heldBlock.moving) {
					float off = (this.heldBlock.heldOffset + this.timer.delta) / 7.0F;
					float offsin = MathHelper.sin((off) * off * MathHelper.PI);
					GL11.glRotatef(MathHelper.sin((float) Math.sqrt(off) * MathHelper.PI) * 80.0F, 0.0F, 1.0F, 0.0F);
					GL11.glRotatef(-offsin * 20.0F, 1.0F, 0.0F, 0.0F);
				}

				float brightness = this.level.getBrightness((int) this.player.x, (int) this.player.y, (int) this.player.z);
				GL11.glColor4f(brightness, brightness, brightness, 1);

				if(!this.hideGui) {
					if(this.heldBlock.block != null) {
						GL11.glScalef(0.4F, 0.4F, 0.4F);
						GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
						this.heldBlock.block.getModel().renderAll(0, 0, 0, this.level.getBrightness((int) this.player.x, (int) this.player.y, (int) this.player.z));
					} else {
						this.player.bindTexture(this.textureManager);
						GL11.glScalef(1.0F, -1.0F, -1.0F);
						GL11.glTranslatef(0.0F, 0.2F, 0.0F);
						GL11.glRotatef(-120.0F, 0.0F, 0.0F, 1.0F);
						GL11.glScalef(1.0F, 1.0F, 1.0F);
						ModelPart arm = this.player.getModel().leftArm;
						if(!arm.hasList) {
							arm.generateList(0.0625F);
						}

						GL11.glCallList(arm.list);
					}
				}

				GL11.glDisable(GL11.GL_NORMALIZE);
				GL11.glPopMatrix();
				ClientRenderHelper.getHelper().setLighting(false);
				if(!this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					break;
				}

				pass++;
			}

			ClientRenderHelper.getHelper().ortho();
			this.hud.render(this.timer.delta, this.currentScreen != null, Mouse.getX() * width / this.width, height - Mouse.getY() * height / this.height - 1);
		} else {
			GL11.glViewport(0, 0, this.width, this.height);
			GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			ClientRenderHelper.getHelper().ortho();
		}

		if(this.currentScreen != null) {
			this.currentScreen.render();
		}

		if(this.progressBar.isVisible()) {
			this.progressBar.render(false);
		}

		checkGLError("Render");
		Thread.yield();
		Display.update();

		if(this.settings.getBooleanSetting("options.limit-fps").getValue()) {
			try {
				Thread.sleep(5);
			} catch(InterruptedException e) {
			}
		}

		checkGLError("Post render");
		this.fps++;
		while(System.currentTimeMillis() >= this.lastUpdate + 1000) {
			this.debugInfo = this.fps + " fps, " + Chunk.chunkUpdates + " chunk updates";
			com.mojang.minecraft.render.Chunk.chunkUpdates = 0;
			this.lastUpdate += 1000;
			this.fps = 0;
		}
		
		return true;
	}

	public void grabMouse() {
		if(!Mouse.isGrabbed()) {
			Mouse.setGrabbed(true);
			this.setCurrentScreen(null);
			this.lastClick = this.ticks + 10000;
		}
	}

	public void displayMenu() {
		if(this.currentScreen == null && this.ingame && (!this.isInMultiplayer() || this.session.isConnected() && this.session.getState() == State.GAME)) {
			this.setCurrentScreen(new MenuScreen());
		}
	}

	private void onMouseClick(int button) {
		if(button != 0 || this.blockHitTime <= 0) {
			if(button == 0) {
				this.heldBlock.heldOffset = -1;
				this.heldBlock.moving = true;
			}

			int selected = this.player.inventory.getSelected();
			if(button == 1 && selected > 0 && this.mode.useItem(this.player, selected)) {
				this.heldBlock.heldPosition = 0;
			} else if(this.selected == null) {
				if(button == 0 && !(this.mode instanceof CreativeGameMode)) {
					this.blockHitTime = 10;
				}

			} else {
				if(this.selected.entityPos) {
					if(button == 0) {
						this.selected.entity.hurt(this.player, 4);
						return;
					}
				} else {
					int x = this.selected.x;
					int y = this.selected.y;
					int z = this.selected.z;
					if(button != 0) {
						if(this.selected.side == 0) {
							y--;
						}

						if(this.selected.side == 1) {
							y++;
						}

						if(this.selected.side == 2) {
							z--;
						}

						if(this.selected.side == 3) {
							z++;
						}

						if(this.selected.side == 4) {
							x--;
						}

						if(this.selected.side == 5) {
							x++;
						}
					}

					if(button == 0) {
						if(this.level != null && (Blocks.fromId(this.level.getTile(x, y, z)) != VanillaBlock.BEDROCK || this.player.userType >= 100)) {
							this.mode.hitBlock(x, y, z);
							return;
						}
					} else {
						int id = this.player.inventory.getSelected();
						if(id <= 0) {
							return;
						}

						if(this.player.openclassic.getPlaceMode() > 0) {
							id = this.player.openclassic.getPlaceMode();
						}

						BlockType block = this.level.openclassic.getBlockTypeAt(x, y, z);
						AABB collision = BlockUtils.getCollisionBox(id, x, y, z);
						if((block == null || block.canPlaceIn()) && (collision == null || (!this.player.bb.intersects(collision) && this.level.isFree(collision)))) {
							if(!this.mode.canPlace(id)) {
								return;
							}

							if(this.isInMultiplayer()) {
								this.session.send(new PlayerSetBlockMessage((short) x, (short) y, (short) z, button == 1, (byte) id));
							}

							if(!this.isInMultiplayer() && EventManager.callEvent(new BlockPlaceEvent(this.level.openclassic.getBlockAt(x, y, z), OpenClassic.getClient().getPlayer(), this.heldBlock.block)).isCancelled()) {
								return;
							}

							this.level.netSetTile(x, y, z, id);
							this.heldBlock.heldPosition = 0;
							if(Blocks.fromId(id) != null && Blocks.fromId(id).getPhysics() != null) {
								Blocks.fromId(id).getPhysics().onPlace(this.level.openclassic.getBlockAt(x, y, z));
							}

							BlockType type = Blocks.fromId(id);
							if(type != null && type.getStepSound() != StepSound.NONE) {
								this.level.playSound(type.getStepSound().getSound(), x, y, z, (type.getStepSound().getVolume() + 1.0F) / 2.0F, type.getStepSound().getPitch() * 0.8F);
							}
						}
					}
				}
			}
		}
	}

	private void tick() {
		this.audio.update(this.player);
		((ClassicScheduler) OpenClassic.getGame().getScheduler()).tick(this.schedTicks);

		if(this.currentScreen != null) {
			this.lastClick = this.ticks + 10000;
		}

		if(this.currentScreen != null) {
			while(Mouse.next()) {
				if(this.currentScreen != null && Mouse.getEventButtonState()) {
					int x = Mouse.getEventX() * this.currentScreen.getWidth() / this.width;
					int y = this.currentScreen.getHeight() - Mouse.getEventY() * this.currentScreen.getHeight() / this.height - 1;
					this.currentScreen.onMouseClick(x, y, Mouse.getEventButton());
				}
			}

			if(this.currentScreen != null) {
				while(Keyboard.next()) {
					if(Keyboard.getEventKeyState()) {
						if(this.currentScreen != null) {
							this.currentScreen.onKeyPress(Keyboard.getEventCharacter(), Keyboard.getEventKey());
						}
					}
				}

				if(this.currentScreen != null) {
					this.currentScreen.update();
				}
			}
		}

		if(!this.ingame) return;
		if(System.currentTimeMillis() > this.audio.nextBGM && this.audio.playMusic("bg")) {
			this.audio.nextBGM = System.currentTimeMillis() + rand.nextInt(900000) + 300000L;
		}

		this.mode.spawnMobs();
		this.hud.ticks++;

		for(int index = 0; index < this.hud.chatHistory.size(); index++) {
			this.hud.chatHistory.get(index).time++;
		}
		
		for(int index = 0; index < this.textureManager.animations.size(); index++) {
			AnimatedTexture animation = this.textureManager.animations.get(index);
			animation.anaglyph = this.textureManager.settings.getBooleanSetting("options.3d-anaglyph").getValue();
			animation.animate();
		}

		if(this.isInMultiplayer()) {
			if(this.currentScreen instanceof ErrorScreen) {
				this.progressBar.setVisible(false);
			} else {
				if(!this.session.isConnected()) {
					this.progressBar.setVisible(true);
					this.progressBar.setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.multiplayer"));
					this.progressBar.setSubtitle(OpenClassic.getGame().getTranslator().translate("connecting.connect"));
					this.progressBar.setProgress(-1);
					this.progressBar.render();
				} else {
					if(this.session.connectSuccess()) {
						try {
							this.session.tick();
						} catch(Exception e) {
							e.printStackTrace();
							this.session.disconnect(e.toString());
							this.session = null;
						}
					}

					if(this.isInMultiplayer() && this.session.getState() == State.GAME) {
						this.session.send(new PlayerTeleportMessage((byte) -1, this.player.x, this.player.y, this.player.z, this.player.yaw, this.player.pitch));
					}
				}
			}
		}

		if(this.currentScreen == null && this.player != null && this.player.health <= 0) {
			this.setCurrentScreen(null);
		}

		while(Keyboard.next()) {
			this.player.setKey(Keyboard.getEventKey(), Keyboard.getEventKeyState());
			if(Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent()) {
				this.player.keyPress(Keyboard.getEventKey());
			}
			if(Keyboard.getEventKeyState()) {
				if(this.currentScreen != null) {
					if(Keyboard.getEventKeyState()) {
						this.currentScreen.onKeyPress(Keyboard.getEventCharacter(), Keyboard.getEventKey());
					}
				}

				if(Keyboard.getEventKey() == Keyboard.KEY_F6) {
					if(Display.isFullscreen()) {
						try {
							Display.setFullscreen(false);
							Display.setDisplayMode(new DisplayMode(854, 480));
						} catch(LWJGLException e) {
							e.printStackTrace();
						}
					} else {
						try {
							Display.setDisplayMode(Display.getDesktopDisplayMode());
							Display.setFullscreen(true);
						} catch(LWJGLException e) {
							e.printStackTrace();
						}
					}

					this.resize();
				}

				if(Keyboard.getEventKey() == Keyboard.KEY_F1) {
					this.hideGui = !this.hideGui;
				}

				if(this.ingame && (!this.isInMultiplayer() || this.session.isConnected() && this.session.getState() == State.GAME)) {
					if(Keyboard.getEventKey() == Keyboard.KEY_F2) {
						GL11.glReadBuffer(GL11.GL_FRONT);

						int width = Display.getWidth();
						int height = Display.getHeight();
						ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
						GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

						File file = new File(this.dir, "screenshots/" + (new Date(System.currentTimeMillis()).toString().replaceAll(" ", "-").replaceAll(":", "-")) + ".png");
						BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

						for(int x = 0; x < width; x++) {
							for(int y = 0; y < height; y++) {
								int i = (x + (width * y)) * 4;
								int r = buffer.get(i) & 0xFF;
								int g = buffer.get(i + 1) & 0xFF;
								int b = buffer.get(i + 2) & 0xFF;
								image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
							}
						}

						try {
							ImageIO.write(image, "PNG", file);
							if(this.hud != null) this.player.openclassic.sendMessage("screenshot.saved", file.getName());
						} catch(IOException e) {
							e.printStackTrace();
							if(this.hud != null) this.player.openclassic.sendMessage("screenshot.error", file.getName());
						}
					}

					if(this.currentScreen == null || !this.currentScreen.grabsInput()) {
						if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
							this.displayMenu();
						}

						if(this.mode instanceof CreativeGameMode) {
							if(Keyboard.getEventKey() == this.bindings.loadLocKey.key) {
								PlayerRespawnEvent event = new PlayerRespawnEvent(OpenClassic.getClient().getPlayer(), new Position(OpenClassic.getClient().getLevel(), this.level.xSpawn + 0.5F, this.level.ySpawn, this.level.zSpawn + 0.5F, this.level.yawSpawn, this.level.pitchSpawn));
								if(!event.isCancelled()) {
									this.player.resetPos(event.getPosition());
								}
							}

							if(Keyboard.getEventKey() == this.bindings.saveLocKey.key) {
								this.level.setSpawnPos(this.player.x, this.player.y, this.player.z, this.player.yaw, this.player.pitch);
								this.player.resetPos();
							}
						}

						Keyboard.getEventKey();
						if(Keyboard.getEventKey() == Keyboard.KEY_F5) {
							this.raining = !this.raining;
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_TAB && this.mode instanceof SurvivalGameMode && this.player.arrows > 0) {
							this.level.addEntity(new Arrow(this.level, this.player, this.player.x, this.player.y, this.player.z, this.player.yaw, this.player.pitch, 1.2F));
							this.player.arrows--;
						}

						if(Keyboard.getEventKey() == this.bindings.buildKey.key) {
							this.mode.openInventory();
						}

						if(Keyboard.getEventKey() == this.bindings.chatKey.key) {
							this.player.releaseAllKeys();
							this.setCurrentScreen(new ChatInputScreen());
						}
					}
				}

				for(int selection = 0; selection < 9; selection++) {
					if(Keyboard.getEventKey() == selection + 2) {
						this.player.inventory.selected = selection;
					}
				}

				if(Keyboard.getEventKey() == this.bindings.fogKey.key) {
					this.settings.getSetting("options.render-distance").toggle();
				}
			}

			EventManager.callEvent(new PlayerKeyChangeEvent(OpenClassic.getClient().getPlayer(), Keyboard.getEventKey(), Keyboard.getEventKeyState()));
			if(this.isInMultiplayer() && this.session.isConnected() && this.openclassicServer) {
				this.session.send(new KeyChangeMessage(Keyboard.getEventKey(), Keyboard.getEventKeyState()));
			}
		}

		if(this.currentScreen == null || !this.currentScreen.grabsInput()) {
			while(Mouse.next()) {
				if(Mouse.getEventDWheel() != 0) {
					this.player.inventory.swapPaint(Mouse.getEventDWheel());
				}

				if(this.currentScreen == null) {
					if(!Mouse.isGrabbed() && Mouse.getEventButtonState()) {
						this.grabMouse();
					} else {
						if(Mouse.getEventButtonState()) {
							if(Mouse.getEventButton() == 0) {
								this.onMouseClick(0);
								this.lastClick = this.ticks;
							}

							if(Mouse.getEventButton() == 1) {
								this.onMouseClick(1);
								this.lastClick = this.ticks;
							}

							if(Mouse.getEventButton() == 2 && this.selected != null) {
								int block = this.level.getTile(this.selected.x, this.selected.y, this.selected.z);
								if(block == VanillaBlock.GRASS.getId()) {
									block = VanillaBlock.DIRT.getId();
								}

								if(block == VanillaBlock.DOUBLE_SLAB.getId()) {
									block = VanillaBlock.SLAB.getId();
								}

								if(block == VanillaBlock.BEDROCK.getId()) {
									block = VanillaBlock.STONE.getId();
								}

								this.player.inventory.grabTexture(block, this.mode instanceof CreativeGameMode);
							}
						}
					}
				}

				if(this.currentScreen != null) {
					if(Mouse.getEventButtonState()) {
						int x = Mouse.getEventX() * this.currentScreen.getWidth() / this.width;
						int y = this.currentScreen.getHeight() - Mouse.getEventY() * this.currentScreen.getHeight() / this.height - 1;
						this.currentScreen.onMouseClick(x, y, Mouse.getEventButton());
					}
				}
			}

			if(this.blockHitTime > 0) {
				this.blockHitTime--;
			}

			if(this.currentScreen == null) {
				if(Mouse.isButtonDown(0) && (this.ticks - this.lastClick) >= this.timer.tps / 4 && Mouse.isGrabbed()) {
					this.onMouseClick(0);
					this.lastClick = this.ticks;
				}

				if(Mouse.isButtonDown(1) && (this.ticks - this.lastClick) >= this.timer.tps / 4 && Mouse.isGrabbed()) {
					this.onMouseClick(1);
					this.lastClick = this.ticks;
				}
			}

			if(!this.mode.creative && this.blockHitTime <= 0) {
				if(this.currentScreen == null && Mouse.isButtonDown(0) && Mouse.isGrabbed() && this.selected != null && !this.selected.entityPos) {
					this.mode.hitBlock(this.selected.x, this.selected.y, this.selected.z, this.selected.side);
				} else {
					this.mode.resetHits();
				}
			}
		}

		if(this.level != null) {
			this.rainTicks++;
			this.heldBlock.lastPosition = this.heldBlock.heldPosition;
			if(this.heldBlock.moving) {
				this.heldBlock.heldOffset++;
				if(this.heldBlock.heldOffset == 7) {
					this.heldBlock.heldOffset = 0;
					this.heldBlock.moving = false;
				}
			}

			int id = this.player.inventory.getSelected();
			BlockType block = null;
			if(id > 0) {
				block = Blocks.fromId(id);
			}

			block = this.player.openclassic != null && this.player.openclassic.getPlaceMode() != 0 ? Blocks.fromId(this.player.openclassic.getPlaceMode()) : block;

			float position = (block == this.heldBlock.block ? 1.0F : 0.0F) - this.heldBlock.heldPosition;
			if(position < -0.4F) {
				position = -0.4F;
			}

			if(position > 0.4F) {
				position = 0.4F;
			}

			this.heldBlock.heldPosition += position;
			if(this.heldBlock.heldPosition < 0.1F) {
				this.heldBlock.block = block;
			}

			if(this.raining) {
				for(int count = 0; count < 50; count++) {
					int x = (int) this.player.x + rand.nextInt(9) - 4;
					int z = (int) this.player.z + rand.nextInt(9) - 4;
					int y = this.level.getHighestTile(x, z);
					if(y <= (int) this.player.y + 4 && y >= (int) this.player.y - 4) {
						float xOffset = rand.nextFloat();
						float zOffset = rand.nextFloat();
						this.particleManager.spawnParticle(new WaterDropParticle(this.level, x + xOffset, y + 0.1F, z + zOffset));
					}
				}
			}

			this.levelRenderer.ticks++;
			this.level.tickEntities();
			if(!this.isInMultiplayer()) {
				this.level.tick();
			}

			this.particleManager.tickParticles();
		}

		for(Plugin plugin : OpenClassic.getClient().getPluginManager().getPlugins()) {
			plugin.tick();
		}

		this.schedTicks++;
	}

	private void resize() {
		this.width = Display.getWidth();
		this.height = Display.getHeight();

		if(this.hud != null) {
			this.hud.width = ClientRenderHelper.getHelper().getGuiWidth();
			this.hud.height = ClientRenderHelper.getHelper().getGuiHeight();
		}

		if(this.currentScreen != null) {
			this.currentScreen.setSize(this.width, this.height);
		}
	}

	public boolean isInMultiplayer() {
		return this.session != null;
	}

	public void setLevel(Level level) {
		this.level = level;
		if(level != null) {
			level.initTransient();
			this.mode.apply(level);
			level.font = this.fontRenderer;
			level.rendererContext = this;
			if(this.isInMultiplayer() && this.player != null) {
				this.player.resetPos();
				this.mode.preparePlayer(this.player);
				level.player = this.player;
				level.addEntity(this.player);
			}
		}

		if(this.player == null) {
			this.player = new LocalPlayer(level);
			this.player.resetPos();
			this.mode.preparePlayer(this.player);
			if(level != null) {
				level.player = this.player;
			}
		}

		if(this.player != null) {
			this.player.input = new InputHandler(this.bindings);
			this.mode.apply(this.player);
		}

		if(this.levelRenderer != null) {
			if(this.levelRenderer.level != null) {
				this.levelRenderer.level.rendererContext = null;
			}

			this.levelRenderer.level = level;
			if(level != null) {
				this.levelRenderer.refresh();
			}
		}

		if(this.particleManager != null) {
			if(level != null) {
				level.particleEngine = this.particleManager;
			}

			for(int particle = 0; particle < 2; particle++) {
				this.particleManager.particles[particle].clear();
			}
		}
	}

	public List<String> getPlayers() {
		ArrayList<String> players = new ArrayList<String>();
		players.add(this.data != null ? this.data.username : "Player");
		if(this.isInMultiplayer()) {
			for(NetworkPlayer player : this.netPlayers.values()) {
				players.add(player.name);
			}
		}

		return players;
	}
}
