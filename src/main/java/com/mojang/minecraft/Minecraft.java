package com.mojang.minecraft;

import java.awt.Canvas;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.TextureFactory;
import ch.spacebase.openclassic.api.event.block.BlockPlaceEvent;
import ch.spacebase.openclassic.api.event.player.PlayerKeyChangeEvent;
import ch.spacebase.openclassic.api.event.player.PlayerQuitEvent;
import ch.spacebase.openclassic.api.event.player.PlayerRespawnEvent;
import ch.spacebase.openclassic.api.gui.GuiComponent;
import ch.spacebase.openclassic.api.input.Keyboard;
import ch.spacebase.openclassic.api.input.Mouse;
import ch.spacebase.openclassic.api.math.BoundingBox;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.api.math.Vector;
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
import ch.spacebase.openclassic.client.gui.hud.HeldBlock;
import ch.spacebase.openclassic.client.input.Input;
import ch.spacebase.openclassic.client.input.KeyboardEvent;
import ch.spacebase.openclassic.client.input.MouseEvent;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.render.ClientTextureFactory;
import ch.spacebase.openclassic.client.render.RenderHelper;
import ch.spacebase.openclassic.client.render.level.Chunk;
import ch.spacebase.openclassic.client.settings.MinimapSetting;
import ch.spacebase.openclassic.client.settings.MusicSetting;
import ch.spacebase.openclassic.client.settings.NightSetting;
import ch.spacebase.openclassic.client.settings.SmoothingSetting;
import ch.spacebase.openclassic.client.settings.SurvivalSetting;
import ch.spacebase.openclassic.client.settings.TextureRefreshSetting;
import ch.spacebase.openclassic.client.sound.ClientAudioManager;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.client.util.LWJGLNatives;
import ch.spacebase.openclassic.client.util.RayTracer;
import ch.spacebase.openclassic.client.util.ShaderManager;
import ch.spacebase.openclassic.game.Main;
import ch.spacebase.openclassic.game.network.ClassicSession.State;
import ch.spacebase.openclassic.game.network.msg.PlayerSetBlockMessage;
import ch.spacebase.openclassic.game.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.game.network.msg.custom.KeyChangeMessage;
import ch.spacebase.openclassic.game.scheduler.ClassicScheduler;
import ch.spacebase.openclassic.game.util.InternalConstants;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.item.Arrow;
import com.mojang.minecraft.entity.particle.RainParticle;
import com.mojang.minecraft.entity.player.InputHandler;
import com.mojang.minecraft.entity.player.LocalPlayer;
import com.mojang.minecraft.entity.player.net.NetworkPlayer;
import com.mojang.minecraft.gamemode.CreativeGameMode;
import com.mojang.minecraft.gamemode.GameMode;
import com.mojang.minecraft.gamemode.SurvivalGameMode;
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
	public LocalPlayer player;
	public Canvas canvas;
	public ClientProgressBar progressBar = new ClientProgressBar();
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
	public boolean wasActive = false;
	public int rainTicks = 0;
	public HeldBlock heldBlock = new HeldBlock();
	public HashMap<Byte, NetworkPlayer> netPlayers = new HashMap<Byte, NetworkPlayer>();
	private int schedTicks = 0;
	private long lastUpdate = 0;
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
	public float cracks = 0;

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
		
		GL11.glViewport(0, 0, this.width, this.height);
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

		this.player.input = new InputHandler(this.bindings);
		this.mode.apply(this.player);
	}
	
	public void initGame() {
		this.audio.stopMusic();
		this.audio.setMusicTime(System.currentTimeMillis() + rand.nextInt(900000));
		if(this.server != null) {
			this.session = new ClientSession(this.ocPlayer, this.tempKey, this.server, this.port);
			this.tempKey = null;
		}

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
	
	public void shutdown() {
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
		
		File resourcepacks = new File(this.dir, "resourcepacks");
		if(!resourcepacks.exists()) {
			try {
				resourcepacks.mkdirs();
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
		this.settings.registerSetting(new IntSetting("options.render-distance", new String[] {
				OpenClassic.getGame().getTranslator().translate("options.render-distance-options.far"),
				OpenClassic.getGame().getTranslator().translate("options.render-distance-options.normal"),
				OpenClassic.getGame().getTranslator().translate("options.render-distance-options.short"),
				OpenClassic.getGame().getTranslator().translate("options.render-distance-options.tiny") }));
		
		this.settings.registerSetting(new BooleanSetting("options.view-bobbing"));
		this.settings.getBooleanSetting("options.view-bobbing").setDefault(true);
		this.settings.registerSetting(new TextureRefreshSetting("options.3d-anaglyph"));
		this.settings.registerSetting(new BooleanSetting("options.limit-fps"));
		this.settings.registerSetting(new SurvivalSetting("options.survival", new String[] {
				OpenClassic.getGame().getTranslator().translate("options.off"),
				OpenClassic.getGame().getTranslator().translate("options.survival-options.peaceful"),
				OpenClassic.getGame().getTranslator().translate("options.survival-options.normal") }));
		
		this.settings.registerSetting(new SmoothingSetting("options.smoothing"));
		this.settings.registerSetting(new NightSetting("options.night"));
		this.settings.registerSetting(new IntSetting("options.sensitivity", new String[] {
				OpenClassic.getGame().getTranslator().translate("options.survival-options.slow"),
				OpenClassic.getGame().getTranslator().translate("options.survival-options.normal"),
				OpenClassic.getGame().getTranslator().translate("options.survival-options.fast"),
				OpenClassic.getGame().getTranslator().translate("options.survival-options.faster"),
				OpenClassic.getGame().getTranslator().translate("options.survival-options.fastest") }));
		
		this.settings.getIntSetting("options.sensitivity").setDefault(1);
		this.settings.registerSetting(new MinimapSetting("options.minimap"));
		
		this.hackSettings = new Settings();
		this.hackSettings.registerSetting(new BooleanSetting("hacks.speed"));
		this.hackSettings.registerSetting(new BooleanSetting("hacks.flying"));
		OpenClassic.getClient().getConfig().applyDefault("options.resource-pack", "none");
		OpenClassic.getClient().getConfig().save();
		
		this.mode = this.settings.getIntSetting("options.survival").getValue() > 0 ? new SurvivalGameMode(this) : new CreativeGameMode(this);

		((ClassicClient) OpenClassic.getClient()).init();
		this.baseGUI = new GuiComponent("base", 0, 0, this.width, this.height);
		this.baseGUI.setFocused(true);
		this.fps = 0;
		
		this.initRender();
		this.init = true;
		if(this.server == null || this.server.equals("") || this.port == 0) {
			OpenClassic.getClient().setActiveComponent(new LoginScreen());
		} else {
			this.initGame();
		}
		
		while(this.running && !Display.isCloseRequested()) {
			this.timer.update();
			Input.update();
			for(int tick = 0; tick < this.timer.elapsedTicks; tick++) {
				this.ticks++;
				this.tick();
			}
			
			this.render(this.timer.delta);
		}

		this.shutdown();
		this.destroyRender();
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
					Display.setIcon(new ByteBuffer[] { this.loadIcon(Main.class.getResourceAsStream("/icon.png")) });
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
		
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glClearDepth(GL11.GL_CLIENT_PIXEL_STORE_BIT);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glLineWidth(2);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glViewport(0, 0, this.width, this.height);
		
		RenderHelper.getHelper().init();
		ShaderManager.setup();
	}
	
	private void destroyRender() {
		ShaderManager.cleanup();
		Display.destroy();
	}
	
	private void render(float delta) {
		if(this.width != Display.getWidth() || this.height != Display.getHeight()) {
			this.resize();
		}

		this.displayActive = Display.isActive();
		this.mode.applyBlockCracks(delta);
		((ClientTextureFactory) TextureFactory.getFactory()).renderUpdateTextures();
		if(this.level != null && this.player != null) {
			ClientLevel level = this.level;
			LocalPlayer player = this.player;
			if(Input.isMouseGrabbed()) {
				int x = Input.getMouseDX();
				int y = Input.getMouseDY();
				byte direction = 1;
				if(this.settings.getBooleanSetting("options.invert-mouse").getValue()) {
					direction = -1;
				}
				
				player.turn(x, (y * direction));
				Input.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
			} else {
				player.turn(0, 0);
			}

			for(int pass = 0; pass < 2; pass++) {
				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					if(pass == 0) {
						GL11.glColorMask(false, true, true, false);
					} else {
						GL11.glColorMask(true, false, false, false);
					}
				}


				level.render(delta, pass);
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				GL11.glLoadIdentity();
				if(this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					GL11.glTranslatef(((pass << 1) - 1) * 0.1F, 0, 0);
				}

				RenderHelper.getHelper().hurtEffect(player.openclassic, delta);
				if(this.settings.getBooleanSetting("options.view-bobbing").getValue()) {
					RenderHelper.getHelper().applyBobbing(player.openclassic, delta);
				}
				
				this.heldBlock.render(delta);
				if(!this.settings.getBooleanSetting("options.3d-anaglyph").getValue()) {
					break;
				}
			}

			GL11.glColorMask(true, true, true, false);
			RenderHelper.getHelper().ortho();
			if(this.hud != null) {
				this.hud.render(Input.getMouseX(), Display.getHeight() - Input.getMouseY());
			}
		} else {
			GL11.glClearColor(0, 0, 0, 0);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			RenderHelper.getHelper().ortho();
		}

		this.baseGUI.render(Input.getMouseX(), Display.getHeight() - Input.getMouseY());
		if(this.progressBar.isVisible()) {
			this.progressBar.render(false);
		}

		Display.update();
		this.fps++;
		if(this.settings.getBooleanSetting("options.limit-fps").getValue()) {
			Display.sync(Display.getDesktopDisplayMode().getFrequency());
		}

		if(System.currentTimeMillis() - this.lastUpdate >= 1000) {
			if(this.hud != null) {
				this.hud.debugInfo = this.fps + " fps, " + Chunk.chunkUpdates + " chunk updates";
			}
			
			Chunk.chunkUpdates = 0;
			this.fps = 0;
			this.lastUpdate = System.currentTimeMillis();
		}
	}

	public void setMouseGrabbed(boolean grabbed) {
		if(!Input.isMouseGrabbed() && grabbed) {
			if(OpenClassic.getClient().getActiveComponent() != null) {
				OpenClassic.getClient().setActiveComponent(null);
			}
			
			this.lastClick = this.ticks + 10000;
		}
		
		Input.setMouseGrabbed(grabbed);
	}

	private void onMouseClick(int button) {
		if(button != 0 || this.blockHitTime <= 0) {
			if(button == 0) {
				this.heldBlock.move();
			}

			int selected = this.player.inventory.getSelected();
			if(button == 1 && selected > 0 && this.mode.useItem(this.player, selected)) {
				this.heldBlock.setHeldPosition(0);
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
					} else if(this.player.inventory.getSelected() > 0) {
						BlockType type = Blocks.fromId(this.player.inventory.getSelected());
						if(type == null) {
							return;
						}

						if(this.ocPlayer.getPlaceMode() != null) {
							type = this.ocPlayer.getPlaceMode();
						}

						BlockType block = this.level.getBlockTypeAt(x, y, z);
						BoundingBox collision = type.getModel(this.level, x, y, z).getCollisionBox(x, y, z);
						if((block == null || block.canPlaceIn()) && (collision == null || (!this.player.bb.intersects(collision) && this.level.isFree(collision)))) {
							if(!this.mode.canPlace(type.getId())) {
								return;
							}

							if(this.isInMultiplayer()) {
								this.session.send(new PlayerSetBlockMessage((short) x, (short) y, (short) z, button == 1, type.getId()));
							}

							if(!this.isInMultiplayer() && EventManager.callEvent(new BlockPlaceEvent(this.level.getBlockAt(x, y, z), OpenClassic.getClient().getPlayer(), this.heldBlock.getBlock())).isCancelled()) {
								return;
							}

							this.level.setBlockAt(x, y, z, type);
							this.heldBlock.setHeldPosition(0);
							if(type != null && type.getPhysics() != null) {
								type.getPhysics().onPlace(this.level.getBlockAt(x, y, z));
							}

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
		this.audio.update(this.ocPlayer);
		((ClassicScheduler) OpenClassic.getGame().getScheduler()).tick(this.schedTicks);
		if(OpenClassic.getClient().getActiveComponent() != null) {
			this.lastClick = this.ticks + 10000;
		}
		
		MouseEvent mouse = null;
		while((mouse = Input.nextMouseEvent()) != null) {
			if(this.ingame && OpenClassic.getClient().getActiveComponent() == null) {
				ClientLevel level = this.level;
				if(mouse.getDWheel() != 0) {
					this.player.inventory.scrollSelection(mouse.getDWheel());
				}

				if(!Input.isMouseGrabbed() && mouse.getState()) {
					this.setMouseGrabbed(true);
				} else {
					if(mouse.getState()) {
						if(mouse.getButton() == 0) {
							this.onMouseClick(Mouse.LEFT_BUTTON);
							this.lastClick = this.ticks;
						}

						if(mouse.getButton() == 1) {
							this.onMouseClick(Mouse.RIGHT_BUTTON);
							this.lastClick = this.ticks;
						}

						if(mouse.getButton() == 2 && this.selected != null) {
							int block = level.getBlockTypeAt(this.selected.x, this.selected.y, this.selected.z).getId();
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
			} else if(mouse.getState()) {
				int x = mouse.getX();
				int y = Display.getHeight() - mouse.getY();
				this.baseGUI.onMouseClick(x, y, mouse.getButton());
			}
		}
		
		KeyboardEvent keyboard = null;
		while((keyboard = Input.nextKeyEvent()) != null) {
			if(this.ingame && OpenClassic.getClient().getActiveComponent() == null) {
				ClientLevel level = this.level;
				this.hud.onKeyPress(keyboard.getCharacter(), keyboard.getKey());
				this.player.input.setKeyState(keyboard.getKey(), keyboard.getState());
				if(keyboard.getState() && !keyboard.isRepeat()) {
					this.player.input.keyPress(keyboard.getKey());
				}
				
				if(keyboard.getState()) {
					if(keyboard.getKey() == Keyboard.KEY_F6) {
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

					if(keyboard.getKey() == Keyboard.KEY_F1) {
						this.hud.setVisible(!this.hud.isVisible());
					}

					if(!this.isInMultiplayer() || this.session.isConnected() && this.session.getState() == State.GAME) {
						if(keyboard.getKey() == Keyboard.KEY_F2) {
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

						if(keyboard.getKey() == Keyboard.KEY_ESCAPE) {
							if(OpenClassic.getClient().getActiveComponent() == null && this.ingame && (!this.isInMultiplayer() || this.session.isConnected() && this.session.getState() == State.GAME)) {
								OpenClassic.getClient().setActiveComponent(new IngameMenuScreen());
							}
						}

						if(this.mode instanceof CreativeGameMode) {
							if(keyboard.getKey() == this.bindings.getBinding("options.keys.load-loc").getKey()) {
								PlayerRespawnEvent event = new PlayerRespawnEvent(OpenClassic.getClient().getPlayer(), new Position(OpenClassic.getClient().getLevel(), level.getSpawn().getX(), level.getSpawn().getY(), level.getSpawn().getZ(), level.getSpawn().getYaw(), level.getSpawn().getPitch()));
								if(!event.isCancelled()) {
									this.player.resetPos(event.getPosition());
								}
							}

							if(keyboard.getKey() == this.bindings.getBinding("options.keys.save-loc").getKey()) {
								level.setSpawn(new Position(level, this.player.pos.getX(), this.player.pos.getY(), this.player.pos.getZ(), this.player.pos.getYaw(), this.player.pos.getPitch()));
								this.player.resetPos();
							}
						}

						keyboard.getKey();
						if(keyboard.getKey() == Keyboard.KEY_F5) {
							this.raining = !this.raining;
						}

						if(keyboard.getKey() == Keyboard.KEY_TAB && this.mode instanceof SurvivalGameMode && this.player.arrows > 0) {
							OpenClassic.getGame().getAudioManager().playSound("random.bow", this.player.pos.getX(), this.player.pos.getY(), this.player.pos.getZ(), 1, 1 / (rand.nextFloat() * 0.4f + 0.8f));
							level.addEntity(new Arrow(level, this.player, this.player.pos.getX(), this.player.pos.getY(), this.player.pos.getZ(), this.player.pos.getYaw(), this.player.pos.getPitch(), 1.2F));
							this.player.arrows--;
						}
						
						if(keyboard.getKey() == Keyboard.KEY_Q && this.mode instanceof SurvivalGameMode) {
							this.player.dropHeldItem();
						}

						if(keyboard.getKey() == this.bindings.getBinding("options.keys.blocks").getKey()) {
							this.mode.openInventory();
						}

						if(keyboard.getKey() == this.bindings.getBinding("options.keys.chat").getKey()) {
							this.player.input.resetKeys();
							OpenClassic.getClient().setActiveComponent(new ChatInputScreen());
						}
					}
					
					for(int selection = 0; selection < 9; selection++) {
						if(keyboard.getKey() == selection + 2) {
							this.player.inventory.selected = selection;
						}
					}

					if(keyboard.getKey() == this.bindings.getBinding("options.keys.toggle-fog").getKey()) {
						this.settings.getSetting("options.render-distance").toggle();
					}
				}

				EventManager.callEvent(new PlayerKeyChangeEvent(OpenClassic.getClient().getPlayer(), keyboard.getKey(), keyboard.getState()));
				if(this.isInMultiplayer() && this.session.isConnected() && this.openclassicServer) {
					this.session.send(new KeyChangeMessage(keyboard.getKey(), keyboard.getState()));
				}
			} else if(keyboard.getState()) {
				if(keyboard.getKey() == Keyboard.KEY_ESCAPE && this.ingame) {
					OpenClassic.getClient().setActiveComponent(null);
				} else {
					this.baseGUI.onKeyPress(keyboard.getCharacter(), keyboard.getKey());
				}
			}
		}
		
		this.baseGUI.update(Input.getMouseX(), Display.getHeight() - Input.getMouseY());
		if(this.hud != null) {
			this.hud.update(Input.getMouseX(), Display.getHeight() - Input.getMouseY());
		}
		
		if(this.displayActive && !this.wasActive && !Input.isButtonDown(Mouse.LEFT_BUTTON) && !Input.isButtonDown(Mouse.RIGHT_BUTTON) && !Input.isButtonDown(Mouse.MIDDLE_BUTTON)) {
			if(OpenClassic.getClient().getActiveComponent() == null && this.ingame && (!this.isInMultiplayer() || this.session.isConnected() && this.session.getState() == State.GAME)) {
				OpenClassic.getClient().setActiveComponent(new IngameMenuScreen());
			}
			
			this.wasActive = true;
		} else if(!this.displayActive) {
			this.wasActive = false;
		}
		
		((ClientTextureFactory) TextureFactory.getFactory()).updateTextures();
		
		if(!this.ingame) return;
		if(System.currentTimeMillis() > this.audio.getMusicTime() && this.audio.playMusic("bg")) {
			this.audio.setMusicTime(System.currentTimeMillis() + rand.nextInt(900000) + 300000L);
		}

		if(this.level != null) {
			float pitch = this.player.pos.getInterpolatedPitch(this.timer.delta);
			float yaw = this.player.pos.getInterpolatedYaw(this.timer.delta);
			Vector pVec = this.player.pos.toPosVector(this.timer.delta);
			float ycos = MathHelper.cos(-yaw * MathHelper.DEG_TO_RAD - MathHelper.PI);
			float ysin = MathHelper.sin(-yaw * MathHelper.DEG_TO_RAD - MathHelper.PI);
			float pcos = MathHelper.cos(-pitch * MathHelper.DEG_TO_RAD);
			float psin = MathHelper.sin(-pitch * MathHelper.DEG_TO_RAD);
			float mx = ysin * pcos;
			float mz = ycos * pcos;
			float reach = this.mode.getReachDistance();
			this.selected = RayTracer.rayTrace(this.level, pVec, pVec.clone().add(mx * reach, psin * reach, mz * reach), true);
			if(this.selected != null) {
				reach = this.selected.pos.distance(pVec);
			}

			if(this.mode instanceof CreativeGameMode) {
				reach = 32;
			}

			Entity selectedEntity = null;
			List<Entity> entities = this.level.getEntities(this.player, this.player.bb.expand(mx * reach, psin * reach, mz * reach));

			float distance = 0;
			for(int count = 0; count < entities.size(); count++) {
				Entity entity = entities.get(count);
				if(entity.isPickable()) {
					Intersection pos = RayTracer.rayTrace(entity.bb.grow(0.1F, 0.1F, 0.1F), pVec, pVec.clone().add(mx * reach, psin * reach, mz * reach));
					if(pos != null && (pVec.distance(pos.pos) < distance || distance == 0)) {
						selectedEntity = entity;
						distance = pVec.distance(pos.pos);
					}
				}
			}

			if(selectedEntity != null && !(this.mode instanceof CreativeGameMode)) {
				this.selected = new Intersection(selectedEntity);
			}
			
			this.waterDelay++;
			if(this.waterDelay > 200 && rand.nextInt(1000) < 250) {
				this.waterDelay = 0;
				Block block = null;
				float dist = 10000;
				for(int x = this.player.pos.getBlockX() - 50; x < this.player.pos.getBlockX() + 50; x++) {
					for(int y = this.player.pos.getBlockY() - 20; y < this.player.pos.getBlockY() + 20; y++) {
						for(int z = this.player.pos.getBlockZ() - 50; z < this.player.pos.getBlockZ() + 50; z++) {
							BlockType b = this.level.getBlockTypeAt(x, y, z);
							float d = this.player.pos.distance(x, y, z);
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
						this.session.send(new PlayerTeleportMessage((byte) -1, this.player.pos.getX(), this.player.pos.getY(), this.player.pos.getZ(), this.player.pos.getYaw(), this.player.pos.getPitch()));
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
				if(Input.isButtonDown(Mouse.LEFT_BUTTON) && (this.ticks - this.lastClick) >= InternalConstants.TICKS_PER_SECOND / 4 && Input.isMouseGrabbed()) {
					this.onMouseClick(Mouse.LEFT_BUTTON);
					this.lastClick = this.ticks;
				}

				if(Input.isButtonDown(Mouse.RIGHT_BUTTON) && (this.ticks - this.lastClick) >= InternalConstants.TICKS_PER_SECOND / 4 && Input.isMouseGrabbed()) {
					this.onMouseClick(Mouse.RIGHT_BUTTON);
					this.lastClick = this.ticks;
				}
			}

			if(!this.mode.creative && this.blockHitTime <= 0) {
				if(OpenClassic.getClient().getActiveComponent() == null && Input.isButtonDown(Mouse.LEFT_BUTTON) && Input.isMouseGrabbed() && this.selected != null && !this.selected.hasEntity) {
					this.mode.hitBlock(this.selected.x, this.selected.y, this.selected.z, this.selected.side);
				} else {
					this.mode.resetHits();
				}
			}
		}

		if(this.level != null) {
			this.rainTicks++;
			int id = this.player.inventory.getSelected();
			BlockType block = null;
			if(id > 0) {
				block = Blocks.fromId(id);
			}

			block = this.ocPlayer != null && this.ocPlayer.getPlaceMode() != null ? this.ocPlayer.getPlaceMode() : block;
			this.heldBlock.tick(block);
			
			if(this.raining) {
				for(int count = 0; count < 50; count++) {
					int x = this.player.pos.getBlockX() + rand.nextInt(9) - 4;
					int z = this.player.pos.getBlockZ() + rand.nextInt(9) - 4;
					int y = this.level.getHighestBlockY(x, z);
					if(y <= this.player.pos.getBlockY() + 4 && y >= this.player.pos.getBlockY() - 4) {
						float xOffset = rand.nextFloat();
						float zOffset = rand.nextFloat();
						this.level.getParticleManager().spawnParticle(new RainParticle(new Position(this.level, x + xOffset, y + 0.1F, z + zOffset)));
					}
				}
			}

			this.level.tick();
		}

		for(Plugin plugin : OpenClassic.getClient().getPluginManager().getPlugins()) {
			plugin.tick();
		}

		this.schedTicks++;
	}
	
}
