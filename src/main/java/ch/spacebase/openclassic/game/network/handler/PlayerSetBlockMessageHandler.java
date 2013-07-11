package ch.spacebase.openclassic.game.network.handler;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.event.block.BlockBreakEvent;
import ch.spacebase.openclassic.api.event.block.BlockPlaceEvent;
import ch.spacebase.openclassic.api.network.msg.BlockChangeMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerSetBlockMessage;
import ch.spacebase.openclassic.api.player.Session.State;
import ch.spacebase.openclassic.server.level.ServerLevel;
import ch.spacebase.openclassic.server.network.ServerSession;
import ch.spacebase.openclassic.server.player.ServerPlayer;

public class PlayerSetBlockMessageHandler extends MessageHandler<PlayerSetBlockMessage> {

	// TODO: adjust for data
	@Override
	public void handleServer(ServerSession session, ServerPlayer player, PlayerSetBlockMessage message) {
		if(session == null || player == null) return;
		if(session.getState() != State.GAME) return;
		
		if(message.getBlock() == VanillaBlock.AIR.getId() || message.getBlock() == VanillaBlock.WATER.getId() || message.getBlock() == VanillaBlock.STATIONARY_WATER.getId() || message.getBlock() == VanillaBlock.LAVA.getId() || message.getBlock() == VanillaBlock.STATIONARY_LAVA.getId() || message.getBlock() == VanillaBlock.BEDROCK.getId()) {
			session.disconnect("Block type hack detected.");
			return;
		}
		
		// TODO: Reach hack checks and check if player is in position
		
		BlockType old = player.getPosition().getLevel().getBlockTypeAt(message.getX(), message.getY(), message.getZ());
		if(!message.isPlacing() && old == VanillaBlock.BEDROCK && !player.hasPermission("openclassic.commands.solid")) {
			session.disconnect("Block break hack detected.");
			return;
		}
			
		Block block = player.getPosition().getLevel().getBlockAt(message.getX(), message.getY(), message.getZ());
		byte type = (message.isPlacing()) ? message.getBlock() : 0;
		BlockType b = Blocks.get(type);
		if(message.isPlacing() && !(b.getPhysics() != null && !b.getPhysics().canPlace(block))) {
			session.send(new BlockChangeMessage((short) block.getPosition().getBlockX(), (short) block.getPosition().getBlockY(), (short) block.getPosition().getBlockZ(), block.getTypeId()));
			return;
		}
		
		if(!message.isPlacing()) {
			if(OpenClassic.getGame().getEventManager().dispatch(new BlockBreakEvent(block, player, Blocks.get(message.getBlock()))).isCancelled()) {
				session.send(new BlockChangeMessage((short) block.getPosition().getBlockX(), (short) block.getPosition().getBlockY(), (short) block.getPosition().getBlockZ(), block.getTypeId()));
				return;
			}
		}
		
		// TODO: adjust for data player.getPosition().getLevel().setBlockIdAt(message.getX(), message.getY(), message.getZ(), type);
		if(message.isPlacing()) {
			if(OpenClassic.getGame().getEventManager().dispatch(new BlockPlaceEvent(block, player, Blocks.get(message.getBlock()))).isCancelled()) {
				if(player.getPosition().getLevel().getBlockIdAt(message.getX(), message.getY(), message.getZ()) == type) {
					player.getPosition().getLevel().setBlockAt(message.getX(), message.getY(), message.getZ(), old);
				}
				
				return;
			}
		}
		
		((ServerLevel) player.getPosition().getLevel()).updatePhysics(message.getX(), message.getY(), message.getZ());
		
		if(block != null && block.getType() != null && block.getType().getPhysics() != null) {
			if(message.isPlacing()) {
				block.getType().getPhysics().onPlace(block, BlockFace.DOWN);
			} else {
				block.getType().getPhysics().onBreak(block);
			}
		}
	}

}
