package com.mojang.minecraft;

import java.awt.Canvas;
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
import org.lwjgl.util.glu.GLU;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.Model;
import ch.spacebase.openclassic.api.event.block.BlockPlaceEvent;
import ch.spacebase.openclassic.api.event.player.PlayerKeyChangeEvent;
import ch.spacebase.openclassic.api.event.player.PlayerQuitEvent;
import ch.spacebase.openclassic.api.event.player.PlayerRespawnEvent;
import ch.spacebase.openclassic.api.gui.GuiComponent;
import ch.spacebase.openclassic.api.math.BoundingBox;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.api.plugin.Plugin;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;
import ch.spacebase.openclassic.api.settings.BooleanSetting;
import ch.spacebase.openclassic.api.settings.IntSetting;
import ch.spacebase.openclassic.api.settings.Settings;
import ch.spacebase.openclassic.api.settings.bindings.Bindings;
import ch.spacebase.openclassic.api.settings.bindings.KeyBinding;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.ClientProgressBar;
import ch.spacebase.openclassic.client.gui.ChatInputScreen;
import ch.spacebase.openclassic.client.gui.ErrorScreen;
import ch.spacebase.openclassic.client.gui.GameOverScreen;
import ch.spacebase.openclassic.client.gui.LoginScreen;
import ch.spacebase.openclassic.client.gui.MainMenuScreen;
import ch.spacebase.openclassic.client.gui.IngameMenuScreen;
import ch.spacebase.openclassic.client.gui.hud.ClientHUDScreen;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.render.RenderHelper;
import ch.spacebase.openclassic.client.render.Renderer;
import ch.spacebase.openclassic.client.settings.MinimapSetting;
import ch.spacebase.openclassic.client.settings.MusicSetting;
import ch.spacebase.openclassic.client.settings.NightSetting;
import ch.spacebase.openclassic.client.settings.SurvivalSetting;
import ch.spacebase.openclassic.client.settings.TextureRefreshSetting;
import ch.spacebase.openclassic.client.sound.ClientAudioManager;
import ch.spacebase.openclassic.client.util.BlockUtils;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.client.util.LWJGLNatives;
import ch.spacebase.openclassic.client.util.ShaderManager;
import ch.spacebase.openclassic.game.network.ClassicSession.State;
import ch.spacebase.openclassic.game.network.msg.PlayerSetBlockMessage;
import ch.spacebase.openclassic.game.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.game.network.msg.custom.KeyChangeMessage;
import ch.spacebase.openclassic.game.scheduler.ClassicScheduler;
import ch.spacebase.openclassic.game.util.InternalConstants;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.item.Arrow;
import com.mojang.minecraft.entity.item.Item;
import com.mojang.minecraft.entity.model.ModelPart;
import com.mojang.minecraft.entity.model.Vector;
import com.mojang.minecraft.entity.particle.ParticleManager;
import com.mojang.minecraft.entity.particle.RainParticle;
import com.mojang.minecraft.entity.player.InputHandler;
import com.mojang.minecraft.entity.player.LocalPlayer;
import com.mojang.minecraft.entity.player.net.NetworkPlayer;
import com.mojang.minecraft.gamemode.CreativeGameMode;
import com.mojang.minecraft.gamemode.GameMode;
import com.mojang.minecraft.gamemode.SurvivalGameMode;
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
import com.mojang.minecraft.util.Intersection;
import com.mojang.minecraft.util.Timer;
import com.zachsthings.onevent.EventManager;

public class Minecraft implements Runnable {

	private static final Random rand = new Random();

	public GameMode mode;
	public int width;
	public int height;
	public Timer timer = new Timer(InternalConstants.TICKS_PER_SECOND);
	public ClientLevel level;
	public LevelRenderer levelRenderer;
	public LocalPlayer player;
	public ParticleManager particleManager;
	public Canvas canvas;
	public TextureManager textureManager;
	public FontRenderer fontRenderer;
	public ClientProgressBar progressBar = new ClientProgressBar();
	public FogRenderer fogRenderer = new FogRenderer(this);
	public ClientAudioManager audio;
	private int ticks;
	private int blockHitTime;
	public ClientHUDScreen hud;
	public Intersection selected;
	public String server;
	public int port;
	public volatile boolean running;
	private int lastClick;
	public boolean raining;
	public File dir;
	public boolean ingame;
	private boolean shutdown = false;

	public boolean openclassicServer = false;
	public String openclassicVersion = "";
	public List<RemotePluginInfo> serverPlugins = new ArrayList<RemotePluginInfo>();
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
	public String tempKey = null;
	public ClientPlayer ocPlayer;
	public GuiComponent baseGUI;
	private boolean init = false;
	private int waterDelay = 200;

	public Minecraft(Canvas canvas, int width, int height) {
		this.ticks = 0;
		this.blockHitTime = 0;
		this.selected = null;
		this.server = null;
		this.port = 0;
		this.running = false;
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
	}
	
	public boolean isInMultiplayer() {
		return this.session != null;
	}
	
	private void resize() {
		this.width = Display.getWidth();
		this.height = Display.getHeight();

		if(this.hud != null) {
			this.hud.setSize(this.width, this.height);
			this.hud.clearComponents();
			this.hud.onAttached(this.hud.getParent());
		}
		
		this.baseGUI.setSize(this.width, this.height);
		GuiComponent component = OpenClassic.getClient().getActiveComponent();
		if(component != null) {
			component.clearComponents();
			component.onAttached(component.getParent());
		}
	}

