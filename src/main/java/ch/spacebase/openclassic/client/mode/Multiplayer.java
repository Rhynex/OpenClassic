package ch.spacebase.openclassic.client.mode;

import java.util.ArrayList;
import java.util.List;


import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.network.msg.PlayerSetBlockMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.util.LoginInfo;

public class Multiplayer extends Mode {

	private List<RemotePluginInfo> serverPlugins = new ArrayList<RemotePluginInfo>();
	private ClientSession session;
	
	public Multiplayer(String host, int port) {
		this.setLevel(null);
		this.session = new ClientSession(this, LoginInfo.getName(), LoginInfo.getKey(), host, port);
	}
	
	public void addServerPlugin(RemotePluginInfo plugin) {
		this.serverPlugins.add(plugin);
	}
	
	public List<RemotePluginInfo> getServerPlugins() {
		return new ArrayList<RemotePluginInfo>(this.serverPlugins);
	}
	
	public void update() {
		if(this.session != null) this.session.update();
		if(this.getLevel() != null) {
			super.update();
			byte yaw = (byte) ((int) (this.getPlayer().getPosition().getYaw() * 256 / 360f) & 255);
			byte pitch = (byte) ((int) (this.getPlayer().getPosition().getPitch()  * 256 / 360f) & 255);
			this.session.send(new PlayerTeleportMessage((byte) -1, this.getPlayer().getPosition().getX(), this.getPlayer().getPosition().getY() + Constants.FOOT_EYE_DISTANCE, this.getPlayer().getPosition().getZ(), yaw, pitch));
		}
	}
	
	public void renderUpdate(float delta) {
		if(this.getLevel() != null) {
			super.renderUpdate(delta);
		}
	}
	
	public void renderPerspective(float delta) {
		if(this.getLevel() != null) {
			super.renderPerspective(delta);
		}
	}
	
	public void renderOrtho(int width, int height) {
		if(this.getLevel() != null) {
			super.renderOrtho(width, height);
		}
	}
	
	public void onBreak(int x, int y, int z, BlockType old) {
		this.session.send(new PlayerSetBlockMessage((short) x, (short) y, (short) z, false, this.getPlayer().getQuickBar().getBlock(this.getPlayer().getQuickBar().getSelected())));
	}
	
	public void onPlace(int x, int y, int z, BlockType type) {
		this.session.send(new PlayerSetBlockMessage((short) x, (short) y, (short) z, true, type.getId()));
	}

	public ClientSession getSession() {
		return this.session;
	}
	
	@Override
	public boolean isInGame() {
		return this.getSession().isConnected() && super.isInGame();
	}

	@Override
	public void unload() {
		if(this.session != null && this.session.isConnected()) this.session.disconnect(null);
		LoginInfo.setKey(null);
		super.unload();
	}
	
}
