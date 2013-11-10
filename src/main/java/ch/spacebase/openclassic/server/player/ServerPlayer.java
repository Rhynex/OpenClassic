package ch.spacebase.openclassic.server.player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.event.player.PlayerTeleportEvent;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.network.msg.IdentificationMessage;
import ch.spacebase.openclassic.api.network.msg.LevelDataMessage;
import ch.spacebase.openclassic.api.network.msg.LevelFinalizeMessage;
import ch.spacebase.openclassic.api.network.msg.LevelInitializeMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerChatMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerDespawnMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerOpMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerSpawnMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.api.network.msg.custom.LevelColorMessage;
import ch.spacebase.openclassic.api.permissions.Group;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.server.ClassicServer;
import ch.spacebase.openclassic.server.network.ServerSession;

import com.zachsthings.onevent.EventManager;

public class ServerPlayer implements Player {

	private byte playerId;
	private Position pos;
	private String name;
	private String displayName;
	private ServerSession session;
	private byte placeMode = 0;
	// private int airTicks = 0;
	private ClientInfo client = new ClientInfo(this);
	private NBTData data;
	private List<String> hidden = new CopyOnWriteArrayList<String>();
	private boolean breakBedrock = false;

	private boolean sendingLevel = false;

	public ServerPlayer(String name, Position pos, ServerSession session) {
		this.name = name;
		this.displayName = name;
		this.pos = pos;
		this.session = session;
		this.data = new NBTData(this.name);
		this.data.load(OpenClassic.getServer().getDirectory().getPath() + "/players/" + this.name + ".nbt");

		session.setPlayer(this);

		this.playerId = (byte) (((ClassicServer) OpenClassic.getGame()).getSessionRegistry().size());
	}

	public Session getSession() {
		return this.session;
	}

	public byte getPlayerId() {
		return this.playerId;
	}

	public Position getPosition() {
		return this.pos;
	}

	public void setPosition(Position pos) {
		this.pos = pos;
	}

	public String getName() {
		return this.name;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String name) {
		this.displayName = name;
	}

	public byte getPlaceMode() {
		return this.placeMode;
	}

	public void setPlaceMode(int type) {
		this.placeMode = (byte) type;
	}

	public void moveTo(Position pos) {
		this.moveTo(pos.getLevel(), pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());
	}

	public void moveTo(float x, float y, float z) {
		this.moveTo(this.pos.getLevel(), x, y, z);
	}

	public void moveTo(float x, float y, float z, float yaw, float pitch) {
		this.moveTo(this.pos.getLevel(), x, y, z, yaw, pitch);
	}

	public void moveTo(Level level, float x, float y, float z) {
		this.moveTo(this.pos.getLevel(), x, y, z, this.pos.getYaw(), this.pos.getPitch());
	}

	public void moveTo(Level level, float x, float y, float z, float yaw, float pitch) {
		Position to = new Position(level, x, y, z, yaw, pitch);
		Level old = this.pos.getLevel();

		PlayerTeleportEvent event = EventManager.callEvent(new PlayerTeleportEvent(this, this.getPosition(), to));
		if(event.isCancelled()) return;

		this.pos = event.getTo();
		if(!old.getName().equals(this.pos.getLevel().getName())) {
			this.pos.getLevel().addPlayer(this);
			old.removePlayer(this.getName());
			old.sendToAllExcept(this, new PlayerDespawnMessage(this.getPlayerId()));
			this.session.send(new IdentificationMessage(Constants.PROTOCOL_VERSION, "Sending to " + this.pos.getLevel().getName() + "...", "", this.getGroup().hasPermission("openclassic.commands.solid") ? Constants.OP : Constants.NOT_OP));
			this.sendLevel(this.pos.getLevel());
		} else {
			this.getSession().send(new PlayerTeleportMessage((byte) -1, this.getPosition().getX(), this.getPosition().getY(), this.getPosition().getZ(), this.getPosition().getYaw(), this.getPosition().getPitch()));
			this.getPosition().getLevel().sendToAllExcept(this, new PlayerTeleportMessage(this.getPlayerId(), this.getPosition().getX(), this.getPosition().getY() + 0.59375f, this.getPosition().getZ(), this.getPosition().getYaw(), this.getPosition().getPitch()));
		}
	}

