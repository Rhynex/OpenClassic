package ch.spacebase.openclassic.client.player;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.model.BoundingBox;
import ch.spacebase.openclassic.api.block.model.CuboidModel;
import ch.spacebase.openclassic.api.component.BasicComponentHolder;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.permissions.Group;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;
import ch.spacebase.openclassic.api.render.RenderHelper;

public class OtherPlayer extends BasicComponentHolder implements Player {

	private static final float BOUNDING_WIDTH = 0.6f;
	private static final float BOUNDING_HEIGHT = 1.8f;
	
	private CuboidModel model = new CuboidModel(BlockType.TERRAIN, 1, 0.3f, 0, 0.3f, 0.9f, 1.85f, 0.9f);
	private BoundingBox bb = new BoundingBox(BOUNDING_WIDTH / 2, 0, BOUNDING_WIDTH / 2, BOUNDING_WIDTH + BOUNDING_WIDTH / 2, BOUNDING_HEIGHT, BOUNDING_WIDTH + BOUNDING_WIDTH / 2);
	private byte playerId;
	private String displayName;
	private String name;
	private Position pos;
	
	public OtherPlayer(byte playerId, String name, Position pos) {
		this.playerId = playerId;
		this.displayName = name;
		this.name = Color.stripColor(name);
		this.pos = pos;
	}
	
	public void update() {
		((ClientPlayer) OpenClassic.getClient().getPlayer()).push(this);
	}
	
	public void render(float delta) {
		double dist = this.pos.distanceSquared(OpenClassic.getClient().getPlayer().getPosition());
		if(dist < 4096) {
			this.model.renderAll(this.pos.getX() - 0.5f, this.pos.getY() - 0.95f, this.pos.getZ() - 0.5f, 1); // TODO: Create a proper model. Any help would be appreciated :p
		
			glPushMatrix();
			glTranslatef(this.pos.getX(), this.pos.getY() + 1.4875f, this.pos.getZ());
			glNormal3f(0, 1, 0);
			glRotatef(-OpenClassic.getClient().getPlayer().getPosition().getYaw(), 0, 1, 0);
			glRotatef(-OpenClassic.getClient().getPlayer().getPosition().getPitch(), 1, 0, 0);
			glScalef(0.05f, -0.05f, 0.05f);
			RenderHelper.getHelper().drawBox(-1 - RenderHelper.getHelper().getStringWidth(this.displayName) / 2, -1f, RenderHelper.getHelper().getStringWidth(this.displayName) / 2, 7.99f, Integer.MIN_VALUE);
			if(this.name.equalsIgnoreCase("Notch") || this.name.equalsIgnoreCase("Steveice10")) {
				RenderHelper.getHelper().renderTextNoShadow(this.displayName, 0, 0, 16776960, true);
			} else {
				RenderHelper.getHelper().renderTextNoShadow(this.displayName, 0, 0, true);
			}
	
			glDepthFunc(GL_GREATER);
			glDepthMask(false);
			RenderHelper.getHelper().drawBox(-1 - RenderHelper.getHelper().getStringWidth(this.displayName) / 2, -1f, RenderHelper.getHelper().getStringWidth(this.displayName) / 2, 7.99f, Integer.MIN_VALUE);
			RenderHelper.getHelper().renderTextNoShadow(this.displayName, 0, 0, 5263440, true);
			glDepthMask(true);
			glDepthFunc(GL_LEQUAL);
			glPopMatrix();
		}
	}
	
	public void move(float x, float y, float z) {
		this.bb.move(x, y, z);
		this.pos.setX((this.bb.getX1() + this.bb.getX2()) / 2f);
		this.pos.setY(this.bb.getY1());
		this.pos.setZ((this.bb.getZ1() + this.bb.getZ2()) / 2f);
	}
	
	public BoundingBox getBoundingBox() {
		return this.bb;
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
	}

	@Override
	public boolean hasPermission(String permission) {
		return false;
	}

	@Override
	public String getCommandPrefix() {
		return "/";
	}

	@Override
	public String getLanguage() {
		return OpenClassic.getGame().getLanguage();
	}

	@Override
	public Session getSession() {
		return null;
	}

	@Override
	public byte getPlayerId() {
		return this.playerId;
	}

	@Override
	public Position getPosition() {
		return this.pos;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public void setDisplayName(String name) {
		this.displayName = name;
	}

	@Override
	public byte getPlaceMode() {
		return 0;
	}

	@Override
	public void setPlaceMode(int type) {
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
		return "";
	}

	@Override
	public SocketAddress getAddress() {
		return null;
	}

	@Override
	public void disconnect(String reason) {
	}

	@Override
	public boolean hasCustomClient() {
		return false;
	}

	@Override
	public String getClientVersion() {
		return "";
	}

	@Override
	public NBTData getData() {
		return null;
	}

	@Override
	public List<RemotePluginInfo> getPlugins() {
		return new ArrayList<RemotePluginInfo>();
	}

	@Override
	public void chat(String message) {
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

}
