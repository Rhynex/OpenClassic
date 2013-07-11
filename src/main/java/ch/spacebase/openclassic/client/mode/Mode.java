package ch.spacebase.openclassic.client.mode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.inventory.ItemStack;
import ch.spacebase.openclassic.api.item.Item;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.gui.inventory.PlayerInventoryScreen;
import ch.spacebase.openclassic.client.gui.ChatInputScreen;
import ch.spacebase.openclassic.client.gui.ClassicMainScreen;
import ch.spacebase.openclassic.client.gui.MenuScreen;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.math.Intersection;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;
import ch.spacebase.openclassic.client.render.Frustrum;
import ch.spacebase.openclassic.client.render.animation.Animation;
import ch.spacebase.openclassic.client.util.Selection;

public abstract class Mode {

	private ClientPlayer player;
	private ClientLevel level;
	private long lastChange = System.currentTimeMillis();
	protected Selection select = new Selection();
	private ClassicMainScreen main = new ClassicMainScreen(this);
	private boolean hideGui = false;
	private HeldBlock held = new HeldBlock();
	private Random rand = new Random();
	
	private int soundCounter;
	private float hitTime;
	private int hitDelay;
	
	public Mode() {
		this.player = new ClientPlayer();
	}
	
	public void update() {
		this.player.update();
		this.level.update();
		Intersection inter = this.level.getTracer().trace(this.player.getPosition().toPosVector().add(0, Constants.EYE_HEIGHT, 0), this.player.getPosition().toDirVector(), 50);
		if(inter.isHit()) {
			BlockFace face = null;
			for(BlockFace f : BlockFace.values()) {
				if((f.getModX() == inter.getDiff().getBlockX() && f.getModX() != 0) || (f.getModY() == inter.getDiff().getBlockY() && f.getModY() != 0) || (f.getModZ() == inter.getDiff().getBlockZ() && f.getModZ() != 0)) {
					face = f;
					break;
				}
			}
			
			if(!inter.getPosition().equals(this.select.getPosition())) this.selectionChanged();
			this.select.set(inter.getPosition(), face);
		} else {
			this.select.set(null, null);
		}
		
		this.held.update(this.player.getInventory().getHeldItem() != null ? this.player.getInventory().getHeldItem().getItem() : null);
		this.main.update();
		this.hitting(OpenClassic.getClient().getCurrentScreen() == null && Mouse.isButtonDown(0) && this.select.isValid());
	}
	
	private void updateAnimation(Animation anim) {
		anim.refresh();
		ByteBuffer buffer = BufferUtils.createByteBuffer(anim.getPixelData().length);
		buffer.put(anim.getPixelData());
		buffer.flip();
		BlockType.TERRAIN.bind();
		glTexSubImage2D(GL_TEXTURE_2D, 0, anim.getTextureId() % 16 << 4, anim.getTextureId() / 16 << 4, 16, 16, 6408, 5121, buffer);
	}
	
	public void pollInput() {
		if(this.player.isDead()) return;
		this.player.rotateY((OpenClassic.getClient().getConfig().getBoolean("options.invert-mouse", false) ? -Mouse.getDX() : Mouse.getDX()) * 2 * 0.16f);
		this.player.rotateX(-(Mouse.getDY() * 2 * 0.16f));
		
		if(Keyboard.isKeyDown(OpenClassic.getClient().getConfig().getInteger("keys.forward", Keyboard.KEY_W))) {
			this.player.setStrafe(-1.25f);
		}
		
		if(Keyboard.isKeyDown(OpenClassic.getClient().getConfig().getInteger("keys.back", Keyboard.KEY_S))) {
			this.player.setStrafe(1.25f);
		}
		
		if(Keyboard.isKeyDown(OpenClassic.getClient().getConfig().getInteger("keys.left", Keyboard.KEY_A))) {
			this.player.setForward(-1.25f);
		}
		
		if(Keyboard.isKeyDown(OpenClassic.getClient().getConfig().getInteger("keys.right", Keyboard.KEY_D))) {
			this.player.setForward(1.25f);
		}
		
		if(Keyboard.isKeyDown(OpenClassic.getClient().getConfig().getInteger("keys.jump", Keyboard.KEY_SPACE)) && (this.player.isOnGround() || this.player.isInWater() || this.player.isInLava())) {
			if(this.player.isInWater() || this.player.isInLava()) {
				this.player.getVelocity().add(0, 0.04f, 0);
			} else {
				this.player.getVelocity().setY(0.42f);
			}
		}
		
		if(Mouse.isButtonDown(0)) {
			if(System.currentTimeMillis() - this.lastChange > 200 && this.select.isValid()) {
				this.setLastChange();
				if(this.player.getInventory().getHeldItem() != null) {
					Item item = this.player.getInventory().getHeldItem().getItem();
					if(item.getPhysics() != null) {
						item.getPhysics().onLeftClick(this.player.getInventory().getHeldItem(), this.player, this.level.getBlockAt(this.select.getPosition()));
					}
				}
				
				this.held.onBlock();
				if(this.select.isValid() && this.getBreakSpeed() >= 1) {
					this.breakBlock();
				}
			}
		} else {
			this.hitting(false);
		}
		
		if(Mouse.isButtonDown(1) && System.currentTimeMillis() - this.lastChange > 200) {			
			this.setLastChange();
			Item item = this.player.getInventory().getHeldItem() != null ? this.player.getInventory().getHeldItem().getItem() : null;
			boolean interact = false;
			if(this.select.isValid()) {
				BlockType type = this.select.getPosition().getBlockType();
				if(type.getPhysics() != null && type.getPhysics().onInteracted(this.player.getInventory().getHeldItem(), this.level.getBlockAt(this.select.getPosition()))) {
					interact = true;
				} else if(item != null && item.getPhysics() != null && item.getPhysics().onRightClick(this.player.getInventory().getHeldItem(), this.player, this.level.getBlockAt(this.select.getPosition()))) {
					interact = true;
				}
			} else {
				if(item != null && item.getPhysics() != null && item.getPhysics().onRightClick(this.player.getInventory().getHeldItem(), this.player)) {
					interact = true;
				}
			}
			
			if(interact) this.held.onBlock();
		}
	}
	
