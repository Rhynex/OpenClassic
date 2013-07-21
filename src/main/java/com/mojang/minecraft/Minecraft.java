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

import ch.spacebase.openclassic.api.Color;
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
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.ClientProgressBar;
import ch.spacebase.openclassic.client.gui.LoginScreen;
import ch.spacebase.openclassic.client.gui.MainMenuScreen;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;
import ch.spacebase.openclassic.client.sound.ClientAudioManager;
import ch.spacebase.openclassic.client.util.BlockUtils;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.client.util.LWJGLNatives;
import ch.spacebase.openclassic.client.util.ShaderManager;
import ch.spacebase.openclassic.game.scheduler.ClassicScheduler;

import com.mojang.minecraft.gamemode.CreativeGameMode;
import com.mojang.minecraft.gamemode.GameMode;
import com.mojang.minecraft.gamemode.SurvivalGameMode;
import com.mojang.minecraft.gui.ChatInputScreen;
import com.mojang.minecraft.gui.ErrorScreen;
import com.mojang.minecraft.gui.GameOverScreen;
import com.mojang.minecraft.gui.HUDScreen;
import com.mojang.minecraft.gui.MenuScreen;
import com.mojang.minecraft.item.Arrow;
import com.mojang.minecraft.item.Item;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.model.ModelPart;
import com.mojang.minecraft.model.Vector;
import com.mojang.minecraft.particle.ParticleManager;
import com.mojang.minecraft.particle.WaterDropParticle;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.player.InputHandler;
import com.mojang.minecraft.player.LocalPlayer;
import com.mojang.minecraft.player.net.NetworkPlayer;
import com.mojang.minecraft.render.Chunk;
import com.mojang.minecraft.render.ChunkVisibleAndDistanceComparator;
import com.mojang.minecraft.render.Frustum;
import com.mojang.minecraft.render.FontRenderer;
import com.mojang.minecraft.render.LevelRenderer;
import com.mojang.minecraft.render.Renderer;
import com.mojang.minecraft.render.ShapeRenderer;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.minecraft.render.animation.AnimatedTexture;
import com.mojang.minecraft.render.animation.WaterTexture;
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
	public Renderer renderer = new Renderer(this);
	public ClientAudioManager audio;
	public ResourceDownloadThread resourceThread;
	private int ticks;
	private int blockHitTime;
	public Robot robot;
	public HUDScreen hud;
	public boolean awaitingLevel;
	public MovingObjectPosition selected;
	public GameSettings settings;
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
	public boolean hacks = true;
	public List<RemotePluginInfo> serverPlugins = new ArrayList<RemotePluginInfo>();
	public boolean ctf;
	public int mipmapMode = 0;
	public ClientSession session;
	public HashMap<Byte, NetworkPlayer> netPlayers = new HashMap<Byte, NetworkPlayer>();

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
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if (info.getName().equals("Nimbus")) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.canvas = canvas;
		this.width = width;
		this.height = height;
		if (canvas != null) {
			try {
				this.robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
	}

	public final void setCurrentScreen(GuiScreen screen) {
		if (this.currentScreen != null) {
			this.currentScreen.onClose();
		}

		if (screen == null && this.player != null && this.mode instanceof SurvivalGameMode && this.player.health <= 0) {
			screen = new GameOverScreen();
		}

		this.currentScreen = screen;
		if (screen != null) {
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
		int error = GL11.glGetError();
		if (error != 0) {
			String message = GLU.gluErrorString(error);
			System.err.println("########## GL ERROR ##########");
			System.err.println("@ " + task);
			System.err.println(error + ": " + message);
			System.exit(0);
		}
	}

	public final void shutdown() {
		if(this.shutdown) {
			return;
		}

		this.shutdown = true;
		this.running = false;
		if(this.ingame) this.stopGame(false);
		if (this.resourceThread != null) {
			this.resourceThread.running = false;
		}

		((ClassicScheduler) OpenClassic.getClient().getScheduler()).stop();
		this.audio.cleanup();
		OpenClassic.setGame(null);
		Display.destroy();

		System.exit(0);
	}

	public void stopGame(boolean menu) {
		this.audio.stopMusic();
		this.serverPlugins.clear();
		if(menu) this.setCurrentScreen(new MainMenuScreen());
		if(this.data != null) this.data.key = "";

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
		this.hacks = true;
		this.player = null;
		this.settings.speed = false;
		this.settings.flying = false;
		this.hideGui = false;
	}

	public void initGame() {
		this.initGame(OpenClassic.getGame().getGenerator("normal"));
	}

	public void initGame(Generator gen) {
		this.audio.stopMusic();
		this.audio.lastBGM = System.currentTimeMillis();
		if (this.server != null && this.data != null) {
			Level level = new Level();
			level.setData(8, 8, 8, new byte[512]);
			this.setLevel(level);
		} else {
			if (this.level == null) {
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

		if (this.server != null && this.data != null && this.player != null) {
			this.session = new ClientSession(this.player.openclassic, this.data.key, this.server, this.port);
			this.hacks = false;
		}

		this.mode = this.settings.survival && !this.isInMultiplayer() ? new SurvivalGameMode(this) : new CreativeGameMode(this);
		if(this.level != null) {
			this.mode.apply(this.level);
		}

		if(this.player != null) {
			this.mode.apply(this.player);
		}

		this.ingame = true;
	}

	private void handleException(Throwable e) {
		if(!this.running) {
			return;
		}

		if(this.started) {
			if(e instanceof LWJGLException) {
				this.running = false;
			} else {
				setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("core.client-error"), String.format(OpenClassic.getGame().getTranslator().translate("core.game-broke"), e)));
			}
		} else {
			String msg = "Failed to start the game.";
			if(OpenClassic.getGame() != null && OpenClassic.getGame().getTranslator() != null) {
				msg = OpenClassic.getGame().getTranslator().translate("core.fail-start");
			}

			JOptionPane.showMessageDialog(null, e.toString(), msg, 0);
			this.running = false;
		}

		e.printStackTrace();
	}

	private ByteBuffer loadIcon(InputStream in) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 128 * 128);
		buffer.clear();
		byte[] data = (byte[]) ImageIO.read(in).getRaster().getDataElements(0, 0, 128, 128, null);
		buffer.put(data);
		buffer.rewind();
		return buffer;
	}

	@SuppressWarnings({"unused"})
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
				System.err.println("Uncaught exception in thread \"" + t.getName() + "\"");
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

		try {
			if (this.canvas != null) {
				Display.setParent(this.canvas);
			} else {
				Display.setDisplayMode(new DisplayMode(this.width, this.height));
				Display.setResizable(true);
				try {
					Display.setIcon(new ByteBuffer[] {
							this.loadIcon(TextureManager.class.getResourceAsStream("/icon.png"))
					});
				} catch(IOException e) {
					System.err.println("Failed to load icon!");
					e.printStackTrace();
				}
			}
		} catch (LWJGLException e) {
			this.handleException(e);
			return;
		}

		Display.setTitle("OpenClassic " + Constants.VERSION);
		try {
			Display.create();
			Keyboard.create();
			Mouse.create();
		} catch (LWJGLException e) {
			this.handleException(e);
			return;
		}

		try {
			Controllers.create();
		} catch (LWJGLException e) {
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

		if (GLContext.getCapabilities().OpenGL30) {
			this.mipmapMode = 1;
		} else if (GLContext.getCapabilities().GL_EXT_framebuffer_object) {
			this.mipmapMode = 2;
		} else if (GLContext.getCapabilities().OpenGL14) {
			this.mipmapMode = 3;
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		}

		checkGLError("Startup");
		SessionData.loadFavorites(this.dir);

		this.audio = new ClientAudioManager(this);
		this.settings = new GameSettings(this, this.dir);

		this.mode = this.settings.survival ? new SurvivalGameMode(this) : new CreativeGameMode(this);
		this.textureManager = new TextureManager(this.settings);
		this.textureManager.addAnimatedTexture((new com.mojang.minecraft.render.animation.LavaTexture()));
		this.textureManager.addAnimatedTexture((new com.mojang.minecraft.render.animation.WaterTexture()));
		this.fontRenderer = new FontRenderer(this.settings, "/default.png", this.textureManager);
		this.levelRenderer = new LevelRenderer(this.textureManager);
		Item.initModels();
		GL11.glViewport(0, 0, this.width, this.height);
		checkGLError("Post Startup");

		((ClassicClient) OpenClassic.getClient()).init();
		this.resourceThread = new ResourceDownloadThread(dir, this, this.progressBar);
		this.resourceThread.start();

		this.progressBar.setVisible(true);
		this.progressBar.setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.loading"));
		this.progressBar.setSubtitle(OpenClassic.getGame().getTranslator().translate("http.downloading-resources"));
		this.progressBar.setProgress(-1);
		this.progressBar.render();

		ShaderManager.setup();
		long lastUpdate = System.currentTimeMillis();
		int fps = 0;

		while (this.running) {
			if (this.waiting) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			} else {
				if (Display.isCloseRequested()) {
					break;
				}

				if(this.width != Display.getWidth() || this.height != Display.getHeight()) {
					this.resize();
				}

				if(!this.started) {
					if(this.resourceThread.isFinished()) {
						this.progressBar.setVisible(false);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}

						if(this.server == null || this.server.equals("") || this.port == 0) {
							this.setCurrentScreen(new LoginScreen());
						} else {
							this.initGame();
						}

						this.started = true;
					}
				}

				long sysClock = System.currentTimeMillis() - this.timer.lastSysClock;
				long hrClock = System.nanoTime() / 1000000L;
				if (sysClock > 1000) {
					long diff = hrClock - this.timer.lastHRClock;
					double adj = (double) sysClock / (double) diff;
					this.timer.adjustment += (adj - this.timer.adjustment) * 0.20000000298023224D;
					this.timer.lastSysClock = System.currentTimeMillis();
					this.timer.lastHRClock = hrClock;
				}

				if (sysClock < 0L) {
					this.timer.lastSysClock = System.currentTimeMillis();
					this.timer.lastHRClock = hrClock;
				}

				double sec = hrClock / 1000D;
				double add = (sec - this.timer.lastHR) * this.timer.adjustment;
				this.timer.lastHR = sec;
				if (add < 0) {
					add = 0;
				}

				if (add > 1) {
					add = 1;
				}

				this.timer.elapsedPartialTicks = (float) (this.timer.elapsedPartialTicks + add * this.timer.speed * this.timer.tps);
				this.timer.elapsedTicks = (int) this.timer.elapsedPartialTicks;
				if (this.timer.elapsedTicks > 100) {
					this.timer.elapsedTicks = 100;
				}

				this.timer.elapsedPartialTicks -= this.timer.elapsedTicks;
				this.timer.renderPartialTicks = this.timer.elapsedPartialTicks;

				for (int tick = 0; tick < this.timer.elapsedTicks; tick++) {
					this.ticks++;
					this.tick();
				}

				checkGLError("Pre render");
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				this.mode.applyBlockCracks(this.timer.renderPartialTicks);
				if (this.renderer.displayActive && !Display.isActive() && !Mouse.isButtonDown(0) && !Mouse.isButtonDown(1) && !Mouse.isButtonDown(2)) { // Fixed focus bug for some computers/OS's
					this.displayMenu();
				}

				this.renderer.displayActive = Display.isActive();
				if (Mouse.isGrabbed()) {
					int x = Mouse.getDX();
					int y = Mouse.getDY();
					byte direction = 1;
					if (this.settings.invertMouse) {
						direction = -1;
					}

					this.player.turn(x, (y * direction));
					Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
				}

				int width = ClientRenderHelper.getHelper().getGuiWidth();
				int height = ClientRenderHelper.getHelper().getGuiHeight();
				if (this.level != null) {
					float var29 = this.player.xRotO + (this.player.xRot - this.player.xRotO) * this.timer.renderPartialTicks;
					float var30 = this.player.yRotO + (this.player.yRot - this.player.yRotO) * this.timer.renderPartialTicks;
					Vector var31 = this.renderer.getPlayerVector(this.timer.renderPartialTicks);
					float var32 = MathHelper.cos(-var30 * MathHelper.DEG_TO_RAD - MathHelper.PI);
					float var69 = MathHelper.sin(-var30 * MathHelper.DEG_TO_RAD - MathHelper.PI);
					float var74 = MathHelper.cos(-var29 * MathHelper.DEG_TO_RAD);
					float var33 = MathHelper.sin(-var29 * MathHelper.DEG_TO_RAD);
					float var34 = var69 * var74;
					float var87 = var32 * var74;
					float reach = this.mode.getReachDistance();
					this.selected = this.level.clip(var31, var31.add(var34 * reach, var33 * reach, var87 * reach), true);
					if (this.selected != null) {
						reach = this.selected.blockPos.distance(this.renderer.getPlayerVector(this.timer.renderPartialTicks));
					}

					var31 = this.renderer.getPlayerVector(this.timer.renderPartialTicks);
					if (this.mode instanceof CreativeGameMode) {
						reach = 32;
					}

					this.renderer.entity = null;
					List<Entity> entities = this.level.blockMap.getEntities(this.player, this.player.bb.expand(var34 * reach, var33 * reach, var87 * reach));

					float distance = 0;
					for (int count = 0; count < entities.size(); ++count) {
						Entity entity = entities.get(count);
						if (entity.isPickable()) {
							MovingObjectPosition pos = entity.bb.grow(0.1F, 0.1F, 0.1F).clip(var31, var31.add(var34 * reach, var33 * reach, var87 * reach));
							if (pos != null && (var31.distance(pos.blockPos) < distance || distance == 0)) {
								this.renderer.entity = entity;
								distance = var31.distance(pos.blockPos);
							}
						}
					}

					if (this.renderer.entity != null && !(this.mode instanceof CreativeGameMode)) {
						this.renderer.mc.selected = new MovingObjectPosition(this.renderer.entity);
					}

					int var77 = 0;

					while (true) {
						if (var77 >= 2) {
							GL11.glColorMask(true, true, true, false);
							break;
						}

						if (this.settings.anaglyph) {
							if (var77 == 0) {
								GL11.glColorMask(false, true, true, false);
							} else {
								GL11.glColorMask(true, false, false, false);
							}
						}

						GL11.glViewport(0, 0, this.width, this.height);
						var29 = 1.0F / (4 - this.settings.viewDistance);
						var29 = 1.0F - (float) Math.pow(var29, 0.25D);
						var30 = (this.level.skyColor >> 16 & 255) / 255.0F;
						float var117 = (this.level.skyColor >> 8 & 255) / 255.0F;
						var32 = (this.level.skyColor & 255) / 255.0F;
						this.renderer.fogRed = (this.level.fogColor >> 16 & 255) / 255.0F;
						this.renderer.fogBlue = (this.level.fogColor >> 8 & 255) / 255.0F;
						this.renderer.fogGreen = (this.level.fogColor & 255) / 255.0F;
						this.renderer.fogRed += (var30 - this.renderer.fogRed) * var29;
						this.renderer.fogBlue += (var117 - this.renderer.fogBlue) * var29;
						this.renderer.fogGreen += (var32 - this.renderer.fogGreen) * var29;
						BlockType type = Blocks.fromId(this.level.getTile((int) this.player.x, (int) (this.player.y + 0.12F), (int) this.player.z));
						if (type != null && type.isLiquid()) {
							if (type == VanillaBlock.WATER || type == VanillaBlock.STATIONARY_WATER) {
								this.renderer.fogRed = 0.02F;
								this.renderer.fogBlue = 0.02F;
								this.renderer.fogGreen = 0.2F;
							} else if (type == VanillaBlock.LAVA || type == VanillaBlock.STATIONARY_LAVA) {
								this.renderer.fogRed = 0.6F;
								this.renderer.fogBlue = 0.1F;
								this.renderer.fogGreen = 0.0F;
							}
						}

						if (this.renderer.mc.settings.anaglyph) {
							float var1000 = (this.renderer.fogRed * 30.0F + this.renderer.fogBlue * 59.0F + this.renderer.fogGreen * 11.0F) / 100.0F;
							var33 = (this.renderer.fogRed * 30.0F + this.renderer.fogBlue * 70.0F) / 100.0F;
							var34 = (this.renderer.fogRed * 30.0F + this.renderer.fogGreen * 70.0F) / 100.0F;
							this.renderer.fogRed = var1000;
							this.renderer.fogBlue = var33;
							this.renderer.fogGreen = var34;
						}

						GL11.glClearColor(this.renderer.fogRed, this.renderer.fogBlue, this.renderer.fogGreen, 0.0F);
						GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
						GL11.glEnable(GL11.GL_CULL_FACE);
						this.renderer.fogEnd = (512 >> (this.renderer.mc.settings.viewDistance << 1));
						GL11.glMatrixMode(GL11.GL_PROJECTION);
						GL11.glLoadIdentity();
						var29 = 0.07F;
						if (this.renderer.mc.settings.anaglyph) {
							GL11.glTranslatef((-((var77 << 1) - 1)) * var29, 0.0F, 0.0F);
						}

						var69 = 70;
						if (this.player.health <= 0) {
							var69 /= (1.0F - 500.0F / (this.player.deathTime + this.timer.renderPartialTicks + 500.0F)) * 2.0F + 1.0F;
						}

						GLU.gluPerspective(var69, (float) this.renderer.mc.width / (float) this.renderer.mc.height, 0.05F, this.renderer.fogEnd);
						GL11.glMatrixMode(GL11.GL_MODELVIEW);
						GL11.glLoadIdentity();
						if (this.settings.anaglyph) {
							GL11.glTranslatef(((var77 << 1) - 1) * 0.1F, 0.0F, 0.0F);
						}

						this.renderer.hurtEffect(this.timer.renderPartialTicks);
						if (this.settings.viewBobbing) {
							this.renderer.applyBobbing(this.timer.renderPartialTicks);
						}

						GL11.glTranslatef(0.0F, 0.0F, -0.1F);
						GL11.glRotatef(this.player.xRotO + (this.player.xRot - this.player.xRotO) * this.timer.renderPartialTicks, 1.0F, 0.0F, 0.0F);
						GL11.glRotatef(this.player.yRotO + (this.player.yRot - this.player.yRotO) * this.timer.renderPartialTicks, 0.0F, 1.0F, 0.0F);
						float rx = this.player.xo + (this.player.x - this.player.xo) * this.timer.renderPartialTicks;
						float ry = this.player.yo + (this.player.y - this.player.yo) * this.timer.renderPartialTicks;
						float rz = this.player.zo + (this.player.z - this.player.zo) * this.timer.renderPartialTicks;
						GL11.glTranslatef(-rx, -ry, -rz);
						Frustum clipping = Frustum.getInstance();

						for (int count = 0; count < this.levelRenderer.chunkCache.length; count++) {
							this.levelRenderer.chunkCache[count].clip(clipping);
						}

						try {
							Collections.sort(this.levelRenderer.chunks, new ChunkVisibleAndDistanceComparator(this.player));
						} catch(Exception e) {
						}

						int var98 = this.levelRenderer.chunks.size() - 1;
						int var105 = this.levelRenderer.chunks.size();
						if (var105 > 3) {
							var105 = 3;
						}

						for (int count = 0; count < var105; ++count) {
							Chunk chunk = this.levelRenderer.chunks.remove(var98 - count);
							chunk.update();
							chunk.loaded = false;
						}

						this.renderer.updateFog();
						GL11.glEnable(GL11.GL_FOG);
						this.levelRenderer.sortChunks(this.player, 0);
						if (this.level.preventsRendering(this.player.x, this.player.y, this.player.z, 0.1F)) {
							for (int var122 = (int) this.player.x - 1; var122 <= (int) this.player.x + 1; ++var122) {
								for (int var125 = (int) this.player.y - 1; var125 <= (int) this.player.y + 1; ++var125) {
									for (int var38 = (int) this.player.z - 1; var38 <= (int) this.player.z + 1; ++var38) {
										var105 = var38;
										var98 = var125;
										int var99 = this.levelRenderer.level.getTile(var122, var125, var38);
										if (var99 != 0 && Blocks.fromId(var99) != null && Blocks.fromId(var99).getPreventsRendering()) {
											GL11.glColor4f(0.2F, 0.2F, 0.2F, 1.0F);
											GL11.glDepthFunc(GL11.GL_LESS);
											ShapeRenderer.instance.begin();

											Blocks.fromId(var99).getModel().renderAll(var122, var98, var105, 0.2F);

											ShapeRenderer.instance.end();
											GL11.glCullFace(GL11.GL_FRONT);
											ShapeRenderer.instance.begin();

											Blocks.fromId(var99).getModel().renderAll(var122, var98, var105, 0.2F);

											ShapeRenderer.instance.end();
											GL11.glCullFace(GL11.GL_BACK);
											GL11.glDepthFunc(GL11.GL_LEQUAL);
										}
									}
								}
							}
						}

						this.renderer.setLighting(true);
						Vector var103 = this.renderer.getPlayerVector(this.timer.renderPartialTicks);
						this.levelRenderer.level.blockMap.render(var103, clipping, this.levelRenderer.textures, this.timer.renderPartialTicks);
						this.renderer.setLighting(false);
						this.renderer.updateFog();
						float var107 = this.timer.renderPartialTicks;
						var29 = -MathHelper.cos(this.player.yRot * 3.1415927F / 180.0F);
						var117 = -(var30 = -MathHelper.sin(this.player.yRot * 3.1415927F / 180.0F)) * MathHelper.sin(this.player.xRot * 3.1415927F / 180.0F);
						var32 = var29 * MathHelper.sin(this.player.xRot * 3.1415927F / 180.0F);
						var69 = MathHelper.cos(this.player.xRot * 3.1415927F / 180.0F);

						for (int particle = 0; particle < 2; ++particle) {
							if (this.particleManager.particles[particle].size() != 0) {
								int textureId = 0;
								if (particle == 0) {
									textureId = this.particleManager.textureManager.bindTexture("/particles.png");
								}

								if (particle == 1) {
									textureId = this.particleManager.textureManager.bindTexture("/terrain.png");
								}

								RenderHelper.getHelper().bindTexture(textureId);
								ShapeRenderer.instance.begin();

								for (int count = 0; count < this.particleManager.particles[particle].size(); ++count) {
									this.particleManager.particles[particle].get(count).render(ShapeRenderer.instance, var107, var29, var69, var30, var117, var32);
								}

								ShapeRenderer.instance.end();
							}
						}

						RenderHelper.getHelper().bindTexture("/rock.png", true);
						GL11.glEnable(GL11.GL_TEXTURE_2D);
						GL11.glCallList(this.levelRenderer.listId);
						this.renderer.updateFog();
						RenderHelper.getHelper().bindTexture("/clouds.png", true);
						GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
						var107 = (this.levelRenderer.level.cloudColor >> 16 & 255) / 255.0F;
						var29 = (this.levelRenderer.level.cloudColor >> 8 & 255) / 255.0F;
						var30 = (this.levelRenderer.level.cloudColor & 255) / 255.0F;
						if (this.settings.anaglyph) {
							var117 = (var107 * 30.0F + var29 * 59.0F + var30 * 11.0F) / 100.0F;
							var32 = (var107 * 30.0F + var29 * 70.0F) / 100.0F;
							var69 = (var107 * 30.0F + var30 * 70.0F) / 100.0F;
							var107 = var117;
							var29 = var32;
							var30 = var69;
						}

						var33 = 4.8828125E-4F;
						float var1000 = (this.levelRenderer.level.height + 2);
						var34 = (this.levelRenderer.ticks + this.timer.renderPartialTicks) * var33 * 0.03F;
						float var35 = 0;
						ShapeRenderer.instance.begin();
						ShapeRenderer.instance.color(var107, var29, var30);

						for (int var86 = -2048; var86 < this.levelRenderer.level.width + 2048; var86 += 512) {
							for (int var125 = -2048; var125 < this.levelRenderer.level.depth + 2048; var125 += 512) {
								ShapeRenderer.instance.vertexUV(var86, var1000, (var125 + 512), var86 * var33 + var34, (var125 + 512) * var33);
								ShapeRenderer.instance.vertexUV((var86 + 512), var1000, (var125 + 512), (var86 + 512) * var33 + var34, (var125 + 512) * var33);
								ShapeRenderer.instance.vertexUV((var86 + 512), var1000, var125, (var86 + 512) * var33 + var34, var125 * var33);
								ShapeRenderer.instance.vertexUV(var86, var1000, var125, var86 * var33 + var34, var125 * var33);
								ShapeRenderer.instance.vertexUV(var86, var1000, var125, var86 * var33 + var34, var125 * var33);
								ShapeRenderer.instance.vertexUV((var86 + 512), var1000, var125, (var86 + 512) * var33 + var34, var125 * var33);
								ShapeRenderer.instance.vertexUV((var86 + 512), var1000, (var125 + 512), (var86 + 512) * var33 + var34, (var125 + 512) * var33);
								ShapeRenderer.instance.vertexUV(var86, var1000, (var125 + 512), var86 * var33 + var34, (var125 + 512) * var33);
							}
						}

						ShapeRenderer.instance.end();
						GL11.glDisable(GL11.GL_TEXTURE_2D);
						ShapeRenderer.instance.begin();
						var34 = (this.levelRenderer.level.skyColor >> 16 & 255) / 255.0F;
						var35 = (this.levelRenderer.level.skyColor >> 8 & 255) / 255.0F;
						var87 = (this.levelRenderer.level.skyColor & 255) / 255.0F;
						if (this.settings.anaglyph) {
							float var36 = (var34 * 30.0F + var35 * 59.0F + var87 * 11.0F) / 100.0F;
							var69 = (var34 * 30.0F + var35 * 70.0F) / 100.0F;
							var1000 = (var34 * 30.0F + var87 * 70.0F) / 100.0F;
							var34 = var36;
							var35 = var69;
							var87 = var1000;
						}

						ShapeRenderer.instance.color(var34, var35, var87);
						var1000 = (this.levelRenderer.level.height + 10);

						for (int var125 = -2048; var125 < this.levelRenderer.level.width + 2048; var125 += 512) {
							for (int var68 = -2048; var68 < this.levelRenderer.level.depth + 2048; var68 += 512) {
								ShapeRenderer.instance.vertex(var125, var1000, var68);
								ShapeRenderer.instance.vertex((var125 + 512), var1000, var68);
								ShapeRenderer.instance.vertex((var125 + 512), var1000, (var68 + 512));
								ShapeRenderer.instance.vertex(var125, var1000, (var68 + 512));
							}
						}

						ShapeRenderer.instance.end();
						GL11.glEnable(GL11.GL_TEXTURE_2D);
						this.renderer.updateFog();
						int var108;
						if (this.renderer.mc.selected != null) {
							GL11.glDisable(GL11.GL_ALPHA_TEST);
							MovingObjectPosition var10001 = this.renderer.mc.selected;
							var105 = this.player.inventory.getSelected();
							MovingObjectPosition var102 = var10001;
							com.mojang.minecraft.render.ShapeRenderer var113 = com.mojang.minecraft.render.ShapeRenderer.instance;
							GL11.glEnable(GL11.GL_BLEND);
							GL11.glEnable(GL11.GL_ALPHA_TEST);
							GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
							GL11.glColor4f(1.0F, 1.0F, 1.0F, (MathHelper.sin(System.currentTimeMillis() / 100.0F) * 0.2F + 0.4F) * 0.5F);
							if (this.levelRenderer.cracks > 0) {
								GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
								RenderHelper.getHelper().bindTexture("/terrain.png", true);
								GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
								GL11.glPushMatrix();
								int var114 = this.levelRenderer.level.getTile(var102.x, var102.y, var102.z);
								BlockType var10000 = var114 > 0 ? Blocks.fromId(var114) : null;
								var113.begin();
								var113.noColor();
								GL11.glDepthMask(false);
								if (var10000 == null) {
									var10000 = VanillaBlock.STONE;
								}

								for (int var86 = 0; var86 < var10000.getModel().getQuads().size(); ++var86) {
									ClientRenderHelper.getHelper().drawCracks(var10000.getModel().getQuad(var86), var102.x, var102.y, var102.z, 240 + (int) (this.levelRenderer.cracks * 10.0F));
								}

								var113.end();
								GL11.glDepthMask(true);
								GL11.glPopMatrix();
							}


							GL11.glDisable(GL11.GL_BLEND);
							GL11.glDisable(GL11.GL_ALPHA_TEST);
							var10001 = this.renderer.mc.selected;
							this.player.inventory.getSelected();
							var102 = var10001;
							GL11.glEnable(GL11.GL_BLEND);
							GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
							GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
							GL11.glLineWidth(2.0F);
							GL11.glDisable(GL11.GL_TEXTURE_2D);
							GL11.glDepthMask(false);
							var29 = 0.002F;
							int block = this.levelRenderer.level.getTile(var102.x, var102.y, var102.z);
							if (block > 0) {
								AABB aabb = BlockUtils.getSelectionBox(block, var102.x, var102.y, var102.z).grow(var29, var29, var29);
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
						this.renderer.updateFog();
						GL11.glEnable(GL11.GL_TEXTURE_2D);
						GL11.glEnable(GL11.GL_BLEND);
						RenderHelper.getHelper().bindTexture("/water.png", true);
						GL11.glCallList(this.levelRenderer.listId + 1);
						GL11.glDisable(GL11.GL_BLEND);
						GL11.glEnable(GL11.GL_BLEND);
						GL11.glColorMask(false, false, false, false);
						int cy = this.levelRenderer.sortChunks(this.player, 1);
						GL11.glColorMask(true, true, true, true);
						if (this.renderer.mc.settings.anaglyph) {
							if (var77 == 0) {
								GL11.glColorMask(false, true, true, false);
							} else {
								GL11.glColorMask(true, false, false, false);
							}
						}

						if (cy > 0) {
							RenderHelper.getHelper().bindTexture("/terrain.png", true);
							GL11.glCallLists(this.levelRenderer.buffer);
						}

						GL11.glDepthMask(true);
						GL11.glDisable(GL11.GL_BLEND);
						GL11.glDisable(GL11.GL_FOG);
						if (this.renderer.mc.raining) {
							int x = (int) this.player.x;
							int y = (int) this.player.y;
							int z = (int) this.player.z;
							GL11.glDisable(GL11.GL_CULL_FACE);
							GL11.glNormal3f(0, 1, 0);
							GL11.glEnable(GL11.GL_BLEND);
							GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
							RenderHelper.getHelper().bindTexture("/rain.png", true);

							for (int cx = x - 5; cx <= x + 5; ++cx) {
								for (int cz = z - 5; cz <= z + 5; ++cz) {
									cy = this.level.getHighestTile(cx, cz);
									int var86 = y - 5;
									int var125 = y + 5;
									if (var86 < cy) {
										var86 = cy;
									}

									if (var125 < cy) {
										var125 = cy;
									}

									if (var86 != var125) {
										var1000 = (((this.renderer.levelTicks + cx * 3121 + cz * 418711) % 32) + this.timer.renderPartialTicks) / 32.0F;
										float var124 = cx + 0.5F - this.player.x;
										var35 = cz + 0.5F - this.player.z;
										float var92 = (float) Math.sqrt(var124 * var124 + var35 * var35) / 5;
										GL11.glColor4f(1.0F, 1.0F, 1.0F, (1.0F - var92 * var92) * 0.7F);
										ShapeRenderer.instance.begin();
										ShapeRenderer.instance.vertexUV(cx, var86, cz, 0.0F, var86 * 2.0F / 8.0F + var1000 * 2.0F);
										ShapeRenderer.instance.vertexUV((cx + 1), var86, (cz + 1), 2.0F, var86 * 2.0F / 8.0F + var1000 * 2.0F);
										ShapeRenderer.instance.vertexUV((cx + 1), var125, (cz + 1), 2.0F, var125 * 2.0F / 8.0F + var1000 * 2.0F);
										ShapeRenderer.instance.vertexUV(cx, var125, cz, 0.0F, var125 * 2.0F / 8.0F + var1000 * 2.0F);
										ShapeRenderer.instance.vertexUV(cx, var86, (cz + 1), 0.0F, var86 * 2.0F / 8.0F + var1000 * 2.0F);
										ShapeRenderer.instance.vertexUV((cx + 1), var86, cz, 2.0F, var86 * 2.0F / 8.0F + var1000 * 2.0F);
										ShapeRenderer.instance.vertexUV((cx + 1), var125, cz, 2.0F, var125 * 2.0F / 8.0F + var1000 * 2.0F);
										ShapeRenderer.instance.vertexUV(cx, var125, (cz + 1), 0.0F, var125 * 2.0F / 8.0F + var1000 * 2.0F);
										ShapeRenderer.instance.end();
									}
								}
							}

							GL11.glEnable(GL11.GL_CULL_FACE);
							GL11.glDisable(GL11.GL_BLEND);
						}

						if (this.renderer.entity != null) {
							this.renderer.entity.renderHover(this.renderer.mc.textureManager, this.timer.renderPartialTicks);
						}

						GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
						GL11.glLoadIdentity();
						if (this.renderer.mc.settings.anaglyph) {
							GL11.glTranslatef(((var77 << 1) - 1) * 0.1F, 0.0F, 0.0F);
						}

						this.renderer.hurtEffect(this.timer.renderPartialTicks);
						if (this.renderer.mc.settings.viewBobbing) {
							this.renderer.applyBobbing(this.timer.renderPartialTicks);
						}

						var117 = this.renderer.heldBlock.lastPosition + (this.renderer.heldBlock.heldPosition - this.renderer.heldBlock.lastPosition) * this.timer.renderPartialTicks;
						GL11.glPushMatrix();
						GL11.glRotatef(this.player.xRotO + (this.player.xRot - this.player.xRotO) * this.timer.renderPartialTicks, 1.0F, 0.0F, 0.0F);
						GL11.glRotatef(this.player.yRotO + (this.player.yRot - this.player.yRotO) * this.timer.renderPartialTicks, 0.0F, 1.0F, 0.0F);
						this.renderer.setLighting(true);
						GL11.glPopMatrix();
						GL11.glPushMatrix();
						var69 = 0.8F;
						if (this.renderer.heldBlock.moving) {
							var33 = MathHelper.sin((var1000 = (this.renderer.heldBlock.heldOffset + this.timer.renderPartialTicks) / 7.0F) * 3.1415927F);
							GL11.glTranslatef(-MathHelper.sin((float) Math.sqrt(var1000) * 3.1415927F) * 0.4F, MathHelper.sin((float) Math.sqrt(var1000) * 3.1415927F * 2.0F) * 0.2F, -var33 * 0.2F);
						}

						GL11.glTranslatef(0.7F * var69, -0.65F * var69 - (1.0F - var117) * 0.6F, -0.9F * var69);
						GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
						GL11.glEnable(GL11.GL_NORMALIZE);
						if (this.renderer.heldBlock.moving) {
							var33 = MathHelper.sin((var1000 = (this.renderer.heldBlock.heldOffset + this.timer.renderPartialTicks) / 7.0F) * var1000 * 3.1415927F);
							GL11.glRotatef(MathHelper.sin((float) Math.sqrt(var1000) * 3.1415927F) * 80.0F, 0.0F, 1.0F, 0.0F);
							GL11.glRotatef(-var33 * 20.0F, 1.0F, 0.0F, 0.0F);
						}

						float brightness = this.level.getBrightness((int) this.player.x, (int) this.player.y, (int) this.player.z);
						GL11.glColor4f(brightness, brightness, brightness, 1);
						com.mojang.minecraft.render.ShapeRenderer var123 = com.mojang.minecraft.render.ShapeRenderer.instance;

						if(!this.hideGui) {
							if (this.renderer.heldBlock.block != null) {
								var34 = 0.4F;
								GL11.glScalef(0.4F, var34, var34);
								GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
								this.renderer.heldBlock.block.getModel().renderAll(0, 0, 0, this.level.getBrightness((int) this.player.x, (int) this.player.y, (int) this.player.z));
							} else {
								this.player.bindTexture(this.textureManager);
								GL11.glScalef(1.0F, -1.0F, -1.0F);
								GL11.glTranslatef(0.0F, 0.2F, 0.0F);
								GL11.glRotatef(-120.0F, 0.0F, 0.0F, 1.0F);
								GL11.glScalef(1.0F, 1.0F, 1.0F);
								ModelPart arm = this.player.getModel().leftArm;
								if (!arm.hasList) {
									arm.generateList(0.0625F);
								}

								GL11.glCallList(arm.list);
							}
						}

						GL11.glDisable(GL11.GL_NORMALIZE);
						GL11.glPopMatrix();
						this.renderer.setLighting(false);
						if (!this.renderer.mc.settings.anaglyph) {
							break;
						}

						var77++;
					}

					this.hud.render(this.timer.renderPartialTicks, this.currentScreen != null, Mouse.getX() * width / this.width, height - Mouse.getY() * height / this.height - 1);
				} else {
					GL11.glViewport(0, 0, this.width, this.height);
					GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
					GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
					GL11.glMatrixMode(GL11.GL_PROJECTION);
					GL11.glLoadIdentity();
					GL11.glMatrixMode(GL11.GL_MODELVIEW);
					GL11.glLoadIdentity();
					this.renderer.enableGuiMode();
				}

				if (this.currentScreen != null) {
					this.currentScreen.render();
				}

				if(this.progressBar.isVisible()) {
					this.progressBar.render(false);
				}

				Thread.yield();
				Display.update();

				if (this.settings.limitFPS) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
					}
				}

				checkGLError("Post render");
				fps++;

				while (System.currentTimeMillis() >= lastUpdate + 1000) {
					this.debugInfo = fps + " fps, " + Chunk.chunkUpdates + " chunk updates";
					com.mojang.minecraft.render.Chunk.chunkUpdates = 0;
					lastUpdate += 1000;
					fps = 0;
				}
			}
		}

		ShaderManager.cleanup();
		this.shutdown();
		return;
	}

	public final void grabMouse() {
		if (!Mouse.isGrabbed()) {
			Mouse.setGrabbed(true);
			this.setCurrentScreen(null);
			this.lastClick = this.ticks + 10000;
		}
	}

	public final void displayMenu() {
		if (this.currentScreen == null && this.ingame && (!this.isInMultiplayer() || this.session.isConnected() && this.session.getState() == State.GAME)) {
			this.setCurrentScreen(new MenuScreen());
		}
	}

	private void onMouseClick(int button) {
		if (button != 0 || this.blockHitTime <= 0) {
			if (button == 0) {
				this.renderer.heldBlock.heldOffset = -1;
				this.renderer.heldBlock.moving = true;
			}

			int selected = this.player.inventory.getSelected();
			if (button == 1 && selected > 0 && this.mode.useItem(this.player, selected)) {
				this.renderer.heldBlock.heldPosition = 0;
			} else if (this.selected == null) {
				if (button == 0 && !(this.mode instanceof CreativeGameMode)) {
					this.blockHitTime = 10;
				}

			} else {
				if (this.selected.entityPos) {
					if (button == 0) {
						this.selected.entity.hurt(this.player, 4);
						return;
					}
				} else {
					int x = this.selected.x;
					int y = this.selected.y;
					int z = this.selected.z;
					if (button != 0) {
						if (this.selected.side == 0) {
							--y;
						}

						if (this.selected.side == 1) {
							++y;
						}

						if (this.selected.side == 2) {
							--z;
						}

						if (this.selected.side == 3) {
							++z;
						}

						if (this.selected.side == 4) {
							--x;
						}

						if (this.selected.side == 5) {
							++x;
						}
					}

					if (button == 0) {
						if (this.level != null && (Blocks.fromId(this.level.getTile(x, y, z)) != VanillaBlock.BEDROCK || this.player.userType >= 100)) {
							this.mode.hitBlock(x, y, z);
							return;
						}
					} else {
						int id = this.player.inventory.getSelected();
						if (id <= 0) {
							return;
						}

						if(this.player.openclassic.getPlaceMode() > 0) {
							id = this.player.openclassic.getPlaceMode();
						}

						BlockType block = this.level.openclassic.getBlockTypeAt(x, y, z);
						AABB collision = BlockUtils.getCollisionBox(id, x, y, z);
						if ((block == null || block.canPlaceIn()) && (collision == null || (!this.player.bb.intersects(collision) && this.level.isFree(collision)))) {
							if (!this.mode.canPlace(id)) {
								return;
							}

							if (this.isInMultiplayer()) {
								this.session.send(new PlayerSetBlockMessage((short) x, (short) y, (short) z, button == 1, (byte) id));
							}

							if(!this.isInMultiplayer() && EventManager.callEvent(new BlockPlaceEvent(this.level.openclassic.getBlockAt(x, y, z), OpenClassic.getClient().getPlayer(), this.renderer.heldBlock.block)).isCancelled()) {
								return;
							}

							this.level.netSetTile(x, y, z, id);
							this.renderer.heldBlock.heldPosition = 0;
							if(Blocks.fromId(id) != null && Blocks.fromId(id).getPhysics() != null) {
								Blocks.fromId(id).getPhysics().onPlace(this.level.openclassic.getBlockAt(x, y, z));
							}

							BlockType type = Blocks.fromId(id);
							if (type != null && type.getStepSound() != StepSound.NONE) {
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
		((ClassicScheduler) OpenClassic.getGame().getScheduler()).tick();

		if (this.currentScreen != null) {
			this.lastClick = this.ticks + 10000;
		}

		if (this.currentScreen != null) {
			while (Mouse.next()) {
				if (this.currentScreen != null && Mouse.getEventButtonState()) {
					int x = Mouse.getEventX() * this.currentScreen.getWidth() / this.width;
					int y = this.currentScreen.getHeight() - Mouse.getEventY() * this.currentScreen.getHeight() / this.height - 1;
					this.currentScreen.onMouseClick(x, y, Mouse.getEventButton());
				}
			}

			if(this.currentScreen != null) {
				while (Keyboard.next()) {
					if (Keyboard.getEventKeyState()) {
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

		if (System.currentTimeMillis() > this.audio.lastBGM && this.audio.playMusic("bg")) {
			this.audio.lastBGM = System.currentTimeMillis() + rand.nextInt(900000) + 300000L;
		}

		this.mode.spawnMobs();
		this.hud.ticks++;

		for (int index = 0; index < this.hud.chatHistory.size(); ++index) {
			this.hud.chatHistory.get(index).time++;
		}

		RenderHelper.getHelper().bindTexture("/terrain.png", true);

		for (int index = 0; index < this.textureManager.animations.size(); index++) {
			AnimatedTexture animation = this.textureManager.animations.get(index);
			animation.anaglyph = this.textureManager.settings.anaglyph;
			animation.animate();

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

		if (this.isInMultiplayer()) {
			if(this.currentScreen instanceof ErrorScreen) {
				this.progressBar.setVisible(false);
			} else {
				if (!this.session.isConnected()) {
					this.progressBar.setVisible(true);
					this.progressBar.setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.multiplayer"));
					this.progressBar.setSubtitle(OpenClassic.getGame().getTranslator().translate("connecting.connect"));
					this.progressBar.setProgress(-1);
					this.progressBar.render();
				} else {
					if (this.session.connectSuccess()) {
						try {
							this.session.tick();
						} catch (Exception e) {
							e.printStackTrace();
							this.session.disconnect(e.toString());
							this.session = null;
						}
					}

					if (this.isInMultiplayer() && this.session.getState() == State.GAME) {
						this.session.send(new PlayerTeleportMessage((byte) -1, this.player.x, this.player.y, this.player.z, this.player.yRot, this.player.xRot));
					}
				}
			}
		}

		if (this.currentScreen == null && this.player != null && this.player.health <= 0) {
			this.setCurrentScreen(null);
		}

		while (Keyboard.next()) {
			this.player.setKey(Keyboard.getEventKey(), Keyboard.getEventKeyState());
			if(Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent()) {
				this.player.keyPress(Keyboard.getEventKey());
			}
			if (Keyboard.getEventKeyState()) {
				if (this.currentScreen != null) {
					if (Keyboard.getEventKeyState()) {
						this.currentScreen.onKeyPress(Keyboard.getEventCharacter(), Keyboard.getEventKey());
					}
				}

				if(Keyboard.getEventKey() == Keyboard.KEY_F6) {
					if(Display.isFullscreen()) {
						try {
							Display.setFullscreen(false);
							Display.setDisplayMode(new DisplayMode(854, 480));
						} catch (LWJGLException e) {
							e.printStackTrace();
						}
					} else {
						try {
							Display.setDisplayMode(Display.getDesktopDisplayMode());
							Display.setFullscreen(true);
						} catch (LWJGLException e) {
							e.printStackTrace();
						}
					}

					this.resize();
				}

				if(Keyboard.getEventKey() == Keyboard.KEY_F1) {
					this.hideGui = !this.hideGui;
				}

				if (this.ingame && (!this.isInMultiplayer() || this.session.isConnected() && this.session.getState() == State.GAME)) {					
					if(Keyboard.getEventKey() == Keyboard.KEY_F2) {
						GL11.glReadBuffer(GL11.GL_FRONT);

						int width = Display.getWidth();
						int height = Display.getHeight();
						ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
						GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

						File file = new File(this.dir, "screenshots/" + (new Date(System.currentTimeMillis()).toString().replaceAll(" ", "-").replaceAll(":", "-")) + ".png");
						BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

						for(int x = 0; x < width; x++) {
							for(int y = 0; y < height; y++)
							{
								int i = (x + (width * y)) * 4;
								int r = buffer.get(i) & 0xFF;
								int g = buffer.get(i + 1) & 0xFF;
								int b = buffer.get(i + 2) & 0xFF;
								image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
							}
						}

						try {
							ImageIO.write(image, "PNG", file);
							if(this.hud != null) this.hud.addChat(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("screenshot.saved"), file.getName()));
						} catch (IOException e) {
							e.printStackTrace();
							if(this.hud != null) this.hud.addChat(Color.RED + String.format(OpenClassic.getGame().getTranslator().translate("screenshot.error"), file.getName()));
						}
					}

					if(this.currentScreen == null || !this.currentScreen.grabsInput()) {
						if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
							this.displayMenu();
						}

						if (this.mode instanceof CreativeGameMode) {
							if (Keyboard.getEventKey() == this.settings.loadLocKey.key && !this.ctf) {
								PlayerRespawnEvent event = new PlayerRespawnEvent(OpenClassic.getClient().getPlayer(), new Position(OpenClassic.getClient().getLevel(), this.level.xSpawn + 0.5F, this.level.ySpawn, this.level.zSpawn + 0.5F, this.level.yawSpawn, this.level.pitchSpawn));
								if(!event.isCancelled()) {
									this.player.resetPos(event.getPosition());
								}
							}

							if (Keyboard.getEventKey() == this.settings.saveLocKey.key && !this.ctf) {
								this.level.setSpawnPos(this.player.x, this.player.y, this.player.z, this.player.yRot, this.player.xRot);
								this.player.resetPos();
							}
						}

						Keyboard.getEventKey();
						if (Keyboard.getEventKey() == Keyboard.KEY_F5) {
							this.raining = !this.raining;
						}

						if (Keyboard.getEventKey() == Keyboard.KEY_TAB && this.mode instanceof SurvivalGameMode && this.player.arrows > 0) {
							this.level.addEntity(new Arrow(this.level, this.player, this.player.x, this.player.y, this.player.z, this.player.yRot, this.player.xRot, 1.2F));
							--this.player.arrows;
						}

						if (Keyboard.getEventKey() == this.settings.buildKey.key) {
							this.mode.openInventory();
						}

						if (Keyboard.getEventKey() == this.settings.chatKey.key) {
							this.player.releaseAllKeys();
							this.setCurrentScreen(new ChatInputScreen());
						}
					}
				}

				for (int selection = 0; selection < 9; ++selection) {
					if (Keyboard.getEventKey() == selection + 2) {
						this.player.inventory.selected = selection;
					}
				}

				if (Keyboard.getEventKey() == this.settings.fogKey.key) {
					this.settings.toggleSetting(4, !Keyboard.isKeyDown(42) && !Keyboard.isKeyDown(54) ? 1 : -1);
				}
			}

			EventManager.callEvent(new PlayerKeyChangeEvent(OpenClassic.getClient().getPlayer(), Keyboard.getEventKey(), Keyboard.getEventKeyState()));
			if(this.isInMultiplayer() && this.session.isConnected() && this.openclassicServer) {
				this.session.send(new KeyChangeMessage(Keyboard.getEventKey(), Keyboard.getEventKeyState()));
			}
		}

		if (this.currentScreen == null || !this.currentScreen.grabsInput()) {
			while (Mouse.next()) {
				if (Mouse.getEventDWheel() != 0) {
					this.player.inventory.swapPaint(Mouse.getEventDWheel());
				}

				if (this.currentScreen == null) {
					if (!Mouse.isGrabbed() && Mouse.getEventButtonState()) {
						this.grabMouse();
					} else {
						if(Mouse.getEventButtonState()) {
							if (Mouse.getEventButton() == 0) {
								this.onMouseClick(0);
								this.lastClick = this.ticks;
							}

							if (Mouse.getEventButton() == 1) {
								this.onMouseClick(1);
								this.lastClick = this.ticks;
							}

							if (Mouse.getEventButton() == 2 && this.selected != null) {
								int block = this.level.getTile(this.selected.x, this.selected.y, this.selected.z);
								if (block == VanillaBlock.GRASS.getId()) {
									block = VanillaBlock.DIRT.getId();
								}

								if (block == VanillaBlock.DOUBLE_SLAB.getId()) {
									block = VanillaBlock.SLAB.getId();
								}

								if (block == VanillaBlock.BEDROCK.getId()) {
									block = VanillaBlock.STONE.getId();
								}

								this.player.inventory.grabTexture(block, this.mode instanceof CreativeGameMode);
							}
						}
					}
				}

				if (this.currentScreen != null) {
					if (Mouse.getEventButtonState()) {
						int x = Mouse.getEventX() * this.currentScreen.getWidth() / this.width;
						int y = this.currentScreen.getHeight() - Mouse.getEventY() * this.currentScreen.getHeight() / this.height - 1;
						this.currentScreen.onMouseClick(x, y, Mouse.getEventButton());
					}
				}
			}

			if (this.blockHitTime > 0) {
				this.blockHitTime--;
			}

			if (this.currentScreen == null) {
				if (Mouse.isButtonDown(0) && (this.ticks - this.lastClick) >= this.timer.tps / 4 && Mouse.isGrabbed()) {
					this.onMouseClick(0);
					this.lastClick = this.ticks;
				}

				if (Mouse.isButtonDown(1) && (this.ticks - this.lastClick) >= this.timer.tps / 4 && Mouse.isGrabbed()) {
					this.onMouseClick(1);
					this.lastClick = this.ticks;
				}
			}

			if (!this.mode.creative && this.blockHitTime <= 0) {
				if (this.currentScreen == null && Mouse.isButtonDown(0) && Mouse.isGrabbed() && this.selected != null && !this.selected.entityPos) {
					this.mode.hitBlock(this.selected.x, this.selected.y, this.selected.z, this.selected.side);
				} else {
					this.mode.resetHits();
				}
			}
		}

		if (this.level != null) {
			this.renderer.levelTicks++;
			this.renderer.heldBlock.lastPosition = this.renderer.heldBlock.heldPosition;
			if (this.renderer.heldBlock.moving) {
				this.renderer.heldBlock.heldOffset++;
				if (this.renderer.heldBlock.heldOffset == 7) {
					this.renderer.heldBlock.heldOffset = 0;
					this.renderer.heldBlock.moving = false;
				}
			}

			int id = this.player.inventory.getSelected();
			BlockType block = null;
			if (id > 0) {
				block = Blocks.fromId(id);
			}

			block = this.player.openclassic != null && this.player.openclassic.getPlaceMode() != 0 ? Blocks.fromId(this.player.openclassic.getPlaceMode()) : block;

			float position = (block == this.renderer.heldBlock.block ? 1.0F : 0.0F) - this.renderer.heldBlock.heldPosition;
			if (position < -0.4F) {
				position = -0.4F;
			}

			if (position > 0.4F) {
				position = 0.4F;
			}

			this.renderer.heldBlock.heldPosition += position;
			if (this.renderer.heldBlock.heldPosition < 0.1F) {
				this.renderer.heldBlock.block = block;
			}

			if (this.raining) {
				for (int count = 0; count < 50; ++count) {
					int x = (int) this.player.x + this.renderer.rand.nextInt(9) - 4;
					int z = (int) this.player.z + this.renderer.rand.nextInt(9) - 4;
					int y = this.level.getHighestTile(x, z);
					if (y <= (int) this.player.y + 4 && y >= (int) this.player.y - 4) {
						float xOffset = this.renderer.rand.nextFloat();
						float zOffset = this.renderer.rand.nextFloat();
						this.particleManager.spawnParticle(new WaterDropParticle(this.level, x + xOffset, y + 0.1F, z + zOffset));
					}
				}
			}

			this.levelRenderer.ticks++;
			this.level.tickEntities();
			if (!this.isInMultiplayer()) {
				this.level.tick();
			}

			this.particleManager.tickParticles();
		}

		for(Plugin plugin : OpenClassic.getClient().getPluginManager().getPlugins()) {
			plugin.tick();
		}
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
		if (level != null) {
			level.initTransient();
			this.mode.apply(level);
			level.font = this.fontRenderer;
			level.rendererContext = this;
			if (this.isInMultiplayer() && this.player != null) {
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
			if (level != null) {
				level.player = this.player;
			}
		}

		if (this.player != null) {
			this.player.input = new InputHandler(this.settings);
			this.mode.apply(this.player);
		}

		if (this.levelRenderer != null) {
			if (this.levelRenderer.level != null) {
				this.levelRenderer.level.rendererContext = null;
			}

			this.levelRenderer.level = level;
			if (level != null) {
				this.levelRenderer.refresh();
			}
		}

		if (this.particleManager != null) {
			if (level != null) {
				level.particleEngine = this.particleManager;
			}

			for (int particle = 0; particle < 2; ++particle) {
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
