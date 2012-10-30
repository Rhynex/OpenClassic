package ch.spacebase.openclassic.client.mode;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexSubImage2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.BoundingBox;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.gui.BlockSelectScreen;
import ch.spacebase.openclassic.client.gui.ChatInputScreen;
import ch.spacebase.openclassic.client.gui.ClassicMainScreen;
import ch.spacebase.openclassic.client.gui.MenuScreen;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.math.Intersection;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.player.OtherPlayer;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;
import ch.spacebase.openclassic.client.render.Frustrum;
import ch.spacebase.openclassic.client.render.animation.Animation;
import ch.spacebase.openclassic.client.util.Selection;

public abstract class Mode {

	private ClientPlayer player;
	private ClientLevel level;
	private long lastChange = System.currentTimeMillis();
	private Selection select = new Selection();
	private ClassicMainScreen main = new ClassicMainScreen(this);
	private HeldBlock held = new HeldBlock();
	
	public Mode() {
		this.player = new ClientPlayer();
	}
	
	public void update() {
		this.player.update();
		this.level.update();
		Intersection inter = this.level.getTracer().trace(this.player.getPosition().toPosVector().add(0, 1.65f, 0), this.player.getPosition().toDirVector(), 50);
		if(inter.isHit()) {
			BlockFace face = null;
			for(BlockFace f : BlockFace.values()) {
				if((f.getModX() == inter.getDiff().getBlockX() && f.getModX() != 0) || (f.getModY() == inter.getDiff().getBlockY() && f.getModY() != 0) || (f.getModZ() == inter.getDiff().getBlockZ() && f.getModZ() != 0)) {
					face = f;
					break;
				}
			}
			
			this.select.set(inter.getPosition(), face);
		} else {
			this.select.set(null, null);
		}
		
		this.held.update(this.player.getPlaceMode() != null ? this.player.getPlaceMode() : this.player.getQuickBar().getBlock(this.player.getQuickBar().getSelected()));
		this.main.update();
	}
	
	private void updateAnimation(Animation anim) {
		anim.refresh();

		ByteBuffer buffer = BufferUtils.createByteBuffer(anim.getPixelData().length);
		buffer.put(anim.getPixelData());
		buffer.flip();
		RenderHelper.getHelper().bindTexture("/level/terrain.png", true);
		glTexSubImage2D(GL_TEXTURE_2D, 0, anim.getTextureId() % 16 << 4, anim.getTextureId() / 16 << 4, 16, 16, 6408, 5121, buffer);
	}
	