	public void hitting(boolean hitting) {
		if(this.hitDelay > 0) {
			this.hitDelay--;
			return;
		}
		
		if(hitting) {
			this.hitTime += this.getBreakSpeed();
			if(this.soundCounter % 4 == 0) {
				StepSound sound = this.select.getPosition().getBlockType().getStepSound();
				OpenClassic.getClient().getAudioManager().playSound(sound.getSound(), this.select.getPosition().getX(), this.select.getPosition().getY(), this.select.getPosition().getZ(), (sound.getVolume() + 1.0F) / 8F, sound.getPitch() * 0.5F);
			}
			
			this.soundCounter++;
			if(this.hitTime >= 1) {
				this.breakBlock();
				this.hitTime = 0;
				this.soundCounter = 0;
				this.hitDelay = 5;
			}
			
			if(this.select.isValid()) ClientRenderHelper.getHelper().spawnBlockParticles(this.select.getPosition().getBlockType(), this.level, this.select.getPosition(), this.select.getFace());
		} else {
			this.hitTime = 0;
			this.soundCounter = 0;
		}
	}
	
	protected float getBreakSpeed() {
		BlockType type = this.select.getPosition().getBlockType();
		Item item = this.player.getInventory().getHeldItem() != null ? this.player.getInventory().getHeldItem().getItem() : null;
		if(type.getPhysics() != null && !type.getPhysics().canHarvest(item)) {
			return 1f / type.getHardness() / 100f;
		} else {
			float playerSpeed = item != null && item.getPhysics() != null ? item.getPhysics().getBreakSpeed(this.select.getPosition().getBlockType()) : 1;
			if(this.player.isHeadInWater()) playerSpeed /= 5;
			if(!this.player.isOnGround()) playerSpeed /= 5;
			return playerSpeed / type.getHardness() / 30f;
		}
	}
	
	protected void selectionChanged() {
		this.hitTime = 0;
		this.soundCounter = 0;
	}
	
	public void breakBlock() {
		BlockType old = this.select.getPosition().getBlockType();
		this.level.setBlockAt(this.select.getPosition().getBlockX(), this.select.getPosition().getBlockY(), this.select.getPosition().getBlockZ(), VanillaBlock.AIR);
		ItemStack held = this.player.getInventory().getHeldItem();
		if((old.getDropChance() == 0 || this.rand.nextInt(old.getDropChance()) == 0) && (old.getPhysics() == null || old.getPhysics().canHarvest(held != null ? held.getItem() : null))) {
			for(ItemStack drop : old.getDrops()) {
				this.player.getInventory().add(drop.clone());
			}
		}
		
		if(held != null && held.getItem().getMaxDamage() > 0) {
			held.damage();
			if(held.getDamage() > held.getItem().getMaxDamage()) {
				held.setSize(held.getSize() - 1);
				held.setDamage(0);
				if(held.getSize() <= 0) this.player.getInventory().setHeldItem(null);
			}
		}
		
		ClientRenderHelper.getHelper().spawnDestructionParticles(old, this.level, this.select.getPosition());
		if (old.getStepSound() != StepSound.NONE) {
			if(old.getStepSound() == StepSound.SAND) {
				OpenClassic.getClient().getAudioManager().playSound(StepSound.GRAVEL.getSound(), this.select.getPosition().getBlockX(), this.select.getPosition().getBlockY(), this.select.getPosition().getBlockZ(), (StepSound.GRAVEL.getVolume() + 1) / 2f, StepSound.GRAVEL.getPitch() * 0.8f);
			} else {
				OpenClassic.getClient().getAudioManager().playSound(old.getStepSound().getSound(), this.select.getPosition().getBlockX(), this.select.getPosition().getBlockY(), this.select.getPosition().getBlockZ(), (old.getStepSound().getVolume() + 1) / 2f, old.getStepSound().getPitch() * 0.8f);
			}
		}
		
		this.onBreak(this.select.getPosition().getBlockX(), this.select.getPosition().getBlockY(), this.select.getPosition().getBlockZ(), old);
	}
	
