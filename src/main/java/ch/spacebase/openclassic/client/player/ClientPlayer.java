package ch.spacebase.openclassic.client.player;

import static org.lwjgl.opengl.GL11.*;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.BoundingBox;
import ch.spacebase.openclassic.api.component.BasicComponentHolder;
import ch.spacebase.openclassic.api.component.type.NBTComponent;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.inventory.ItemStack;
import ch.spacebase.openclassic.api.inventory.PlayerInventory;
import ch.spacebase.openclassic.api.item.Items;
import ch.spacebase.openclassic.api.item.VanillaItem;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.api.math.Vector;
import ch.spacebase.openclassic.api.network.msg.PlayerChatMessage;
import ch.spacebase.openclassic.api.permissions.Group;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.gui.GameOverScreen;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.math.Intersection;
import ch.spacebase.openclassic.client.mode.Multiplayer;
import ch.spacebase.openclassic.client.util.LoginInfo;
import ch.spacebase.opennbt.TagBuilder;
import ch.spacebase.opennbt.tag.ByteTag;
import ch.spacebase.opennbt.tag.CompoundTag;
import ch.spacebase.opennbt.tag.FloatTag;
import ch.spacebase.opennbt.tag.IntTag;
import ch.spacebase.opennbt.tag.ShortTag;

public class ClientPlayer extends BasicComponentHolder implements Player {

	private static final float BOUNDING_WIDTH = 0.6f;
	private static final float BOUNDING_HEIGHT = 1.8f;
	private static final Random rand = new Random();

	private Position pos = new Position(null, 0, 0, 0, 0, 0);
	private Vector velocity = new Vector(0, 0, 0);
	private boolean grounded = true;

	private float forward = 0;
	private float strafe = 0;
	private float walkDist = 0;
	private float prevWalkDist = 0;
	private float nextStep = 0;
	private float bob = 0;
	private float prevBob = 0;
	private int hurtTime = 0;
	private int deathTime = 0;
	private int attackYaw = 0;
	private boolean dead = false;
	private int health = Constants.MAX_PLAYER_HEALTH;
	private int air = Constants.MAX_PLAYER_AIR;
	private int prevHealth = 0;
	private int invincibleTicks = 0;
	private float fallDistance = 0;

	private BoundingBox bb = new BoundingBox(BOUNDING_WIDTH / 2, 0, BOUNDING_WIDTH / 2, BOUNDING_WIDTH + BOUNDING_WIDTH / 2, BOUNDING_HEIGHT, BOUNDING_WIDTH + BOUNDING_WIDTH / 2);
	private boolean op = false;
	private PlayerInventory inv = new PlayerInventory();
	
	public ClientPlayer() {
		this.add(NBTComponent.class).load("Player", OpenClassic.getClient().getDirectory().getPath() + "/player.nbt");
	}
	
	public void respawn() {
		this.air = Constants.MAX_PLAYER_AIR;
		this.health = Constants.MAX_PLAYER_HEALTH;
		this.dead = false;
		this.hurtTime = 0;
		this.deathTime = 0;
		this.moveTo(this.pos.getLevel().getSpawn());
	}
	