	public Group getGroup() {
		return OpenClassic.getServer().getPermissionManager().getPlayerGroup(this.getName());
	}

	public void setGroup(Group group) {
		OpenClassic.getServer().getPermissionManager().setPlayerGroup(this.getName(), group);
	}

	public SocketAddress getAddress() {
		return this.session.getAddress();
	}

	public String getIp() {
		return this.session.getAddress().toString().replace("/", "").split(":")[0];
	}

	@Override
	public boolean hasPermission(String permission) {
		return this.getGroup() != null && this.getGroup().hasPermission(permission);
	}

	@Override
	public String getCommandPrefix() {
		return "/";
	}

	public void disconnect(String reason) {
		this.session.disconnect(reason);
	}

	public void tick() {
		// Experimental
		/*
		 * if(!OpenClassic.getServer().getConfig().getBoolean("options.allow-flight"
		 * , false)) {
		 * if(this.pos.getLevel().getBlockTypeAt(this.pos.getBlockX(),
		 * this.pos.getBlockY() - 2, this.pos.getBlockZ()) == BlockType.AIR) {
		 * this.airTicks++; } else if(this.airTicks != 0) { this.airTicks = 0; }
		 * if(this.airTicks > 300) {
		 * this.session.disconnect("Flying is not allowed on this server."); } }
		 */
	}

	public void destroy() {
		this.getPosition().getLevel().removePlayer(this.getName());
		this.playerId = 0;
		this.pos = null;
		this.session = null;
	}
	
	@Override
	public void sendMessage(String message) {
		this.sendInternal(OpenClassic.getServer().getTranslator().translate(message, this.getLanguage()));
	}
	
	@Override
	public void sendMessage(String message, Object... args) {
		this.sendInternal(String.format(OpenClassic.getServer().getTranslator().translate(message, this.getLanguage()), args));
	}
	
	private void sendInternal(String message) {
		if(!this.hasCustomClient()) {
			message.replace(Color.ORANGE.toString(), Color.RED.toString());
			message.replace(Color.BROWN.toString(), Color.RED.toString());
		}
		
		for(String msg : message.split("\n")) {
			this.getSession().send(new PlayerChatMessage(this.getPlayerId(), msg));
		}
	}