	public void setLastChange() {
		this.lastChange = System.currentTimeMillis();
	}
	
	public void onKeyboard(int key) {
		if(key == Keyboard.KEY_ESCAPE) {
			OpenClassic.getClient().setCurrentScreen(new MenuScreen());
		}
		
		if(key == OpenClassic.getClient().getConfig().getInteger("keys.togglefog", Keyboard.KEY_F)) {
			OpenClassic.getClient().getConfig().setValue("options.view-distance", OpenClassic.getClient().getConfig().getInteger("options.view-distance", 0) + 1 & 3);
			try {
				OpenClassic.getClient().getConfig().save();
			} catch (IOException e) {
				OpenClassic.getLogger().severe("Failed to save config!");
				e.printStackTrace();
			}
		}
		
		if(key == Keyboard.KEY_F1) {
			this.hideGui = !this.hideGui;
		}
		
		if(Keyboard.getEventKey() == Keyboard.KEY_F2) {
			GL11.glReadBuffer(GL11.GL_FRONT);

			int width = Display.getWidth();
			int height = Display.getHeight();
			ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
			GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

			File file = new File(OpenClassic.getClient().getDirectory(), "screenshots/" + (new Date(System.currentTimeMillis()).toString().replaceAll(" ", "-").replaceAll(":", "-")) + ".png");
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
				this.main.addChat(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("screenshot.saved"), file.getName()));
			} catch (IOException e) {
				e.printStackTrace();
				this.main.addChat(Color.RED + String.format(OpenClassic.getGame().getTranslator().translate("screenshot.error"), file.getName()));
			}
		}
		
		if(key == OpenClassic.getClient().getConfig().getInteger("keys.inventory", Keyboard.KEY_I)) {
			OpenClassic.getClient().setCurrentScreen(new PlayerInventoryScreen());
		}
		
		if(key == OpenClassic.getClient().getConfig().getInteger("keys.chat", Keyboard.KEY_T)) {
			OpenClassic.getClient().setCurrentScreen(new ChatInputScreen());
		}
		
		for(int k = 1; k <= 9; k++) {
			if(key == k + 1) {
				this.player.getInventory().setSelectedSlot(k - 1);
			}
		}
	}
	
	public void onScroll(int delta) {
		this.player.getInventory().scroll(delta > 0 ? -1 : delta < 0 ? 1 : 0);
	}
	
	public void onClick(int x, int y, int button) {
	}
	
	public void renderUpdate(float delta) {
		this.updateAnimation(Animation.LAVA);
		this.updateAnimation(Animation.WATER);
		this.level.getRenderer().update();
	}
	
	public void renderPerspective(float delta) {
		this.player.look(delta);
		Frustrum.update();
		glEnable(GL_FOG);
		this.level.render(delta);
		glDisable(GL_FOG);
		glPushMatrix();
		glBlendFunc(GL_DST_COLOR, GL_SRC_COLOR);
		glDepthMask(false);
		if(this.hitTime > 0 && this.select.isValid()) {
			int tex = 240 + (int) (this.hitTime * 10);
			BlockType type = this.select.getPosition().getBlockType();
			type.getModel().render(type, this.select.getPosition().getX(), this.select.getPosition().getY(), this.select.getPosition().getZ(), this.level.getBrightness(this.select.getPosition().getBlockX(), this.select.getPosition().getBlockY(), this.select.getPosition().getBlockZ()), BlockType.TERRAIN.getSubTexture(tex, Constants.TERRAIN_SIZE, Constants.TERRAIN_SIZE));
		}
		
		glDepthMask(true);
		glPopMatrix();
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		if(this.select.isValid()) {
			this.select.render();
		}
		
		glPushMatrix();
		glClear(GL_DEPTH_BUFFER_BIT);
		glLoadIdentity();
		this.held.render(this.level.getBrightness(this.player.getPosition().getBlockX(), this.player.getPosition().getBlockY(), this.player.getPosition().getBlockZ()), delta);
		glPopMatrix();
	}
	
	public void renderOrtho(int width, int height) {
		if(!this.main.isSetup()) this.main.setup(width, height);
		if(!this.hideGui) {
			this.main.render();
		}
	}
	
	public void onBreak(int x, int y, int z, BlockType old) {
		if(old.getPhysics() != null) {
			old.getPhysics().onBreak(this.getLevel().getBlockAt(x, y, z));
		}
	}
	
	public void unload() {
		this.main.clearWidgets();
	}
	
	public ClassicMainScreen getMainScreen() {
		return this.main;
	}
	
	public ClientPlayer getPlayer() {
		return this.player;
	}
	
	public ClientLevel getLevel() {
		return this.level;
	}
	
	public void setLevel(ClientLevel level) {
		this.level = level;
	}

	public boolean isInGame() {
		return this.getLevel() != null;
	}
	
	public Selection getSelection() {
		return this.select;
	}
	
}