	public void load() {
		NBTData data = this.get(NBTComponent.class).getData();
		CompoundTag level = (CompoundTag) data.get(OpenClassic.getClient().getLevel().getName());
		if(level == null) return;
		CompoundTag inv = (CompoundTag) level.get("Inventory");
		if(inv == null) return;
		int slots = ((IntTag) inv.get("Slots")).getValue();
		for(int slot = 0; slot < slots; slot++) {
			CompoundTag item = (CompoundTag) inv.get("Slot" + slot);
			short id = ((ShortTag) item.get("ID")).getValue();
			if(id != -1) {
				ItemStack it = new ItemStack(Items.get(id, ((ByteTag) item.get("Data")).getValue()), ((IntTag) item.get("Size")).getValue());
				it.setDamage(((IntTag) item.get("Damage")).getValue());
				this.inv.setItem(slot, it);
			} else {
				this.inv.setItem(slot, null);
			}
		}
		
		this.inv.setItem(0, new ItemStack(VanillaBlock.WORKBENCH));
		this.inv.setItem(1, new ItemStack(VanillaBlock.FURNACE_EAST));
		this.inv.setItem(2, new ItemStack(VanillaBlock.CHEST_EAST));
		this.inv.setItem(3, new ItemStack(VanillaItem.IRON_PICKAXE));
		this.inv.setItem(4, new ItemStack(VanillaItem.COAL));
		this.inv.setItem(5, new ItemStack(VanillaBlock.LOG));
		
		this.health = ((IntTag) level.get("Health")).getValue();
		
		CompoundTag pos = (CompoundTag) level.get("Position");
		float x = ((FloatTag) pos.get("X")).getValue();
		float y = ((FloatTag) pos.get("Y")).getValue();
		float z = ((FloatTag) pos.get("Z")).getValue();
		float yaw = ((FloatTag) pos.get("Yaw")).getValue();
		float pitch = ((FloatTag) pos.get("Pitch")).getValue();
		this.moveTo(x, y, z, yaw, pitch);

		this.inv.setItem(5, new ItemStack(VanillaBlock.CHEST_EAST, 64));
		this.inv.setItem(6, new ItemStack(VanillaItem.IRON_PICKAXE));
		this.inv.setItem(7, new ItemStack(VanillaItem.COAL, 64));
		this.inv.setItem(8, new ItemStack(VanillaBlock.SPONGE, 64));
	}
	
	public void save() {
		TagBuilder inv = new TagBuilder("Inventory");
		inv.append("Slots", this.inv.getSize());
		for(int slot = 0; slot < this.getInventory().getSize(); slot++) {
			ItemStack it = this.inv.getItem(slot);
			TagBuilder item = new TagBuilder("Slot" + slot);
			item.append("ID", (short) (it != null ? it.getItem().getId() : -1));
			if(it != null) {
				item.append("Data", it.getItem().getData());
				item.append("Size", it.getSize());
				item.append("Damage", it.getDamage());
			}
			
			inv.append(item);
		}
		
		CompoundTag out = inv.toCompoundTag();
		CompoundTag level = (CompoundTag) this.get(NBTComponent.class).getData().get(OpenClassic.getClient().getLevel().getName());
		if(level == null) {
			level = new CompoundTag(OpenClassic.getClient().getLevel().getName());
			this.get(NBTComponent.class).getData().put(level);
		}
		
		level.put(out.getName(), out);
		level.put("Health", new IntTag("Health", this.health));
		
		TagBuilder pos = new TagBuilder("Position");
		pos.append("X", this.pos.getX());
		pos.append("Y", this.pos.getY());
		pos.append("Z", this.pos.getZ());
		pos.append("Yaw", this.pos.getYaw());
		pos.append("Pitch", this.pos.getPitch());
		CompoundTag res = pos.toCompoundTag();
		level.put(res.getName(), res);
		
		this.get(NBTComponent.class).getData().save(OpenClassic.getClient().getDirectory().getPath() + "/player.nbt");
	}
	