	public void setLevel(ClientLevel level) {
		this.level = level;
		if(level != null) {
			this.mode.apply(level);
			if(this.isInMultiplayer() && this.player != null) {
				this.player.resetPos();
				this.mode.preparePlayer(this.player);
				level.addEntity(this.player);
			}
		}

		if(this.player == null) {
			this.player = new LocalPlayer(level, this.ocPlayer);
			this.ocPlayer.setHandle(this.player);
			this.player.resetPos();
			this.mode.preparePlayer(this.player);
		}

		if(this.player != null) {
			this.player.input = new InputHandler(this.bindings);
			this.mode.apply(this.player);
		}

		if(this.levelRenderer != null) {
			if(this.levelRenderer.level != level) {
				this.levelRenderer.level = level;
			}
			
			if(level != null) {
				this.levelRenderer.refresh();
			}
		}

		if(this.particleManager != null) {
			for(int particle = 0; particle < 2; particle++) {
				this.particleManager.particles[particle].clear();
			}
		}
	}
	
	public void initGame() {
		this.audio.stopMusic();
		this.audio.setMusicTime(System.currentTimeMillis() + rand.nextInt(900000));
		if(this.level == null) {
			ClientLevel level = new ClientLevel();
			level.setData(8, 8, 8, new byte[512]);
			this.setLevel(level);
		}
		
		if(this.server != null) {
			this.session = new ClientSession(this.ocPlayer, this.tempKey, this.server, this.port);
			this.tempKey = null;
		}

		this.particleManager = new ParticleManager(this.textureManager);
		this.hud = new ClientHUDScreen();
		this.hud.onAttached(null);
		this.mode = this.settings.getIntSetting("options.survival").getValue() > 0 && !this.isInMultiplayer() ? new SurvivalGameMode(this) : new CreativeGameMode(this);
		if(this.level != null) {
			this.mode.apply(this.level);
			this.mode.apply(this.player);
		}
		
		this.ingame = true;
	}

