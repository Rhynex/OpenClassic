package ch.spacebase.openclassic.server.player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPOutputStream;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.custom.CustomBlock;
import ch.spacebase.openclassic.api.component.BasicComponentHolder;
import ch.spacebase.openclassic.api.component.type.NBTComponent;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.event.player.PlayerTeleportEvent;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.network.msg.IdentificationMessage;
import ch.spacebase.openclassic.api.network.msg.LevelDataMessage;
import ch.spacebase.openclassic.api.network.msg.LevelFinalizeMessage;
import ch.spacebase.openclassic.api.network.msg.LevelInitializeMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerChatMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerDespawnMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerSpawnMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.api.network.msg.custom.LevelColorMessage;
import ch.spacebase.openclassic.api.permissions.Group;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.game.component.player.ClientInfoComponent;
import ch.spacebase.openclassic.game.component.player.PlaceModeComponent;
import ch.spacebase.openclassic.server.ClassicServer;
import ch.spacebase.openclassic.server.network.ServerSession;

public class ServerPlayer extends BasicComponentHolder implements Player {
	
	private byte playerId;
	private Position pos;
	private String name;
	private String displayName;
	private ServerSession session;
	//private int airTicks = 0;
	private List<String> hidden = new CopyOnWriteArrayList<String>();
	
	public boolean teleported = false;
	private boolean sendingLevel = false;
	
	public ServerPlayer(String name, Position pos, ServerSession session) {
		this.name = name;
		this.displayName = name;
		this.pos = pos;
		this.session = session;
		
		session.setPlayer(this);
		this.playerId = (byte) (((ClassicServer) OpenClassic.getGame()).getSessionRegistry().size());
		
		this.add(PlaceModeComponent.class);
		this.add(ClientInfoComponent.class);
		this.add(NBTComponent.class).load(this.name, OpenClassic.getServer().getDirectory().getPath() + "/players/" + this.name + ".nbt");
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
		return this.get(PlaceModeComponent.class).getPlaceMode();
	}
	
	public void setPlaceMode(int type) {
		this.get(PlaceModeComponent.class).setPlaceMode((byte) type);
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
		
		PlayerTeleportEvent event = OpenClassic.getGame().getEventManager().dispatch(new PlayerTeleportEvent(this, this.getPosition(), to));
		if(event.isCancelled()) return;
		
		this.pos = event.getTo();
		this.teleported = true;
		if(!old.getName().equals(this.pos.getLevel().getName())) {
			this.pos.getLevel().addPlayer(this);
			old.removePlayer(this.getName());
			old.sendToAllExcept(this, new PlayerDespawnMessage(this.getPlayerId()));
			this.session.send(new IdentificationMessage(Constants.PROTOCOL_VERSION, "Sending to " + this.pos.getLevel().getName() + "...", "", this.getGroup().hasPermission("openclassic.commands.solid") ? Constants.OP : Constants.NOT_OP));
			this.sendLevel(this.pos.getLevel());
		} else {
			this.getSession().send(new PlayerTeleportMessage((byte) -1, this.getPosition().getX(), this.getPosition().getY(), this.getPosition().getZ(), (byte) this.getPosition().getYaw(), (byte) this.getPosition().getPitch()));
			this.getPosition().getLevel().sendToAllExcept(this, new PlayerTeleportMessage(this.getPlayerId(), this.getPosition().getX(), this.getPosition().getY() + 0.59375, this.getPosition().getZ(), (byte) this.getPosition().getYaw(), (byte) this.getPosition().getPitch()));
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
		/* if(!OpenClassic.getServer().getConfig().getBoolean("options.allow-flight", false)) {
			if(this.pos.getLevel().getBlockTypeAt(this.pos.getBlockX(), this.pos.getBlockY() - 2, this.pos.getBlockZ()) == BlockType.AIR) {
				this.airTicks++;	
			} else if(this.airTicks != 0) {
				this.airTicks = 0;
			}
			
			if(this.airTicks > 300) {
				this.session.disconnect("Flying is not allowed on this server.");
			}
		} */
	}
	
	public void destroy() {
		this.getPosition().getLevel().removePlayer(this.getName());
		this.playerId = 0;
		this.pos = null;
		this.session = null;
	}
	
	@Override
	public void sendMessage(String message) {		
		this.getSession().send(new PlayerChatMessage(this.getPlayerId(), message));
	}
	
	public void sendLevel(final Level level) {
		final Player player = this;
		OpenClassic.getGame().getScheduler().scheduleAsyncTask(OpenClassic.getGame(), new Runnable() {
			@Override
			public void run() {
				while(sendingLevel) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
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
							if(Blocks.fromId(b[index]) instanceof CustomBlock) {
								b[index] = ((CustomBlock) Blocks.fromId(b[index])).getFallback().getId();
							}
						}
					}
					
					dataOut.writeInt(b.length);
					dataOut.write(b);
					
					dataOut.close();
					gzip.close();

					byte[] data = out.toByteArray();
					
					out.close();

					double numChunks = data.length / 1024;
					double sent = 0;
					
					for (int chunkStart = 0; chunkStart < data.length; chunkStart += 1024) {
						byte[] chunkData = new byte[1024];

						short length = 1024;
						if (data.length - chunkStart < length)
							length = (short) (data.length - chunkStart);

						System.arraycopy(data, chunkStart, chunkData, 0, length);

						session.send(new LevelDataMessage(length, chunkData, (byte) ((sent / numChunks) * 255)));
						sent++;
					}
					
					session.send(new LevelFinalizeMessage(level.getWidth(), level.getHeight(), level.getDepth()));
					moveTo(level.getSpawn());
					
					level.sendToAllExcept(player, new PlayerSpawnMessage(player.getPlayerId(), player.getName(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), (byte) player.getPosition().getYaw(), (byte) player.getPosition().getPitch()));
					for (Player p : level.getPlayers()) {
						if(p.getPlayerId() == getPlayerId()) continue;
						
						session.send(new PlayerSpawnMessage(p.getPlayerId(), p.getName(), p.getPosition().getX(), p.getPosition().getY(), p.getPosition().getZ(), (byte) p.getPosition().getYaw(), (byte) p.getPosition().getPitch()));
					}
					
					if(hasCustomClient()) {
						session.send(new LevelColorMessage("sky", level.getSkyColor()));
						session.send(new LevelColorMessage("fog", level.getFogColor()));
						session.send(new LevelColorMessage("cloud", level.getCloudColor()));
					}
				} catch (Exception e) {
					session.disconnect("Failed to send level!");
					OpenClassic.getLogger().severe("Failed to send level " + level.getName() + " to player " + getName() + "!");
					e.printStackTrace();
				}
				
				sendingLevel = false;
			}
		});
	}

	@Override
	public boolean hasCustomClient() {
		return this.get(ClientInfoComponent.class).isCustom();
	}

	@Override
	public String getClientVersion() {
		return this.get(ClientInfoComponent.class).getVersion();
	}
	
	@Override
	public NBTData getData() {
		return this.get(NBTComponent.class).getData();
	}

	@Override
	public List<RemotePluginInfo> getPlugins() {
		return this.get(ClientInfoComponent.class).getPlugins();
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
		ClientInfoComponent info = this.get(ClientInfoComponent.class);
		return info.getLanguage().equals("") ? OpenClassic.getGame().getLanguage() : info.getLanguage();
	}
	
}
