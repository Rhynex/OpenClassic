package ch.spacebase.openclassic.server.level;

import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.entity.BlockEntity;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.api.network.msg.custom.LevelColorMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.game.level.ClassicLevel;
import ch.spacebase.openclassic.server.player.ServerPlayer;

public class ServerLevel extends ClassicLevel implements Level {
	
	public ServerLevel(String name) {
		super(name);
		this.executor.shutdown();
	}
	
	public ServerLevel(LevelInfo info) {
		super(info);
		this.executor.shutdown();
	}
	
	public void update() {
		for(Player player : this.getPlayers()) {
			((ServerPlayer) player).tick();
		}
		
		for(BlockEntity entity : this.getBlockEntities()) {
			if(entity.getController() != null) entity.getController().tick();
		}
		
		this.physics();
	}
	
	public void render(float delta) {
	}
	
	@Override
	public boolean isLit(int x, int y, int z) {
		boolean lit = false;
		
		for(int curr = y; curr <= Constants.COLUMN_HEIGHT; curr++) {
			if(!this.canLightPass(this.getBlockAt(x, curr, z))) lit = false;
		}
		
		return lit;
	}
	
	public boolean canLightPass(Block block) {
		return block == null || block.getType() == VanillaBlock.AIR || block.getType() == VanillaBlock.DANDELION || block.getType() == VanillaBlock.ROSE || block.getType() == VanillaBlock.RED_MUSHROOM || block.getType() == VanillaBlock.BROWN_MUSHROOM || block.getType() == VanillaBlock.GLASS || block.getType() == VanillaBlock.LEAVES;
	}
	
	public void sendToAll(Message message) {
		for(Player player : this.getPlayers()) {
			player.getSession().send(message);
		}
	}
	
	public void sendToAllExcept(Player skip, Message message) {
		for(Player player : this.getPlayers()) {
			if(player.getPlayerId() == skip.getPlayerId()) continue;
			
			player.getSession().send(message);
		}
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
	
}