	public void stopGame(boolean menu) {
		this.audio.stopMusic();
		this.serverPlugins.clear();
		if(menu) OpenClassic.getClient().setActiveComponent(new MainMenuScreen());
		this.level = null;
		this.particleManager = null;
		this.hud = null;
		if(this.player != null && this.ocPlayer.getData() != null && !this.isInMultiplayer()) {
			this.ocPlayer.getData().save(OpenClassic.getClient().getDirectory().getPath() + "/player.nbt");
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
		this.ocPlayer.setHandle(null);
		this.hacks = true;
	}
	
	public final void shutdown() {
		if(this.shutdown) {
			return;
		}

		this.shutdown = true;
		this.running = false;
		if(this.ingame) this.stopGame(false);
		OpenClassic.getClient().unregisterExecutors(OpenClassic.getClient());
		((ClassicScheduler) OpenClassic.getClient().getScheduler()).shutdown();
		this.audio.cleanup();
		OpenClassic.setGame(null);
		this.destroyRender();
		System.exit(0);
	}

	private void handleException(Throwable e) {
		e.printStackTrace();
		if(!this.running) {
			return;
		} 

		if(this.init && !(e instanceof LWJGLException) && !(e instanceof RuntimeException)) {
			this.progressBar.setVisible(false);
			OpenClassic.getClient().setActiveComponent(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("core.client-error"), String.format(OpenClassic.getGame().getTranslator().translate("core.game-broke"), e)));
		} else {
			String msg = "Exception occured";
			if(OpenClassic.getGame() != null && OpenClassic.getGame().getTranslator() != null) {
				msg = OpenClassic.getGame().getTranslator().translate("core.exception");
			}

			JOptionPane.showMessageDialog(null, "See .minecraft_classic/client.log for more details.\n" + e.toString(), msg, 0);
			System.exit(0);
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

		this.ocPlayer = new ClientPlayer();
		this.audio = new ClientAudioManager(this);
		this.bindings = new Bindings();
		this.bindings.registerBinding(new KeyBinding("options.keys.forward", Keyboard.KEY_W));
		this.bindings.registerBinding(new KeyBinding("options.keys.left", Keyboard.KEY_A));
		this.bindings.registerBinding(new KeyBinding("options.keys.back", Keyboard.KEY_S));
		this.bindings.registerBinding(new KeyBinding("options.keys.right", Keyboard.KEY_D));
		this.bindings.registerBinding(new KeyBinding("options.keys.jump", Keyboard.KEY_SPACE));
		this.bindings.registerBinding(new KeyBinding("options.keys.blocks", Keyboard.KEY_B));
		this.bindings.registerBinding(new KeyBinding("options.keys.chat", Keyboard.KEY_T));
		this.bindings.registerBinding(new KeyBinding("options.keys.toggle-fog", Keyboard.KEY_F));
		this.bindings.registerBinding(new KeyBinding("options.keys.save-loc", Keyboard.KEY_RETURN));
		this.bindings.registerBinding(new KeyBinding("options.keys.load-loc", Keyboard.KEY_R));
		this.bindings.registerBinding(new KeyBinding("options.keys.speedhack", Keyboard.KEY_LCONTROL));
		this.bindings.registerBinding(new KeyBinding("options.keys.fly-down", Keyboard.KEY_LSHIFT));
		this.settings = new Settings();
		this.settings.registerSetting(new MusicSetting("options.music"));
		this.settings.getBooleanSetting("options.music").setDefault(true);
		this.settings.registerSetting(new BooleanSetting("options.sound"));
		this.settings.getBooleanSetting("options.sound").setDefault(true);
		this.settings.registerSetting(new BooleanSetting("options.invert-mouse"));
		this.settings.registerSetting(new BooleanSetting("options.show-info"));
		this.settings.registerSetting(new IntSetting("options.render-distance", new String[] { "FAR", "NORMAL", "SHORT", "TINY" }));
		this.settings.registerSetting(new BooleanSetting("options.view-bobbing"));
		this.settings.getBooleanSetting("options.view-bobbing").setDefault(true);
		this.settings.registerSetting(new TextureRefreshSetting("options.3d-anaglyph"));
		this.settings.registerSetting(new BooleanSetting("options.limit-fps"));
		this.settings.registerSetting(new SurvivalSetting("options.survival", new String[] { "OFF", "PEACEFUL", "NORMAL" }));
		this.settings.registerSetting(new TextureRefreshSetting("options.smoothing"));
		this.settings.registerSetting(new NightSetting("options.night"));
		this.settings.registerSetting(new IntSetting("options.sensitivity", new String[] { "SLOW", "NORMAL", "FAST", "FASTER", "FASTEST" }));
		this.settings.getIntSetting("options.sensitivity").setDefault(1);
		this.settings.registerSetting(new MinimapSetting("options.minimap"));
		
		this.hackSettings = new Settings();
		this.hackSettings.registerSetting(new BooleanSetting("hacks.speed"));
		this.hackSettings.registerSetting(new BooleanSetting("hacks.flying"));
		OpenClassic.getClient().getConfig().applyDefault("options.texture-pack", "none");
		OpenClassic.getClient().getConfig().save();
		
		this.mode = this.settings.getIntSetting("options.survival").getValue() > 0 ? new SurvivalGameMode(this) : new CreativeGameMode(this);
		Item.initModels();
		this.initRender();

		((ClassicClient) OpenClassic.getClient()).init();
		this.baseGUI = new GuiComponent("base", 0, 0, Display.getWidth(), Display.getHeight());
		this.baseGUI.setFocused(true);
		this.init = true;
		this.lastUpdate = System.currentTimeMillis();
		this.fps = 0;
		if(this.server == null || this.server.equals("") || this.port == 0) {
			OpenClassic.getClient().setActiveComponent(new LoginScreen());
		} else {
			this.initGame();
		}
		
		while(this.running) {
			this.timer.update();
			for(int tick = 0; tick < this.timer.elapsedTicks; tick++) {
				this.ticks++;
				this.tick();
			}

			if(!this.render()) {
				break;
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
		} catch(LWJGLException e) {
			this.handleException(e);
			return;
		}

		try {
			Controllers.create();
		} catch(LWJGLException e) {
			e.printStackTrace();
		}

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
		
		RenderHelper.getHelper().init();
		this.textureManager = new TextureManager(this.settings);
		this.textureManager.addAnimatedTexture((new com.mojang.minecraft.render.animation.LavaTexture()));
		this.textureManager.addAnimatedTexture((new com.mojang.minecraft.render.animation.WaterTexture()));
		this.fontRenderer = new FontRenderer("/textures/gui/font.png", this.textureManager);
		this.levelRenderer = new LevelRenderer();
		ShaderManager.setup();
		GL11.glViewport(0, 0, this.width, this.height);
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
		
		for(int index = 0; index < this.textureManager.animations.size(); index++) {
			RenderHelper.getHelper().bindTexture("/textures/level/terrain.png", true);
			AnimatedTexture animation = this.textureManager.animations.get(index);
			ByteBuffer buffer = BufferUtils.createByteBuffer(animation.textureData.length);
			buffer.put(animation.textureData);
			buffer.flip();
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, (animation.textureId % 16) * 16, (animation.textureId / 16) * 16, 16, 16, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
			if(animation instanceof WaterTexture) {
				RenderHelper.getHelper().bindTexture("/textures/level/water.png", true);
				GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 16, 16, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
			}
		}

		if(this.level != null) {
			float pitch = this.player.oPitch + (this.player.pitch - this.player.oPitch) * this.timer.delta;
			float yaw = this.player.oYaw + (this.player.yaw - this.player.oYaw) * this.timer.delta;
			Vector pVec = RenderHelper.getHelper().getPlayerVector(this.player, this.timer.delta);
			float ycos = MathHelper.cos(-yaw * MathHelper.DEG_TO_RAD - MathHelper.PI);
			float ysin = MathHelper.sin(-yaw * MathHelper.DEG_TO_RAD - MathHelper.PI);
			float pcos = MathHelper.cos(-pitch * MathHelper.DEG_TO_RAD);
			float psin = MathHelper.sin(-pitch * MathHelper.DEG_TO_RAD);
			float mx = ysin * pcos;
			float mz = ycos * pcos;
			float reach = this.mode.getReachDistance();
			this.selected = this.level.clip(pVec, pVec.add(mx * reach, psin * reach, mz * reach), true);
			if(this.selected != null) {
				reach = this.selected.pos.distance(RenderHelper.getHelper().getPlayerVector(this.player, this.timer.delta));
			}

			pVec = RenderHelper.getHelper().getPlayerVector(this.player, this.timer.delta);
			if(this.mode instanceof CreativeGameMode) {
				reach = 32;
			}

			Entity selectedEntity = null;
			List<Entity> entities = this.level.getBlockMap().getEntities(this.player, this.player.bb.expand(mx * reach, psin * reach, mz * reach));

			float distance = 0;
			for(int count = 0; count < entities.size(); count++) {
				Entity entity = entities.get(count);
				if(entity.isPickable()) {
					Intersection pos = BlockUtils.clip(entity.bb.grow(0.1F, 0.1F, 0.1F), pVec, pVec.add(mx * reach, psin * reach, mz * reach));
					if(pos != null && (pVec.distance(pos.pos) < distance || distance == 0)) {
						selectedEntity = entity;
						distance = pVec.distance(pos.pos);
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
				float fogDensity = 1 - (float) Math.pow(1F / (4 - this.settings.getIntSetting("options.render-distance").getValue()), 0.25);
				float skyRed = (this.level.getSkyColor() >> 16 & 255) / 255F;
				float skyGreen = (this.level.getSkyColor() >> 8 & 255) / 255F;
				float skyBlue = (this.level.getSkyColor() & 255) / 255F;
				this.fogRenderer.fogRed = (this.level.getFogColor() >> 16 & 255) / 255F;
				this.fogRenderer.fogGreen = (this.level.getFogColor() >> 8 & 255) / 255F;
				this.fogRenderer.fogBlue = (this.level.getFogColor() & 255) / 255F;
				this.fogRenderer.fogRed += (skyRed - this.fogRenderer.fogRed) * fogDensity;
				this.fogRenderer.fogGreen += (skyGreen - this.fogRenderer.fogGreen) * fogDensity;
				this.fogRenderer.fogBlue += (skyBlue - this.fogRenderer.fogBlue) * fogDensity;
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
				GL11.glEnable(GL11.GL_CULL_FACE);
				this.fogRenderer.fogEnd = (512 >> (this.settings.getIntSetting("options.render-distance").getValue() << 1));
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					GL11.glTranslatef((-((pass << 1) - 1)) * 0.07F, 0, 0);
				}

				float fov = 70;
				if(this.player.health <= 0) {
					fov /= (1 - 500 / (this.player.deathTime + this.timer.delta + 500)) * 2 + 1;
				}

				GLU.gluPerspective(fov, (float) this.width / (float) this.height, 0.05F, this.fogRenderer.fogEnd);
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glLoadIdentity();
				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					GL11.glTranslatef(((pass << 1) - 1) * 0.1F, 0, 0);
				}

				RenderHelper.getHelper().hurtEffect(this.player, this.timer.delta);
				if(this.settings.getBooleanSetting("options.view-bobbing").getValue()) {
					RenderHelper.getHelper().applyBobbing(this.player, this.timer.delta);
				}

				GL11.glTranslatef(0, 0, -0.1F);
				GL11.glRotatef(this.player.oPitch + (this.player.pitch - this.player.oPitch) * this.timer.delta, 1, 0, 0);
				GL11.glRotatef(this.player.oYaw + (this.player.yaw - this.player.oYaw) * this.timer.delta, 0, 1, 0);
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
				this.levelRenderer.sortAndRender(this.player, 0);
				if(BlockUtils.preventsRendering(this.level, this.player.x, this.player.y, this.player.z, 0.1F)) {
					for(int bx = (int) this.player.x - 1; bx <= (int) this.player.x + 1; bx++) {
						for(int by = (int) this.player.y - 1; by <= (int) this.player.y + 1; by++) {
							for(int bz = (int) this.player.z - 1; bz <= (int) this.player.z + 1; bz++) {
								BlockType block = this.levelRenderer.level.getBlockTypeAt(bx, by, bz);
								if(block != null && block.getPreventsRendering()) {
									GL11.glColor4f(0.2F, 0.2F, 0.2F, 1);
									GL11.glDepthFunc(GL11.GL_LESS);
									block.getModel(this.level, bx, by, bz).renderAll(bx, by, bz, 0.2F);
									GL11.glCullFace(GL11.GL_FRONT);
									block.getModel(this.level, bx, by, bz).renderAll(bx, by, bz, 0.2F);
									GL11.glCullFace(GL11.GL_BACK);
									GL11.glDepthFunc(GL11.GL_LEQUAL);
								}
							}
						}
					}
				}

				RenderHelper.getHelper().setLighting(true);
				this.levelRenderer.level.getBlockMap().render(RenderHelper.getHelper().getPlayerVector(this.player, this.timer.delta), this.textureManager, this.timer.delta);
				RenderHelper.getHelper().setLighting(false);
				this.fogRenderer.updateFog();
				float xmod = -MathHelper.cos(this.player.yaw * MathHelper.DEG_TO_RAD);
				float zmod = -MathHelper.sin(this.player.yaw * MathHelper.DEG_TO_RAD);
				float xdir = -zmod * MathHelper.sin(this.player.pitch * MathHelper.DEG_TO_RAD);
				float zdir = xmod * MathHelper.sin(this.player.pitch * MathHelper.DEG_TO_RAD);
				float ymod = MathHelper.cos(this.player.pitch * MathHelper.DEG_TO_RAD);

				for(int texture = 0; texture < 2; texture++) {
					if(this.particleManager.particles[texture].size() != 0) {
						int textureId = 0;
						if(texture == 0) {
							textureId = this.textureManager.bindTexture("/textures/level/particles.png");
						}

						if(texture == 1) {
							textureId = this.textureManager.bindTexture("/textures/level/terrain.png");
						}

						RenderHelper.getHelper().bindTexture(textureId);
						Renderer.get().begin();
						for(int count = 0; count < this.particleManager.particles[texture].size(); count++) {
							this.particleManager.particles[texture].get(count).render(this.timer.delta, xmod, ymod, zmod, xdir, zdir);
						}

						Renderer.get().end();
					}
				}

				RenderHelper.getHelper().bindTexture("/textures/level/rock.png", true);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glCallList(this.levelRenderer.boundaryList);
				this.fogRenderer.updateFog();
				RenderHelper.getHelper().bindTexture("/textures/level/clouds.png", true);
				GL11.glColor4f(1, 1, 1, 1);
				float cloudRed = (this.levelRenderer.level.getCloudColor() >> 16 & 255) / 255F;
				float cloudBlue = (this.levelRenderer.level.getCloudColor() >> 8 & 255) / 255F;
				float cloudGreen = (this.levelRenderer.level.getCloudColor() & 255) / 255F;
				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					cloudRed = (cloudRed * 30 + cloudBlue * 59 + cloudGreen * 11) / 100;
					cloudBlue = (cloudRed * 30 + cloudBlue * 70) / 100;
					cloudGreen = (cloudRed * 30 + cloudGreen * 70) / 100;
				}

				float texCoordMod = 1 / 2048f;
				float cloudHeight = (this.levelRenderer.level.getHeight() + 2);
				float movement = (this.levelRenderer.ticks + this.timer.delta) * texCoordMod * 0.03F;
				Renderer.get().begin();
				Renderer.get().color(cloudRed, cloudBlue, cloudGreen);

				for(int x = -2048; x < this.levelRenderer.level.getWidth() + 2048; x += 512) {
					for(int z = -2048; z < this.levelRenderer.level.getDepth() + 2048; z += 512) {
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
				float skRed = (this.levelRenderer.level.getSkyColor() >> 16 & 255) / 255F;
				float skBlue = (this.levelRenderer.level.getSkyColor() >> 8 & 255) / 255F;
				float skGreen = (this.levelRenderer.level.getSkyColor() & 255) / 255F;
				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					skRed = (skRed * 30 + skBlue * 59 + skGreen * 11) / 100;
					skBlue = (skRed * 30 + skBlue * 70) / 100;
					skGreen = (skRed * 30 + skGreen * 70) / 100;
				}

				Renderer.get().color(skRed, skBlue, skGreen);
				float skyHeight = (this.levelRenderer.level.getHeight() + 10);

				for(int x = -2048; x < this.levelRenderer.level.getWidth() + 2048; x += 512) {
					for(int z = -2048; z < this.levelRenderer.level.getDepth() + 2048; z += 512) {
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
					GL11.glColor4f(1, 1, 1, (MathHelper.sin(System.currentTimeMillis() / 100F) * 0.2F + 0.4F) * 0.5F);
					if(this.levelRenderer.cracks > 0) {
						GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
						GL11.glColor4f(1, 1, 1, 0.5F);
						GL11.glPushMatrix();
						BlockType btype = this.levelRenderer.level.getBlockTypeAt(this.selected.x, this.selected.y, this.selected.z);
						GL11.glDepthMask(false);
						if(btype == null) {
							btype = VanillaBlock.STONE;
						}

						Model model = btype.getModel(this.level, this.selected.x, this.selected.y, this.selected.z);
						for(int count = 0; count < model.getQuads().size(); count++) {
							RenderHelper.getHelper().drawCracks(model.getQuad(count), this.selected.x, this.selected.y, this.selected.z, 240 + (int) (this.levelRenderer.cracks * 10));
						}

						GL11.glDepthMask(true);
						GL11.glPopMatrix();
					}

					GL11.glDisable(GL11.GL_BLEND);
					GL11.glDisable(GL11.GL_ALPHA_TEST);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					GL11.glColor4f(0, 0, 0, 0.4F);
					GL11.glLineWidth(2);
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					GL11.glDepthMask(false);
					BlockType block = this.levelRenderer.level.getBlockTypeAt(this.selected.x, this.selected.y, this.selected.z);
					if(block != null) {
						BoundingBox bb = block.getModel(this.level, this.selected.x, this.selected.y, this.selected.z).getSelectionBox(this.selected.x, this.selected.y, this.selected.z).grow(0.002F, 0.002F, 0.002F);
						GL11.glBegin(GL11.GL_LINE_STRIP);
						GL11.glVertex3f(bb.getX1(), bb.getY1(), bb.getZ1());
						GL11.glVertex3f(bb.getX2(), bb.getY1(), bb.getZ1());
						GL11.glVertex3f(bb.getX2(), bb.getY1(), bb.getZ2());
						GL11.glVertex3f(bb.getX1(), bb.getY1(), bb.getZ2());
						GL11.glVertex3f(bb.getX1(), bb.getY1(), bb.getZ1());
						GL11.glEnd();
						GL11.glBegin(GL11.GL_LINE_STRIP);
						GL11.glVertex3f(bb.getX1(), bb.getY2(), bb.getZ1());
						GL11.glVertex3f(bb.getX2(), bb.getY2(), bb.getZ1());
						GL11.glVertex3f(bb.getX2(), bb.getY2(), bb.getZ2());
						GL11.glVertex3f(bb.getX1(), bb.getY2(), bb.getZ2());
						GL11.glVertex3f(bb.getX1(), bb.getY2(), bb.getZ1());
						GL11.glEnd();
						GL11.glBegin(GL11.GL_LINES);
						GL11.glVertex3f(bb.getX1(), bb.getY1(), bb.getZ1());
						GL11.glVertex3f(bb.getX1(), bb.getY2(), bb.getZ1());
						GL11.glVertex3f(bb.getX2(), bb.getY1(), bb.getZ1());
						GL11.glVertex3f(bb.getX2(), bb.getY2(), bb.getZ1());
						GL11.glVertex3f(bb.getX2(), bb.getY1(), bb.getZ2());
						GL11.glVertex3f(bb.getX2(), bb.getY2(), bb.getZ2());
						GL11.glVertex3f(bb.getX1(), bb.getY1(), bb.getZ2());
						GL11.glVertex3f(bb.getX1(), bb.getY2(), bb.getZ2());
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
				RenderHelper.getHelper().bindTexture("/textures/level/water.png", true);
				GL11.glCallList(this.levelRenderer.boundaryList + 1);
				RenderHelper.getHelper().bindTexture("/textures/level/terrain.png", true);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glColorMask(false, false, false, false);
				int cy = this.levelRenderer.sortAndRender(this.player, 1);
				GL11.glColorMask(true, true, true, true);
				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					if(pass == 0) {
						GL11.glColorMask(false, true, true, false);
					} else {
						GL11.glColorMask(true, false, false, false);
					}
				}

				if(cy > 0) {
					GL11.glCallLists(this.levelRenderer.listBuffer);
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
					RenderHelper.getHelper().bindTexture("/textures/level/rain.png", true);

					for(int cx = x - 5; cx <= x + 5; cx++) {
						for(int cz = z - 5; cz <= z + 5; cz++) {
							cy = this.level.getHighestBlockY(cx, cz);
							int minRY = y - 5;
							int maxRY = y + 5;
							if(minRY < cy) {
								minRY = cy;
							}

							if(maxRY < cy) {
								maxRY = cy;
							}

							if(minRY != maxRY) {
								float downfall = (((this.rainTicks + cx * 3121 + cz * 418711) % 32) + this.timer.delta) / 32F;
								float rax = cx + 0.5F - this.player.x;
								float raz = cz + 0.5F - this.player.z;
								float visibility = (float) Math.sqrt(rax * rax + raz * raz) / 5;
								GL11.glColor4f(1, 1, 1, (1 - visibility * visibility) * 0.7F);
								Renderer.get().begin();
								Renderer.get().vertexuv(cx, minRY, cz, 0, minRY * 2 / 8F + downfall * 2);
								Renderer.get().vertexuv((cx + 1), minRY, (cz + 1), 2, minRY * 2 / 8F + downfall * 2);
								Renderer.get().vertexuv((cx + 1), maxRY, (cz + 1), 2, maxRY * 2 / 8F + downfall * 2);
								Renderer.get().vertexuv(cx, maxRY, cz, 0, maxRY * 2 / 8F + downfall * 2);
								Renderer.get().vertexuv(cx, minRY, (cz + 1), 0, minRY * 2 / 8F + downfall * 2);
								Renderer.get().vertexuv((cx + 1), minRY, cz, 2, minRY * 2 / 8F + downfall * 2);
								Renderer.get().vertexuv((cx + 1), maxRY, cz, 2, maxRY * 2 / 8F + downfall * 2);
								Renderer.get().vertexuv(cx, maxRY, (cz + 1), 0, maxRY * 2 / 8F + downfall * 2);
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
					GL11.glTranslatef(((pass << 1) - 1) * 0.1F, 0, 0);
				}

				RenderHelper.getHelper().hurtEffect(this.player, this.timer.delta);
				if(this.settings.getBooleanSetting("options.view-bobbing").getValue()) {
					RenderHelper.getHelper().applyBobbing(this.player, this.timer.delta);
				}

				float heldPos = this.heldBlock.lastPosition + (this.heldBlock.heldPosition - this.heldBlock.lastPosition) * this.timer.delta;
				GL11.glPushMatrix();
				GL11.glRotatef(this.player.oPitch + (this.player.pitch - this.player.oPitch) * this.timer.delta, 1, 0, 0);
				GL11.glRotatef(this.player.oYaw + (this.player.yaw - this.player.oYaw) * this.timer.delta, 0, 1, 0);
				RenderHelper.getHelper().setLighting(true);
				GL11.glPopMatrix();
				GL11.glPushMatrix();
				if(this.heldBlock.moving) {
					float off = (this.heldBlock.heldOffset + this.timer.delta) / 7F;
					float offsin = MathHelper.sin(off * MathHelper.PI);
					GL11.glTranslatef(-MathHelper.sin((float) Math.sqrt(off) * MathHelper.PI) * 0.4F, MathHelper.sin((float) Math.sqrt(off) * MathHelper.TWO_PI) * 0.2F, -offsin * 0.2F);
				}

				GL11.glTranslatef(0.7F * 0.8F, -0.65F * 0.8F - (1 - heldPos) * 0.6F, -0.9F * 0.8F);
				GL11.glRotatef(45, 0, 1, 0);
				GL11.glEnable(GL11.GL_NORMALIZE);
				if(this.heldBlock.moving) {
					float off = (this.heldBlock.heldOffset + this.timer.delta) / 7F;
					float offsin = MathHelper.sin((off) * off * MathHelper.PI);
					GL11.glRotatef(MathHelper.sin((float) Math.sqrt(off) * MathHelper.PI) * 80, 0, 1, 0);
					GL11.glRotatef(-offsin * 20, 1, 0, 0);
				}

				float brightness = this.level.getBrightness((int) this.player.x, (int) this.player.y, (int) this.player.z);
				GL11.glColor4f(brightness, brightness, brightness, 1);
				if(this.hud.isVisible()) {
					if(this.heldBlock.block != null) {
						GL11.glScalef(0.4F, 0.4F, 0.4F);
						GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
						this.heldBlock.block.getModel().renderAll(0, 0, 0, this.level.getBrightness((int) this.player.x, (int) this.player.y, (int) this.player.z));
					} else {
						this.player.bindTexture(this.textureManager);
						GL11.glScalef(1, -1, -1);
						GL11.glTranslatef(0, 0.2F, 0);
						GL11.glRotatef(-120, 0, 0, 1);
						GL11.glScalef(1, 1, 1);
						ModelPart arm = this.player.getModel().leftArm;
						if(!arm.hasList) {
							arm.generateList(0.0625F);
						}

						GL11.glCallList(arm.list);
					}
				}

				GL11.glDisable(GL11.GL_NORMALIZE);
				GL11.glPopMatrix();
				RenderHelper.getHelper().setLighting(false);
				if(!this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					break;
				}

				pass++;
			}

			RenderHelper.getHelper().ortho();
			int mox = Mouse.getEventX();
			int moy = Display.getHeight() - Mouse.getEventY();
			this.hud.render(mox, moy);
		} else {
			GL11.glViewport(0, 0, this.width, this.height);
			GL11.glClearColor(0, 0, 0, 0);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			RenderHelper.getHelper().ortho();
		}

		int mx = Mouse.getEventX();
		int my = Display.getHeight() - Mouse.getEventY();
		this.baseGUI.render(mx, my);
		if(this.progressBar.isVisible()) {
			this.progressBar.render(false);
		}

		Thread.yield();
		Display.update();
		if(this.settings.getBooleanSetting("options.limit-fps").getValue()) {
			try {
				Thread.sleep(5);
			} catch(InterruptedException e) {
			}
		}

		this.fps++;
		while(System.currentTimeMillis() >= this.lastUpdate + 1000) {
			if(this.hud != null) {
				this.hud.debugInfo = this.fps + " fps, " + Chunk.chunkUpdates + " chunk updates";
			}
			
			Chunk.chunkUpdates = 0;
			this.lastUpdate += 1000;
			this.fps = 0;
		}
		
		return true;
	}

	public void grabMouse() {
		if(!Mouse.isGrabbed()) {
			Mouse.setGrabbed(true);
			if(OpenClassic.getClient().getActiveComponent() != null) {
				OpenClassic.getClient().setActiveComponent(null);
			}
			
			this.lastClick = this.ticks + 10000;
		}
	}

	public void displayMenu() {
		if(OpenClassic.getClient().getActiveComponent() == null && this.ingame && (!this.isInMultiplayer() || this.session.isConnected() && this.session.getState() == State.GAME)) {
			OpenClassic.getClient().setActiveComponent(new IngameMenuScreen());
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
				if(this.selected.hasEntity) {
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
						if(this.level != null && (!this.level.getBlockTypeAt(x, y, z).isUnbreakable() || this.ocPlayer.canBreakUnbreakables())) {
							this.mode.hitBlock(x, y, z);
							return;
						}
					} else {
						int id = this.player.inventory.getSelected();
						if(id <= 0) {
							return;
						}

						if(this.ocPlayer.getPlaceMode() > 0) {
							id = this.ocPlayer.getPlaceMode();
						}

						BlockType block = this.level.getBlockTypeAt(x, y, z);
						BoundingBox collision = Blocks.fromId(id).getModel(this.level, x, y, z).getCollisionBox(x, y, z);
						if((block == null || block.canPlaceIn()) && (collision == null || (!this.player.bb.intersects(collision) && this.level.isFree(collision)))) {
							if(!this.mode.canPlace(id)) {
								return;
							}

							if(this.isInMultiplayer()) {
								this.session.send(new PlayerSetBlockMessage((short) x, (short) y, (short) z, button == 1, (byte) id));
							}

							if(!this.isInMultiplayer() && EventManager.callEvent(new BlockPlaceEvent(this.level.getBlockAt(x, y, z), OpenClassic.getClient().getPlayer(), this.heldBlock.block)).isCancelled()) {
								return;
							}

							this.level.setBlockAt(x, y, z, Blocks.fromId(id));
							this.heldBlock.heldPosition = 0;
							if(Blocks.fromId(id) != null && Blocks.fromId(id).getPhysics() != null) {
								Blocks.fromId(id).getPhysics().onPlace(this.level.getBlockAt(x, y, z));
							}

							BlockType type = Blocks.fromId(id);
							if(type != null && type.getStepSound() != StepSound.NONE) {
								this.audio.playSound(type.getStepSound().getSound(), x, y, z, (type.getStepSound().getVolume() + 1) / 2, type.getStepSound().getPitch() * 0.8F);
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
		if(OpenClassic.getClient().getActiveComponent() != null) {
			this.lastClick = this.ticks + 10000;
		}
		
		while(Mouse.next()) {
			if(this.ingame && OpenClassic.getClient().getActiveComponent() == null) {
				if(Mouse.getEventDWheel() != 0) {
					this.player.inventory.scrollSelection(Mouse.getEventDWheel());
				}

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
							int block = this.level.getBlockTypeAt(this.selected.x, this.selected.y, this.selected.z).getId();
							if(block == VanillaBlock.GRASS.getId()) {
								block = VanillaBlock.DIRT.getId();
							}

							if(block == VanillaBlock.DOUBLE_SLAB.getId()) {
								block = VanillaBlock.SLAB.getId();
							}

							if(block == VanillaBlock.BEDROCK.getId()) {
								block = VanillaBlock.STONE.getId();
							}

							this.player.inventory.selectBlock(block, this.mode instanceof CreativeGameMode);
						}
					}
				}
			} else if(Mouse.getEventButtonState()) {
				int x = Mouse.getEventX();
				int y = Display.getHeight() - Mouse.getEventY();
				this.baseGUI.onMouseClick(x, y, Mouse.getEventButton());
			}
		}

		while(Keyboard.next()) {
			if(this.ingame && OpenClassic.getClient().getActiveComponent() == null) {
				this.player.input.setKeyState(Keyboard.getEventKey(), Keyboard.getEventKeyState());
				if(Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent()) {
					this.player.input.keyPress(Keyboard.getEventKey());
				}
				
				if(Keyboard.getEventKeyState()) {
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
						this.hud.setVisible(!this.hud.isVisible());
					}

					if(!this.isInMultiplayer() || this.session.isConnected() && this.session.getState() == State.GAME) {
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
								if(this.hud != null) this.ocPlayer.sendMessage("screenshot.saved", file.getName());
							} catch(IOException e) {
								e.printStackTrace();
								if(this.hud != null) this.ocPlayer.sendMessage("screenshot.error", file.getName());
							}
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
							this.displayMenu();
						}

						if(this.mode instanceof CreativeGameMode) {
							if(Keyboard.getEventKey() == this.bindings.getBinding("options.keys.load-loc").getKey()) {
								PlayerRespawnEvent event = new PlayerRespawnEvent(OpenClassic.getClient().getPlayer(), new Position(OpenClassic.getClient().getLevel(), this.level.getSpawn().getX(), this.level.getSpawn().getY(), this.level.getSpawn().getZ(), this.level.getSpawn().getYaw(), this.level.getSpawn().getPitch()));
								if(!event.isCancelled()) {
									this.player.resetPos(event.getPosition());
								}
							}

							if(Keyboard.getEventKey() == this.bindings.getBinding("options.keys.save-loc").getKey()) {
								this.level.setSpawn(new Position(this.level, this.player.x, this.player.y, this.player.z, this.player.yaw, this.player.pitch));
								this.player.resetPos();
							}
						}

						Keyboard.getEventKey();
						if(Keyboard.getEventKey() == Keyboard.KEY_F5) {
							this.raining = !this.raining;
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_TAB && this.mode instanceof SurvivalGameMode && this.player.arrows > 0) {
							OpenClassic.getGame().getAudioManager().playSound("random.bow", this.player.x, this.player.y, this.player.z, 1, 1 / (rand.nextFloat() * 0.4f + 0.8f));
							this.level.addEntity(new Arrow(this.level, this.player, this.player.x, this.player.y, this.player.z, this.player.yaw, this.player.pitch, 1.2F));
							this.player.arrows--;
						}
						
						if(Keyboard.getEventKey() == Keyboard.KEY_Q && this.mode instanceof SurvivalGameMode) {
							this.player.dropHeldItem();
						}

						if(Keyboard.getEventKey() == this.bindings.getBinding("options.keys.blocks").getKey()) {
							this.mode.openInventory();
						}

						if(Keyboard.getEventKey() == this.bindings.getBinding("options.keys.chat").getKey()) {
							this.player.input.resetKeys();
							OpenClassic.getClient().setActiveComponent(new ChatInputScreen());
						}
					}
					
					for(int selection = 0; selection < 9; selection++) {
						if(Keyboard.getEventKey() == selection + 2) {
							this.player.inventory.selected = selection;
						}
					}

					if(Keyboard.getEventKey() == this.bindings.getBinding("options.keys.toggle-fog").getKey()) {
						this.settings.getSetting("options.render-distance").toggle();
					}
				}

				EventManager.callEvent(new PlayerKeyChangeEvent(OpenClassic.getClient().getPlayer(), Keyboard.getEventKey(), Keyboard.getEventKeyState()));
				if(this.isInMultiplayer() && this.session.isConnected() && this.openclassicServer) {
					this.session.send(new KeyChangeMessage(Keyboard.getEventKey(), Keyboard.getEventKeyState()));
				}
			} else if(Keyboard.getEventKeyState()) {
				if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE && this.ingame) {
					OpenClassic.getClient().setActiveComponent(null);
					return;
				}
				
				this.baseGUI.onKeyPress(Keyboard.getEventCharacter(), Keyboard.getEventKey());
			}
		}

		int mx = Mouse.getX();
		int my = Display.getHeight() - Mouse.getY();
		this.baseGUI.update(mx, my);
		
		if(!this.ingame) return;
		if(System.currentTimeMillis() > this.audio.getMusicTime() && this.audio.playMusic("bg")) {
			this.audio.setMusicTime(System.currentTimeMillis() + rand.nextInt(900000) + 300000L);
		}

		if(this.level != null) {
			this.waterDelay++;
			if(this.waterDelay > 200 && rand.nextInt(1000) < 250) {
				this.waterDelay = 0;
				Block block = null;
				float dist = 10000;
				for(int x = (int) this.player.x - 50; x < this.player.x + 50; x++) {
					for(int y = (int) this.player.y - 20; y < this.player.y + 20; y++) {
						for(int z = (int) this.player.z - 50; z < this.player.z + 50; z++) {
							BlockType b = this.level.getBlockTypeAt(x, y, z);
							float d = (float) Math.sqrt((this.player.x - x) * (this.player.x - x) + (this.player.y - y) * (this.player.y - y) + (this.player.z - z) * (this.player.z - z));
							if(b != null && b.getLiquidName() != null && b.getLiquidName().equals("water") && d < dist) {
								block = this.level.getBlockAt(x, y, z);
								dist = d;
							}
						}
					}
				}
				
				if(block != null) {
					OpenClassic.getGame().getAudioManager().playSound(block.getLevel(), "random.water", block.getPosition().getX() + 0.5f, block.getPosition().getY() + 0.5f, block.getPosition().getZ() + 0.5f, rand.nextFloat() * 0.25f + 0.75f, rand.nextFloat() + 0.5f);
				}
			}
		}
		
		this.mode.spawnMobs();
		int mouseX = Mouse.getX() * this.hud.getWidth() / this.width;
		int mouseY = this.hud.getHeight() - Mouse.getY() * this.hud.getHeight() / this.height - 1;
		this.hud.update(mouseX, mouseY);
		
		for(int index = 0; index < this.textureManager.animations.size(); index++) {
			AnimatedTexture animation = this.textureManager.animations.get(index);
			animation.anaglyph = this.textureManager.settings.getBooleanSetting("options.3d-anaglyph").getValue();
			animation.animate();
		}

		if(this.isInMultiplayer()) {
			if(OpenClassic.getClient().getActiveComponent() instanceof ErrorScreen) {
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

		if(OpenClassic.getClient().getActiveComponent() == null && this.player != null && this.mode instanceof SurvivalGameMode && this.player.health <= 0) {
			OpenClassic.getClient().setActiveComponent(new GameOverScreen());
		}

		if(OpenClassic.getClient().getActiveComponent() == null) {
			if(this.blockHitTime > 0) {
				this.blockHitTime--;
			}

			if(OpenClassic.getClient().getActiveComponent() == null) {
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
				if(OpenClassic.getClient().getActiveComponent() == null && Mouse.isButtonDown(0) && Mouse.isGrabbed() && this.selected != null && !this.selected.hasEntity) {
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

			block = this.ocPlayer != null && this.ocPlayer.getPlaceMode() != 0 ? Blocks.fromId(this.ocPlayer.getPlaceMode()) : block;

			float position = (block == this.heldBlock.block ? 1 : 0) - this.heldBlock.heldPosition;
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
					int y = this.level.getHighestBlockY(x, z);
					if(y <= (int) this.player.y + 4 && y >= (int) this.player.y - 4) {
						float xOffset = rand.nextFloat();
						float zOffset = rand.nextFloat();
						this.particleManager.spawnParticle(new RainParticle(this.level, x + xOffset, y + 0.1F, z + zOffset));
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
	
}
