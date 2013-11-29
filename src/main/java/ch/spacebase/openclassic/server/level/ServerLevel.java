package ch.spacebase.openclassic.server.level;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.level.ClassicLevel;
import ch.spacebase.openclassic.game.network.msg.BlockChangeMessage;
import ch.spacebase.openclassic.game.network.msg.Message;
import ch.spacebase.openclassic.game.network.msg.PlayerDespawnMessage;
import ch.spacebase.openclassic.game.network.msg.custom.LevelColorMessage;
import ch.spacebase.openclassic.server.player.ServerPlayer;

public class ServerLevel extends ClassicLevel {

	private List<Player> players = new ArrayList<Player>();

	public ServerLevel() {
	}

	public ServerLevel(LevelInfo info) {
		super(info);
	}

	@Override
	public List<Player> getPlayers() {
		return new ArrayList<Player>(this.players);
	}
	
	public void addPlayer(Player player) {
		this.players.add(player);
	}

	public void removePlayer(String name) {
		for(Player player : this.getPlayers()) {
			if(player.getName().equalsIgnoreCase(name)) {
				this.players.remove(player);
				this.sendToAllExcept(player, new PlayerDespawnMessage(((ServerPlayer) player).getPlayerId()));
			}
		}
	}

	public void removePlayer(byte id) {
		for(Player player : this.getPlayers()) {
			if(((ServerPlayer) player).getPlayerId() == id) {
				this.players.remove(player);
				this.sendToAllExcept(player, new PlayerDespawnMessage(((ServerPlayer) player).getPlayerId()));
			}
		}
	}
	
	@Override
	public boolean setBlockIdAt(int x, int y, int z, byte type, boolean physics) {
		if(super.setBlockIdAt(x, y, z, type, physics)) {
			this.sendToAll(new BlockChangeMessage((short) x, (short) y, (short) z, type));
			return true;
		}
		
		return false;
	}
	
	@Override
	public void setSkyColor(int color) {
		super.setSkyColor(color);
		this.sendToAll(new LevelColorMessage("sky", color));
	}

	@Override
	public void setFogColor(int color) {
		super.setFogColor(color);
		this.sendToAll(new LevelColorMessage("fog", color));
	}

	@Override
	public void setCloudColor(int color) {
		super.setCloudColor(color);
		this.sendToAll(new LevelColorMessage("cloud", color));
	}
	
	public void sendToAll(Message message) {
		for(Player player : this.getPlayers()) {
			((ServerPlayer) player).getSession().send(message);
		}
	}

	public void sendToAllExcept(Player skip, Message message) {
		for(Player player : this.getPlayers()) {
			if(((ServerPlayer) player).getPlayerId() == ((ServerPlayer) player).getPlayerId()) {
				continue;
			}

			((ServerPlayer) player).getSession().send(message);
		}
	}

}
