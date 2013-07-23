package ch.spacebase.openclassic.server.network;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.event.player.PlayerKickEvent;
import ch.spacebase.openclassic.api.event.player.PlayerQuitEvent;
import ch.spacebase.openclassic.api.network.msg.BlockChangeMessage;
import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.api.network.msg.PlayerDespawnMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerDisconnectMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerSpawnMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.server.player.ServerPlayer;

import com.zachsthings.onevent.EventManager;

public class ServerSession extends ClassicSession {

	private boolean pendingRemoval = false;
	public boolean disconnectMsgSent = false;

	public ServerSession(Channel channel) {
		super(new ServerHandlerLookup());
		this.channel = channel;
	}

	public boolean tick() {
		if(this.pendingRemoval) {
			return false;
		}

		return super.tick();
	}

	public void send(Message message) {
		if(message instanceof BlockChangeMessage && !this.getPlayer().hasCustomClient()) {
			BlockType block = Blocks.fromId(((BlockChangeMessage) message).getBlock());
			if(block.getId() > 49) {
				message = new BlockChangeMessage(((BlockChangeMessage) message).getX(), ((BlockChangeMessage) message).getY(), ((BlockChangeMessage) message).getZ(), VanillaBlock.STONE.getId());
			}
		}

		if(!this.canSendPlayerMessage(message, this.getPlayer())) return;
		super.send(message);
	}

	@Override
	public boolean sendCustomMessages() {
		return ((ServerPlayer) this.getPlayer()).hasCustomClient();
	}

	private boolean canSendPlayerMessage(Message message, Player player) {
		if(message instanceof PlayerDespawnMessage && ((PlayerDespawnMessage) message).getPlayerId() != -1 && !player.canSee(OpenClassic.getServer().getPlayer(((PlayerDespawnMessage) message).getPlayerId()))) {
			return false;
		} else if(message instanceof PlayerTeleportMessage && ((PlayerTeleportMessage) message).getPlayerId() != -1 && !player.canSee(OpenClassic.getServer().getPlayer(((PlayerTeleportMessage) message).getPlayerId()))) {
			return false;
		} else if(message instanceof PlayerSpawnMessage && ((PlayerSpawnMessage) message).getPlayerId() != -1 && !player.canSee(OpenClassic.getServer().getPlayer(((PlayerSpawnMessage) message).getPlayerId()))) {
			return false;
		}

		return true;
	}

	public void disconnect(String reason) {
		if(this.getPlayer() != null && this.getState() == State.GAME) {
			PlayerKickEvent event = EventManager.callEvent(new PlayerKickEvent(this.getPlayer(), reason, this.getPlayer().getDisplayName() + Color.AQUA + " has been kicked. (" + reason + Color.AQUA + ")"));
			if(event.isCancelled()) {
				return;
			}

			EventManager.callEvent(new PlayerQuitEvent(this.getPlayer(), this.getPlayer().getDisplayName() + Color.AQUA + " has been kicked. (" + reason + Color.AQUA + ")"));
			OpenClassic.getServer().broadcastMessage(event.getMessage());
		} else {
			OpenClassic.getLogger().info(this.getAddress() + " disconnected by server: \"" + reason + "\"");
		}

		this.channel.write(new PlayerDisconnectMessage(reason)).addListener(ChannelFutureListener.CLOSE);
		this.disconnectMsgSent = true;
	}

	public void flagForRemoval() {
		this.pendingRemoval = true;
	}

	public void dispose() {
		if(this.getPlayer() != null) {
			((ServerPlayer) this.getPlayer()).destroy();
		}

		super.dispose();
	}

}
