package ch.spacebase.openclassic.client.player;

import java.net.SocketAddress;
import java.util.List;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.event.player.PlayerChatEvent;
import ch.spacebase.openclassic.api.event.player.PlayerTeleportEvent;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.permissions.Group;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.gui.hud.ClientHUDScreen;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.msg.PlayerChatMessage;
import ch.spacebase.openclassic.game.network.msg.custom.CustomMessage;

import com.zachsthings.onevent.EventManager;

public class ClientPlayer implements Player {

	private String name;
	private com.mojang.minecraft.entity.player.Player handle;
	private byte placeMode = 0;
	private DummySession dummySession = new DummySession(this);
	private NBTData data = new NBTData("Player");
	private boolean breakBedrock = false;

	public ClientPlayer() {
		this.data.load(OpenClassic.getClient().getDirectory().getPath() + "/player.nbt");
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void sendMessage(String message) {
		this.sendInternal(OpenClassic.getClient().getTranslator().translate(message, this.getLanguage()));
	}
	
	@Override
	public void sendMessage(String message, Object... args) {
		this.sendInternal(String.format(OpenClassic.getClient().getTranslator().translate(message, this.getLanguage()), args));
	}
	
	private void sendInternal(String message) {
		for(String msg : message.split("\n")) {
			OpenClassic.getClient().getHUD().addChat(msg);
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

	public ClassicSession getSession() {
		return GeneralUtils.getMinecraft().isInMultiplayer() ? GeneralUtils.getMinecraft().session : this.dummySession;
	}

	@Override
	public Position getPosition() {
		if(this.handle == null) {
			return new Position(null, 0, 0, 0);
		}
		
		return new Position(this.handle.level.openclassic, this.handle.x, this.handle.y, this.handle.z, (byte) this.handle.yaw, (byte) this.handle.pitch);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDisplayName() {
		return this.getName();
	}

	@Override
	public void setDisplayName(String name) {
	}

	@Override
	public byte getPlaceMode() {
		return this.placeMode;
	}

	@Override
	public void setPlaceMode(int type) {
		this.placeMode = (byte) type;
	}

	@Override
	public void moveTo(Position pos) {
		this.moveTo(pos.getLevel(), pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());
	}

	@Override
	public void moveTo(float x, float y, float z) {
		this.moveTo(this.handle.level.openclassic, x, y, z, (byte) 0, (byte) 0);
	}

	@Override
	public void moveTo(float x, float y, float z, float yaw, float pitch) {
		this.moveTo(this.handle.level.openclassic, x, y, z, yaw, pitch);
	}

	@Override
	public void moveTo(Level level, float x, float y, float z) {
		this.moveTo(level, x, y, z, (byte) 0, (byte) 0);
	}

	@Override
	public void moveTo(Level level, float x, float y, float z, float yaw, float pitch) {
		if(this.handle == null) {
			return;
		}
		
		PlayerTeleportEvent event = EventManager.callEvent(new PlayerTeleportEvent(this, this.getPosition(), new Position(level, x, y, z, yaw, pitch)));
		if(event.isCancelled()) {
			return;
		}

		if(event.getTo().getLevel() != null && this.handle.level != null && !this.handle.level.name.equals(event.getTo().getLevel().getName())) {
			this.handle.setLevel(((ClientLevel) event.getTo().getLevel()).getHandle());
			GeneralUtils.getMinecraft().setLevel(((ClientLevel) event.getTo().getLevel()).getHandle());
		}

		this.handle.moveTo(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ(), event.getTo().getYaw(), event.getTo().getPitch());
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
		return this.getAddress().toString().replace("/", "").split(":")[0];
	}

	@Override
	public SocketAddress getAddress() {
		return this.getSession().getAddress();
	}

	@Override
	public void disconnect(String reason) {
		this.getSession().disconnect(reason);
	}

	public com.mojang.minecraft.entity.player.Player getHandle() {
		return this.handle;
	}
	
	public void setHandle(com.mojang.minecraft.entity.player.Player handle) {
		this.handle = handle;
		this.placeMode = 0;
		this.breakBedrock = false;
	}

	@Override
	public boolean hasCustomClient() {
		return true;
	}

	@Override
	public String getClientVersion() {
		return Constants.VERSION;
	}

	@Override
	public NBTData getData() {
		return this.data;
	}

	@Override
	public List<RemotePluginInfo> getPlugins() {
		return GeneralUtils.getMinecraft().serverPlugins;
	}

	@Override
	public void chat(String message) {
		if(this.getSession() == this.dummySession) {
			((ClientHUDScreen) OpenClassic.getClient().getHUD()).addChat(message);
			return;
		}
		
		PlayerChatEvent event = EventManager.callEvent(new PlayerChatEvent(OpenClassic.getClient().getPlayer(), message));
		if(event.isCancelled()) {
			return;
		}
		
		this.getSession().send(new PlayerChatMessage((byte) -1, event.getMessage()));
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

	@Override
	public String getLanguage() {
		return OpenClassic.getGame().getLanguage();
	}

	@Override
	public int getInvulnerableTime() {
		if(this.handle == null) {
			return 0;
		}
		
		return this.handle.invulnerableTime;
	}

	@Override
	public boolean isUnderwater() {
		if(this.handle == null) {
			return false;
		}
		
		return this.handle.isUnderWater();
	}

	@Override
	public int getHealth() {
		if(this.handle == null) {
			return 0;
		}
		
		return this.handle.health;
	}

	@Override
	public void setHealth(int health) {
		if(this.handle == null) {
			return;
		}
		
		if(health < 0) {
			health = 0;
		}
		
		if(health > Constants.MAX_HEALTH) {
			health = Constants.MAX_HEALTH;
		}
		
		this.handle.health = health;
	}

	@Override
	public boolean isDead() {
		if(this.handle == null) {
			return false;
		}
		
		return this.handle.dead;
	}

	@Override
	public int getPreviousHealth() {
		if(this.handle == null) {
			return 0;
		}
		
		return this.handle.lastHealth;
	}

	@Override
	public int getAir() {
		if(this.handle == null) {
			return 0;
		}
		
		return this.handle.airSupply;
	}

	@Override
	public void setAir(int air) {
		if(this.handle == null) {
			return;
		}
		
		if(air < 0) {
			air = 0;
		}
		
		if(air > Constants.MAX_AIR) {
			air = Constants.MAX_AIR;
		}
		
		this.handle.airSupply = air;
	}

	@Override
	public int getScore() {
		if(this.handle == null) {
			return 0;
		}
		
		return this.handle.getScore();
	}

	@Override
	public void setScore(int score) {
		if(this.handle == null) {
			return;
		}
		
		this.handle.score = score;
	}

	@Override
	public int getArrows() {
		if(this.handle == null) {
			return 0;
		}
		
		return this.handle.arrows;
	}

	@Override
	public void setArrows(int arrows) {
		if(this.handle == null) {
			return;
		}
		
		if(arrows < 0) {
			arrows = 0;
		}
		
		if(arrows > Constants.MAX_ARROWS) {
			arrows = Constants.MAX_ARROWS;
		}
		
		this.handle.airSupply = arrows;
	}

	@Override
	public int getSelectedSlot() {
		if(this.handle == null) {
			return 0;
		}
		
		return this.handle.inventory.selected;
	}

	@Override
	public int[] getInventoryContents() {
		if(this.handle == null) {
			return new int[0];
		}
		
		return this.handle.inventory.slots;
	}

	@Override
	public int[] getInventoryAmounts() {
		if(this.handle == null) {
			return new int[0];
		}
		
		return this.handle.inventory.count;
	}
	
	@Override
	public void replaceSelected(BlockType block) {
		if(this.handle == null) {
			return;
		}
		
		this.handle.inventory.replaceSlot(block);
	}

	@Override
	public void respawn() {
		if(this.handle == null) {
			return;
		}
		
		this.handle.resetPos();
	}

	@Override
	public boolean canBreakBedrock() {
		return this.breakBedrock;
	}

	@Override
	public void setCanBreakBedrock(boolean canBreak) {
		this.breakBedrock = canBreak;
	}

	@Override
	public void sendCustomMessage(String id, byte[] data) {
		this.getSession().send(new CustomMessage(id, data));
	}

}