	public void look(float delta) {
		float ht = this.hurtTime - delta;
		if(this.health <= 0) {
			float det = this.deathTime + delta;
			GL11.glRotatef(40F - 8000F / (det + 200F), 0.0F, 0.0F, 1.0F);
		}

		if(ht >= 0) {
			ht /= 10;
			ht = MathHelper.sin(ht * ht * ht * ht * (float) Math.PI);
			float attyaw = this.attackYaw;
			GL11.glRotatef(-attyaw, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-ht * 14F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(attyaw, 0.0F, 1.0F, 0.0F);
		}
		
		if(OpenClassic.getClient().getConfig().getBoolean("options.view-bobbing", true)) {
			float walkDist = this.walkDist + (this.walkDist - this.prevWalkDist) * delta;
			float bob = this.bob + (this.bob - this.prevBob) * delta;
			glTranslatef(MathHelper.sin(walkDist * (float) Math.PI) * bob * 0.5f, -Math.abs(MathHelper.cos(walkDist * (float) Math.PI) * bob), 0);
		}
		
		glRotatef(this.pos.getInterpolatedPitch(delta), 1, 0, 0);
		glRotatef(this.pos.getInterpolatedYaw(delta), 0, 1, 0);
		glTranslatef(-this.pos.getInterpolatedX(delta), -(this.pos.getInterpolatedY(delta) + Constants.EYE_HEIGHT), -this.pos.getInterpolatedZ(delta));
	}
	
	public boolean isOp() {
		return this.op;
	}
	
	public void setOp(boolean op) {
		this.op = op;
	}

	public boolean isOnGround() {
		return this.grounded;
	}

	public void move(float x, float y, float z) {
		float oldX = this.pos.getX();
		float oldZ = this.pos.getZ();
		float oldMovX = x;
		float oldMovY = y;
		float oldMovZ = z;
		List<BoundingBox> boxes = ((ClientLevel) OpenClassic.getClient().getLevel()).getBoxes(this.bb.expand(x, y, z));
		for (BoundingBox box : boxes) {
			y = box.clipYCollide(this.bb, y);
		}

		this.bb.move(0, y, 0);

		for (BoundingBox box : boxes) {
			x = box.clipXCollide(this.bb, x);
		}

		this.bb.move(x, 0, 0);

		for (BoundingBox box : boxes) {
			z = box.clipZCollide(this.bb, z);
		}

		this.bb.move(0, 0, z);
		this.pos.setX((this.bb.getX1() + this.bb.getX2()) / 2f);
		this.pos.setY(this.bb.getY1());
		this.pos.setZ((this.bb.getZ1() + this.bb.getZ2()) / 2f);
		this.grounded = oldMovY != y && oldMovY < 0;
		if(this.grounded) {
			if(this.fallDistance > 0) {
				int damage = (int) Math.ceil(this.fallDistance - 3);
				if(damage > 0) {
					this.damage(damage);
					BlockType type = this.pos.clone().subtract(0, 0.2f, 0).getBlockType();
					if(type != null) {
						OpenClassic.getClient().getAudioManager().playSound(this, type.getStepSound().getSound(), type.getStepSound().getVolume() * 0.5F, type.getStepSound().getPitch() * 0.75F);
					}
				}
				
				this.fallDistance = 0;
			}
		} else if(y < 0) {
			this.fallDistance -= y;
		}
		
		if(oldMovX != x) this.velocity.setX(0);
		if(oldMovY != y) this.velocity.setY(0);
		if(oldMovZ != z) this.velocity.setZ(0);
		float xDiff = this.pos.getX() - oldX;
		float zDiff = this.pos.getZ() - oldZ;
		this.walkDist = (float) (this.walkDist + (float) Math.sqrt(xDiff * xDiff + zDiff * zDiff) * 0.6d);
		BlockType type = this.pos.getLevel().getBlockTypeAt(this.pos.getBlockX(), (int) (this.pos.getY() - 0.2f), this.pos.getBlockZ());
		if(type != null && type != VanillaBlock.AIR && !type.isLiquid() && this.walkDist > this.nextStep) {
			this.nextStep++;
			StepSound step = type.getStepSound();
			if(step != StepSound.NONE) {
				OpenClassic.getClient().getAudioManager().playSound(step.getSound(), step.getVolume() * 0.75F, step.getPitch());
			}
		}
	}

	public void setForward(float forward) {
		this.forward = forward;
	}

	public void setStrafe(float strafe) {
		this.strafe = strafe;
	}
	
	public void update() {
		if(!this.dead) {
			this.forward *= 0.6f;
			this.strafe *= 0.6f;
			this.prevBob = this.bob;
			this.prevWalkDist = this.walkDist;
	
			if(this.isInWater()) {
				this.updateVelocity(this.forward, this.strafe, 0.02f);
				this.move(this.velocity.getX(), this.velocity.getY(), this.velocity.getZ());
				this.velocity.multiply(0.8f, 0.8f, 0.8f);
				this.velocity.subtract(0, 0.02f, 0);
				this.fallDistance = 0;
			} else if(this.isInLava()) {
				this.updateVelocity(this.forward, this.strafe, 0.02f);
				this.move(this.velocity.getX(), this.velocity.getY(), this.velocity.getZ());
				this.velocity.multiply(0.5f, 0.5f, 0.5f);
				this.velocity.subtract(0, 0.02f, 0);
			} else {
				this.updateVelocity(this.forward, this.strafe, this.isOnGround() ? 0.1f : 0.02f);
				this.move(this.velocity.getX(), this.velocity.getY(), this.velocity.getZ());
				this.velocity.multiply(0.91f, 0.98f, 0.91f);
				this.velocity.subtract(0, 0.08f, 0);
				if(this.isOnGround()) {
					this.velocity.multiply(0.6f, 1, 0.6f);
				}
			}
	
			float bob = (float) Math.sqrt(this.velocity.getX() * this.velocity.getX() + this.velocity.getZ() * this.velocity.getZ());
			if(bob > 0.1f) {
				bob = 0.1f;
			}
	
			if(!this.isOnGround()) {
				bob = 0;
			}
	
			this.bob += (bob - this.bob) * 0.4f;
		}
	
		if(this.invincibleTicks > 0) this.invincibleTicks--;
		if(this.hurtTime > 0) this.hurtTime--;
		if(this.health <= 0) {
			this.deathTime++;
			if(this.deathTime == 20) {
				this.die();
			}
		}
		
		if(this.isHeadInWater() && !this.dead) {
			this.air--;
			if(this.air <= -20) {
				this.air = 0;
				this.damage(1);
			}
		} else {
			this.air = Constants.MAX_PLAYER_AIR;
		}
		
		if(this.isSuffocating() && !this.dead) {
			this.damage(1);
		}
	}

	public void updateVelocity(float forward, float strafe, float speed) {
		float len = (float) Math.sqrt(forward * forward + strafe * strafe);
		if (len >= 0.01f) {
			if (len < 1) {
				len = 1;
			}

			forward *= speed / len;
			strafe *= speed / len;
			float modX = (float) Math.sin(this.pos.getYaw() * MathHelper.DEG_TO_RAD);
			float modZ = (float) Math.cos(this.pos.getYaw() * MathHelper.DEG_TO_RAD);

			this.velocity.add(forward * modZ - strafe * modX, 0, strafe * modZ + forward * modX);
		}
	}

	public boolean isSuffocating() { 	
		return ((ClientLevel) OpenClassic.getClient().getLevel()).colliding(this.bb.cloneMove(0, Constants.EYE_HEIGHT, 0).shrink(0, Constants.EYE_HEIGHT + 0.01f, 0));
	}
	
	public boolean isInWater() { 	
		return ((ClientLevel) OpenClassic.getClient().getLevel()).containsAny(this.bb, VanillaBlock.WATER, VanillaBlock.STATIONARY_WATER);
	}
	
	public boolean isHeadInWater() { 	
		return ((ClientLevel) OpenClassic.getClient().getLevel()).containsAny(this.bb.cloneMove(0, Constants.EYE_HEIGHT, 0).shrink(0, Constants.EYE_HEIGHT - 0.01f, 0), VanillaBlock.WATER, VanillaBlock.STATIONARY_WATER);
	}

	public boolean isInLava() { 	
		return ((ClientLevel) OpenClassic.getClient().getLevel()).containsAny(this.bb, VanillaBlock.LAVA, VanillaBlock.STATIONARY_LAVA);
	}

	public void push(OtherPlayer player) {
		float xDiff = player.getPosition().getX() - this.getPosition().getX();
		float zDiff = player.getPosition().getZ() - this.getPosition().getZ();
		float sq = xDiff * xDiff + zDiff * zDiff;
		if (sq >= 0.01f && sq < 1) {
			float sqrt = (float) Math.sqrt(sq);
			xDiff /= sqrt;
			zDiff /= sqrt;
			xDiff /= sqrt;
			zDiff /= sqrt;
			xDiff *= 0.05f;
			zDiff *= 0.05f;
			xDiff *= 0.2f;
			zDiff *= 0.2f;
			
			System.out.println(sq + ", " + xDiff + ", " + zDiff);
			this.push(-xDiff, 0, -zDiff);
		}
	}
	
	public void push(float x, float y, float z) {
		this.velocity.add(x, y, z);
	}

	public Vector getVelocity() {
		return this.velocity;
	}

	public void rotateY(float amt) {
		this.pos.setYaw(this.pos.getYaw() + amt);
	}

	public void rotateX(float amt) {
		this.pos.setPitch(this.pos.getPitch() + amt);
		if(this.pos.getPitch() > 90) this.pos.setPitch(90);
		if(this.pos.getPitch() < -90) this.pos.setPitch(-90);
	}

	@Override
	public void sendMessage(String message) {
		if(OpenClassic.getClient().getMainScreen() != null) {
			OpenClassic.getClient().getMainScreen().addChat(message);
		}
	}

	@Override
	public boolean hasPermission(String permission) {
		return true;
	}

	@Override
	public String getCommandPrefix() {
		return "/";
	}

	@Override
	public String getLanguage() {
		return OpenClassic.getClient().getLanguage();
	}

	@Override
	public Session getSession() {
		if(((ClassicClient) OpenClassic.getClient()).getMode() instanceof Multiplayer) {
			return ((Multiplayer) ((ClassicClient) OpenClassic.getClient()).getMode()).getSession();
		}
		
		return null;
	}

	@Override
	public byte getPlayerId() {
		return -1;
	}

	@Override
	public Position getPosition() {
		return this.pos;
	}

	@Override
	public String getName() {
		return LoginInfo.getName();
	}

	@Override
	public String getDisplayName() {
		return LoginInfo.getName();
	}

	@Override
	public void setDisplayName(String name) {
	}

	@Override
	public void moveTo(Position pos) {
		this.moveTo(pos.getLevel(), pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());
	}

	@Override
	public void moveTo(float x, float y, float z) {
		this.moveTo(this.pos.getLevel(), x, y, z);
	}

	@Override
	public void moveTo(float x, float y, float z, float yaw, float pitch) {
		this.moveTo(this.pos.getLevel(), x, y, z, yaw, pitch);
	}

	@Override
	public void moveTo(Level level, float x, float y, float z) {
		this.moveTo(level, x, y, z, this.pos.getYaw(), this.pos.getPitch());
	}

	@Override
	public void moveTo(Level level, float x, float y, float z, float yaw, float pitch) {
		if(level != this.pos.getLevel()) {
			this.pos.setLevel(level);
		}

		this.velocity.zero();
		this.pos.set(x, y, z);
		this.pos.setYaw(yaw);
		this.pos.setPitch(pitch);
		this.pos.resetCache();
		
		this.bb = new BoundingBox(BOUNDING_WIDTH / 2, 0, BOUNDING_WIDTH / 2, BOUNDING_WIDTH + BOUNDING_WIDTH / 2, BOUNDING_HEIGHT, BOUNDING_WIDTH + BOUNDING_WIDTH / 2);
		this.bb.move(this.pos.getX(), this.pos.getY(), this.pos.getZ());
	}

	@Override
	public Group getGroup() {
		return null;
	}

	@Override
	public void setGroup(Group group) {
	}

	@Override
	public String getIp() {
		return this.getAddress() != null ? this.getAddress().toString().replace("/", "").split(":")[0] : "";
	}

	@Override
	public SocketAddress getAddress() {
		if(this.getSession() != null) {
			return this.getSession().getAddress();
		}
		
		return null;
	}

	@Override
	public void disconnect(String reason) {
		if(this.getSession() != null) {
			this.getSession().disconnect(reason);
		}
	}

	@Override
	public boolean hasCustomClient() {
		return true;
	}

	@Override
	public String getClientVersion() {
		return Constants.CLIENT_VERSION;
	}

	@Override
	public NBTData getData() {
		return this.get(NBTComponent.class).getData();
	}

	@Override
	public List<RemotePluginInfo> getPlugins() {
		if(((ClassicClient) OpenClassic.getClient()).getMode() instanceof Multiplayer) {
			return ((Multiplayer) ((ClassicClient) OpenClassic.getClient()).getMode()).getServerPlugins();
		}
		
		return new ArrayList<RemotePluginInfo>();
	}

	@Override
	public void chat(String message) {
		if(this.getSession() != null) {
			this.getSession().send(new PlayerChatMessage((byte) -1, message));
		} else if(OpenClassic.getClient().getMainScreen() != null) {
			OpenClassic.getClient().getMainScreen().addChat(this.getName() + Color.WHITE + ": " + message);
		}
	}

	@Override
	public void hidePlayer(Player player) {
	}

	@Override
	public void showPlayer(Player player) {
	}

	@Override
	public boolean canSee(Player player) {
		return true;
	}

	public BoundingBox getBoundingBox() {
		return this.bb.clone();
	}

	public PlayerInventory getInventory() {
		return this.inv;
	}

	@Override
	public Position getSelectedBlock() {
		return ((ClassicClient) OpenClassic.getClient()).getMode().getSelection().getPosition();
	}
	
	public Position getSelectedLiquid() {
		Intersection inter = ((ClientLevel) this.getPosition().getLevel()).getTracer().traceLiquid(this.getPosition().toPosVector().add(0, Constants.EYE_HEIGHT, 0), this.getPosition().toDirVector(), 50);
		if(inter.isHit()) {
			return inter.getPosition();
		}
		
		return null;
	}

	@Override
	public BlockFace getSelectedFace() {
		return ((ClassicClient) OpenClassic.getClient()).getMode().getSelection().getFace();
	}
	
	@Override
	public int getHealth() {
		return this.health;
	}

	@Override
	public void setHealth(int health) {
		if(this.dead) return;
		this.health = health;
		if(this.health <= 0) {
			this.health = 0;
			OpenClassic.getClient().setCurrentScreen(new GameOverScreen());
		} else if(this.health > Constants.MAX_PLAYER_HEALTH) {
			this.health = Constants.MAX_PLAYER_HEALTH;
		}
	}

	@Override
	public void damage(int damage) {
		if(this.dead) return;		
		if(damage > 0) {
			this.hurtTime = 10;
			this.attackYaw = (int) (Math.random() * 2D) * 180;
			if(this.invincibleTicks > 10) {
				if(this.prevHealth - damage >= this.health) return;
				this.health = this.prevHealth - damage;
			} else {
				this.prevHealth = this.health;
				this.invincibleTicks = 20;
				this.health -= damage;
				this.hurtTime = 10;
			}
			
			OpenClassic.getClient().getAudioManager().playSound("generic.hurt", 1, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
		} else {
			this.health -= damage;
			this.invincibleTicks = 10;
		}
		
		if(this.health <= 0) {
			this.health = 0;
			OpenClassic.getClient().setCurrentScreen(new GameOverScreen());
			this.inv.clear(); // TODO: drop items
		} else if(this.health > Constants.MAX_PLAYER_HEALTH) {
			this.health = Constants.MAX_PLAYER_HEALTH;
		}
	}
	
	@Override
	public void heal(int health) {
		if(this.dead) return;
		if(this.invincibleTicks > 10) return;
		if(health > 0) {
			this.health += health;
			this.invincibleTicks = 10;
		} else {
			this.hurtTime = 10;
			this.attackYaw = (int) (Math.random() * 2D) * 180;
			if(this.invincibleTicks > 10) {
				if(this.prevHealth + health >= this.health) return;
				this.health = this.prevHealth + health;
			} else {
				this.prevHealth = this.health;
				this.invincibleTicks = 20;
				this.health += health;
				this.hurtTime = 10;
			}
			
			OpenClassic.getClient().getAudioManager().playSound("generic.hurt", 1, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
		}
		
		if(this.health <= 0) {
			this.health = 0;
			OpenClassic.getClient().setCurrentScreen(new GameOverScreen());
			this.inv.clear(); // TODO: drop items
		} else if(this.health > Constants.MAX_PLAYER_HEALTH) {
			this.health = Constants.MAX_PLAYER_HEALTH;
		}
	}
	
	public boolean isDead() {
		return this.dead;
	}
	
	public void die() {
		this.dead = true;
	}
	
	public int getInvincibleTicks() {
		return this.invincibleTicks;
	}
	
	public int getPrevHealth() {
		return this.prevHealth;
	}
	
	@Override
	public int getAir() {
		return this.air;
	}

	@Override
	public void setAir(int air) {
		this.air = air;
	}
	
}