	public void sendLevel(final Level level) {
		final Player player = this;
		OpenClassic.getGame().getScheduler().scheduleAsyncTask(OpenClassic.getGame(), new Runnable() {
			@Override
			public void run() {
				while(sendingLevel) {
					try {
						Thread.sleep(5000);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}

				sendingLevel = true;

				try {
					session.send(new LevelInitializeMessage());

					ByteArrayOutputStream out = new ByteArrayOutputStream();
					GZIPOutputStream gzip = new GZIPOutputStream(out);
					DataOutputStream dataOut = new DataOutputStream(gzip);

					byte[] b = level.getBlocks();
					if(!hasCustomClient()) {
						for(int index = 0; index < b.length; index++) {
							if(Blocks.fromId(b[index]).getId() > 49) {
								b[index] = VanillaBlock.STONE.getId();
							}
						}
					}

					dataOut.writeInt(b.length);
					dataOut.write(b);

					IOUtils.closeQuietly(dataOut);
					IOUtils.closeQuietly(gzip);
					IOUtils.closeQuietly(out);
					byte[] data = out.toByteArray();
					double numChunks = data.length / 1024;
					double sent = 0;

					for(int chunkStart = 0; chunkStart < data.length; chunkStart += 1024) {
						byte[] chunkData = new byte[1024];

						short length = 1024;
						if(data.length - chunkStart < length) length = (short) (data.length - chunkStart);

						System.arraycopy(data, chunkStart, chunkData, 0, length);

						session.send(new LevelDataMessage(length, chunkData, (byte) ((sent / numChunks) * 255)));
						sent++;
					}

					session.send(new LevelFinalizeMessage(level.getWidth(), level.getHeight(), level.getDepth()));
					moveTo(level.getSpawn());

					level.sendToAllExcept(player, new PlayerSpawnMessage(player.getPlayerId(), player.getName(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), (byte) player.getPosition().getYaw(), (byte) player.getPosition().getPitch()));
					for(Player p : level.getPlayers()) {
						if(p.getPlayerId() == getPlayerId()) continue;

						session.send(new PlayerSpawnMessage(p.getPlayerId(), p.getName(), p.getPosition().getX(), p.getPosition().getY(), p.getPosition().getZ(), (byte) p.getPosition().getYaw(), (byte) p.getPosition().getPitch()));
					}

					if(hasCustomClient()) {
						session.send(new LevelColorMessage("sky", level.getSkyColor()));
						session.send(new LevelColorMessage("fog", level.getFogColor()));
						session.send(new LevelColorMessage("cloud", level.getCloudColor()));
					}
				} catch(Exception e) {
					session.disconnect("Failed to send level!");
					OpenClassic.getLogger().severe("Failed to send level " + level.getName() + " to player " + getName() + "!");
					e.printStackTrace();
				}

				sendingLevel = false;
			}
		});
	}

	public ClientInfo getClientInfo() {
		return this.client;
	}

	@Override
	public boolean hasCustomClient() {
		return this.client.isCustom();
	}

	@Override
	public String getClientVersion() {
		return this.client.getVersion();
	}

	@Override
	public NBTData getData() {
		return this.data;
	}

	@Override
	public List<RemotePluginInfo> getPlugins() {
		return this.client.getPlugins();
	}

	@Override
	public void chat(String message) {
		this.session.messageReceived(new PlayerChatMessage((byte) -1, message));
	}

	@Override
	public void hidePlayer(Player player) {
		this.getSession().send(new PlayerDespawnMessage(player.getPlayerId()));
		this.hidden.add(player.getName());
	}

	@Override
	public void showPlayer(Player player) {
		this.hidden.remove(player.getName());
		this.getSession().send(new PlayerSpawnMessage(player.getPlayerId(), player.getName(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), (byte) player.getPosition().getYaw(), (byte) player.getPosition().getPitch()));
	}

	@Override
	public boolean canSee(Player player) {
		return this.hidden.contains(player.getName());
	}

	@Override
	public String getLanguage() {
		return this.client.getLanguage().equals("") ? OpenClassic.getGame().getLanguage() : this.client.getLanguage();
	}

	@Override
	public int getInvulnerableTime() {
		return 0;
	}

	@Override
	public boolean isUnderwater() {
		return false;
	}

	@Override
	public int getHealth() {
		return 0;
	}

	@Override
	public void setHealth(int health) {
	}

	@Override
	public boolean isDead() {
		return false;
	}

	@Override
	public int getPreviousHealth() {
		return 0;
	}

	@Override
	public int getAir() {
		return 0;
	}

	@Override
	public void setAir(int air) {
	}

	@Override
	public int getScore() {
		return 0;
	}

	@Override
	public void setScore(int score) {
	}

	@Override
	public int getArrows() {
		return 0;
	}

	@Override
	public void setArrows(int arrows) {
	}

	@Override
	public int getSelectedSlot() {
		return 0;
	}

	@Override
	public int[] getInventoryContents() {
		return new int[0];
	}

	@Override
	public int[] getInventoryAmounts() {
		return new int[0];
	}

	@Override
	public int[] getInventoryPopTimes() {
		return new int[0];
	}
	
	@Override
	public void replaceSelected(BlockType block) {
	}

	@Override
	public void respawn() {
		this.setPosition(this.getPosition().getLevel().getSpawn());
	}

	@Override
	public boolean canBreakBedrock() {
		return this.breakBedrock;
	}

	@Override
	public void setCanBreakBedrock(boolean canBreak) {
		this.breakBedrock = canBreak;
		this.getSession().send(new PlayerOpMessage(canBreak ? Constants.OP : Constants.NOT_OP));
	}

}
