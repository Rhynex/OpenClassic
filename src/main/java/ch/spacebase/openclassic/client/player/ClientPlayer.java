package ch.spacebase.openclassic.client.player;

import static org.lwjgl.opengl.GL11.*;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.BoundingBox;
import ch.spacebase.openclassic.api.component.BasicComponentHolder;
import ch.spacebase.openclassic.api.component.type.NBTComponent;
import ch.spacebase.openclassic.api.data.NBTData;
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
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.mode.Multiplayer;
import ch.spacebase.openclassic.client.util.LoginInfo;
import ch.spacebase.openclassic.game.component.player.PlaceModeComponent;

public class ClientPlayer extends BasicComponentHolder implements Player {

	private static final float BOUNDING_WIDTH = 0.6f;
	private static final float BOUNDING_HEIGHT = 1.8f;

	private Position pos = new Position(null, 0, 0, 0, 0, 0);
	private Vector velocity = new Vector(0, 0, 0);
	private boolean grounded = true;

	private float xrel = 0;
	private float zrel = 0;
	private float walkDist = 0;
	private float prevWalkDist = 0;
	private float nextStep = 0;
	private float bob = 0;
	private float prevBob = 0;

	private QuickBar quickbar = new QuickBar();
	private BoundingBox bb = new BoundingBox(BOUNDING_WIDTH / 2, 0, BOUNDING_WIDTH / 2, BOUNDING_WIDTH + BOUNDING_WIDTH / 2, BOUNDING_HEIGHT, BOUNDING_WIDTH + BOUNDING_WIDTH / 2);
	private boolean op = false;
	
	public ClientPlayer() {
		this.add(NBTComponent.class).load("Player", OpenClassic.getClient().getDirectory().getPath() + "/player.nbt");
		this.add(PlaceModeComponent.class);
	}
	
	public void look(float delta) {
		glRotatef(this.pos.getInterpolatedPitch(delta), 1, 0, 0);
		glRotatef(this.pos.getInterpolatedYaw(delta), 0, 1, 0);
		glTranslatef(-this.pos.getInterpolatedX(delta), -(this.pos.getInterpolatedY(delta) + Constants.EYE_HEIGHT), -this.pos.getInterpolatedZ(delta));
		
		if(OpenClassic.getClient().getConfig().getBoolean("options.view-bobbing", true)) {
			float walkDist = this.walkDist + (this.walkDist - this.prevWalkDist) * delta;
			float bob = this.bob + (this.bob - this.prevBob) * delta;
			glTranslatef(MathHelper.sin(walkDist * (float) Math.PI) * bob * 0.5f, -Math.abs(MathHelper.cos(walkDist * (float) Math.PI) * bob), 0);
		}
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

		if(oldMovX != x) {
			this.velocity.setX(0);
		}

		if(oldMovY != y) {
			this.velocity.setY(0);
		}

		if(oldMovZ != z) {
			this.velocity.setZ(0);
		}

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

	public void setXRel(float xrel) {
		this.xrel = xrel;
	}

	public void setZRel(float zrel) {
		this.zrel = zrel;
	}
	
	public void update() {
		this.xrel *= 0.6f;
		this.zrel *= 0.6f;
		this.prevBob = this.bob;
		this.prevWalkDist = this.walkDist;

		if(this.isInWater()) {
			this.updateVelocity(this.xrel, this.zrel, 0.02f);
			this.move(this.velocity.getX(), this.velocity.getY(), this.velocity.getZ());
			this.velocity.multiply(0.8f, 0.8f, 0.8f);
			this.velocity.subtract(0, 0.02f, 0);
		} else if(this.isInLava()) {
			this.updateVelocity(this.xrel, this.zrel, 0.02f);
			this.move(this.velocity.getX(), this.velocity.getY(), this.velocity.getZ());
			this.velocity.multiply(0.5f, 0.5f, 0.5f);
			this.velocity.subtract(0, 0.02f, 0);
		} else {
			this.updateVelocity(this.xrel, this.zrel, this.isOnGround() ? 0.1f : 0.02f);
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

	public void updateVelocity(float x, float z, float speed) {
		float len = (float) Math.sqrt(x * x + z * z);
		if (len >= 0.01f) {
			if (len < 1) {
				len = 1;
			}

			x *= speed / len;
			z *= speed / len;
			float modX = (float) Math.sin(this.pos.getYaw() * MathHelper.DEG_TO_RAD);
			float modZ = (float) Math.cos(this.pos.getYaw() * MathHelper.DEG_TO_RAD);

			this.velocity.add(x * modZ - z * modX, 0, z * modZ + x * modX);
		}
	}

	public boolean isInWater() { 	
		return ((ClientLevel) OpenClassic.getClient().getLevel()).contains(this.bb, VanillaBlock.WATER, VanillaBlock.STATIONARY_WATER);
	}

	public boolean isInLava() { 	
		return ((ClientLevel) OpenClassic.getClient().getLevel()).contains(this.bb, VanillaBlock.LAVA, VanillaBlock.STATIONARY_LAVA);
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
	public BlockType getPlaceMode() {
		return this.get(PlaceModeComponent.class).getPlaceMode();
	}

	@Override
	public void setPlaceMode(BlockType type) {
		this.get(PlaceModeComponent.class).setPlaceMode(type);
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

	public QuickBar getQuickBar() {
		return this.quickbar;
	}
	
}