	public void pollInput() {
		this.player.rotateY((OpenClassic.getClient().getConfig().getBoolean("options.invert-mouse", false) ? -Mouse.getDX() : Mouse.getDX()) * 2 * 0.16f);
		this.player.rotateX(-(Mouse.getDY() * 2 * 0.16f));
		
		if(Keyboard.isKeyDown(OpenClassic.getClient().getConfig().getInteger("keys.forward", Keyboard.KEY_W))) {
			this.player.setZRel(-1.25f);
		}
		
		if(Keyboard.isKeyDown(OpenClassic.getClient().getConfig().getInteger("keys.back", Keyboard.KEY_S))) {
			this.player.setZRel(1.25f);
		}
		
		if(Keyboard.isKeyDown(OpenClassic.getClient().getConfig().getInteger("keys.left", Keyboard.KEY_A))) {
			this.player.setXRel(-1.25f);
		}
		
		if(Keyboard.isKeyDown(OpenClassic.getClient().getConfig().getInteger("keys.right", Keyboard.KEY_D))) {
			this.player.setXRel(1.25f);
		}
		
		if(Keyboard.isKeyDown(OpenClassic.getClient().getConfig().getInteger("keys.jump", Keyboard.KEY_SPACE)) && (this.player.isOnGround() || this.player.isInWater() || this.player.isInLava())) {
			if(this.player.isInWater() || this.player.isInLava()) {
				this.player.getVelocity().add(0, 0.04f, 0);
			} else {
				this.player.getVelocity().setY(0.42f);
			}
		}
		
		if(Mouse.isButtonDown(0) && System.currentTimeMillis() - this.lastChange > 150 && this.select.isValid() && !(this.level.getBlockTypeAt(this.select.getPosition().getBlockX(), this.select.getPosition().getBlockY(), this.select.getPosition().getBlockZ()) == VanillaBlock.BEDROCK && !this.player.isOp())) {
			this.lastChange = System.currentTimeMillis();
			this.held.onBlock();
			BlockType old = this.level.getBlockTypeAt(this.select.getPosition().getBlockX(), this.select.getPosition().getBlockY(), this.select.getPosition().getBlockZ());
			this.level.setBlockAt(this.select.getPosition().getBlockX(), this.select.getPosition().getBlockY(), this.select.getPosition().getBlockZ(), VanillaBlock.AIR);
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
		
		if(Mouse.isButtonDown(1) && System.currentTimeMillis() - this.lastChange > 150 && this.select.isValid()) {			
			this.lastChange = System.currentTimeMillis();
			BlockType type = this.level.getBlockTypeAt(this.select.getPosition().getBlockX() + this.select.getFace().getModX(), this.select.getPosition().getBlockY() + this.select.getFace().getModY(), this.select.getPosition().getBlockZ() + this.select.getFace().getModZ());
			BlockType placing = this.player.getPlaceMode() != null ? this.player.getPlaceMode() : this.player.getQuickBar().getBlock(this.player.getQuickBar().getSelected());
			BoundingBox collision = placing.getModel().getCollisionBox(this.select.getPosition().getBlockX() + this.select.getFace().getModX(), this.select.getPosition().getBlockY() + this.select.getFace().getModY(), this.select.getPosition().getBlockZ() + this.select.getFace().getModZ());
			if(placing != VanillaBlock.AIR && (type == VanillaBlock.AIR || type.isLiquid()) && !(placing.getPhysics() != null && !placing.getPhysics().canPlace(this.level.getBlockAt(this.select.getPosition().getBlockX() + this.select.getFace().getModX(), this.select.getPosition().getBlockY() + this.select.getFace().getModY(), this.select.getPosition().getBlockZ() + this.select.getFace().getModZ())))) {
				if(collision != null) {
					if(this.player.getBoundingBox().intersects(collision)) return;
					for(Player player : this.level.getPlayers()) {
						if(((OtherPlayer) player).getBoundingBox().intersects(collision)) {
							return;
						}
					}
				}
				
				this.level.setBlockAt(this.select.getPosition().getBlockX() + this.select.getFace().getModX(), this.select.getPosition().getBlockY() + this.select.getFace().getModY(), this.select.getPosition().getBlockZ() + this.select.getFace().getModZ(), placing);
				this.held.onBlock();
				if (placing.getStepSound() != StepSound.NONE) {
					OpenClassic.getClient().getAudioManager().playSound(placing.getStepSound().getSound(), this.select.getPosition().getBlockX() + this.select.getFace().getModX(), this.select.getPosition().getBlockY() + this.select.getFace().getModY(), this.select.getPosition().getBlockZ() + this.select.getFace().getModZ(), (placing.getStepSound().getVolume() + 1) / 2f, placing.getStepSound().getPitch() * 0.8f);
				}
				
				this.onPlace(this.select.getPosition().getBlockX() + this.select.getFace().getModX(), this.select.getPosition().getBlockY() + this.select.getFace().getModY(), this.select.getPosition().getBlockZ() + this.select.getFace().getModZ(), placing);
			}
		}
		
		if(Mouse.isButtonDown(2) && this.select.isValid()) { // TODO: make this interact button
			BlockType block = this.select.getPosition().getLevel().getBlockTypeAt(this.select.getPosition());
			if(block == VanillaBlock.TNT) { // TODO: Move to physics - BlockPhysics.onInteracted?
				this.getLevel().explode(this.select.getPosition().getBlockX(), this.select.getPosition().getBlockY(), this.select.getPosition().getBlockZ(), 4);
				return;
			}
			
			if(block == VanillaBlock.DOUBLE_SLAB) block = VanillaBlock.SLAB;
			if(block == VanillaBlock.GRASS) block = VanillaBlock.DIRT;
			if(this.player.getQuickBar().contains(block)) {
				this.player.getQuickBar().setSelected(this.player.getQuickBar().getSlot(block));
			} else {
				this.player.getQuickBar().setBlock(this.player.getQuickBar().getSelected(), block);
			}
		}
	}
	
	public void onKeyboard(int key) {
		if(key == Keyboard.KEY_ESCAPE) {
			OpenClassic.getClient().setCurrentScreen(new MenuScreen());
		}
		
		if(key == OpenClassic.getClient().getConfig().getInteger("keys.select-block", Keyboard.KEY_B)) {
			OpenClassic.getClient().setCurrentScreen(new BlockSelectScreen());
		}
		
		if(key == OpenClassic.getClient().getConfig().getInteger("keys.chat", Keyboard.KEY_T)) {
			OpenClassic.getClient().setCurrentScreen(new ChatInputScreen());
		}
		
		for(int k = 1; k <= 9; k++) {
			if(key == k + 1) {
				this.player.getQuickBar().setSelected(k - 1);
			}
		}
	}
	
	public void onScroll(int delta) {
		this.player.getQuickBar().scroll(delta > 0 ? -1 : delta < 0 ? 1 : 0);
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
		this.level.render(delta);
		
		if(this.select.isValid()) {
			this.select.render();
		}
		
		glPopMatrix();
		this.held.render(this.level.getBrightness(this.player.getPosition().getBlockX(), this.player.getPosition().getBlockY(), this.player.getPosition().getBlockZ()), delta);
		glPushMatrix();
	}
	
	public void renderOrtho(int width, int height) {
		if(!this.main.isSetup()) this.main.setup(width, height);
		this.main.render();
	}
	
	public void onBreak(int x, int y, int z, BlockType old) {
		if(old.getPhysics() != null) {
			old.getPhysics().onBreak(this.getLevel().getBlockAt(x, y, z));
		}
	}
	
	public void onPlace(int x, int y, int z, BlockType type) {
		if(type.getPhysics() != null) {
			type.getPhysics().onPlace(this.getLevel().getBlockAt(x, y, z));
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
	
}
