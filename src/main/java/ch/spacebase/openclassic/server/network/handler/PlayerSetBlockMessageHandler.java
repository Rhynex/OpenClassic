package ch.spacebase.openclassic.server.network.handler;

import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.event.block.BlockBreakEvent;
import ch.spacebase.openclassic.api.event.block.BlockPlaceEvent;
import ch.spacebase.openclassic.api.network.msg.BlockChangeMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerSetBlockMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session.State;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;
import ch.spacebase.openclassic.server.level.ServerLevel;

import com.zachsthings.onevent.EventManager;

public class PlayerSetBlockMessageHandler extends MessageHandler<PlayerSetBlockMessage> {

	@Override
	public void handle(ClassicSession session, Player player, PlayerSetBlockMessage message) {
		if(session.getState() != State.GAME) return;

		BlockType b = Blocks.fromId(message.getBlock());
		if(b == null || !b.isSelectable()) {
			player.sendMessage("Denied block type hack.");
			return;
		}

		// TODO: Reach hack checks and check if player is in position

		BlockType old = player.getPosition().getLevel().getBlockTypeAt(message.getX(), message.getY(), message.getZ());
		if(!message.isPlacing() && old == VanillaBlock.BEDROCK && !player.hasPermission("openclassic.commands.solid")) {
			player.sendMessage("Denied block break hack.");
			return;
		}

		Block block = player.getPosition().getLevel().getBlockAt(message.getX(), message.getY(), message.getZ());
		byte type = (message.isPlacing()) ? message.getBlock() : 0;
		if(message.isPlacing() && player.getPlaceMode() != 0 && type != 0) type = player.getPlaceMode();

		if(!message.isPlacing()) {
			if(EventManager.callEvent(new BlockBreakEvent(block, player, Blocks.fromId(message.getBlock()))).isCancelled()) {
				session.send(new BlockChangeMessage((short) block.getPosition().getBlockX(), (short) block.getPosition().getBlockY(), (short) block.getPosition().getBlockZ(), block.getTypeId()));
				return;
			}
		}

		player.getPosition().getLevel().setBlockIdAt(message.getX(), message.getY(), message.getZ(), type);
		if(message.isPlacing()) {
			if(EventManager.callEvent(new BlockPlaceEvent(block, player, Blocks.fromId(message.getBlock()))).isCancelled()) {
				if(player.getPosition().getLevel().getBlockIdAt(message.getX(), message.getY(), message.getZ()) == type) {
					player.getPosition().getLevel().setBlockAt(message.getX(), message.getY(), message.getZ(), old);
				}

				return;
			}
		}

		((ServerLevel) player.getPosition().getLevel()).updatePhysics(message.getX(), message.getY(), message.getZ());
		if(block != null && block.getType() != null && block.getType().getPhysics() != null) {
			if(message.isPlacing()) {
				block.getType().getPhysics().onPlace(block);
			} else {
				block.getType().getPhysics().onBreak(block);
			}
		}
	}

}
