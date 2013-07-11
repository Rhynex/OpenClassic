package ch.spacebase.openclassic.client.block.physics;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.complex.vanilla.Chest;
import ch.spacebase.openclassic.api.block.physics.BlockPhysics;
import ch.spacebase.openclassic.api.inventory.ItemStack;
import ch.spacebase.openclassic.api.item.Item;
import ch.spacebase.openclassic.client.gui.inventory.ChestScreen;

public class ChestPhysics implements BlockPhysics {

	@Override
	public void onPlace(Block block, BlockFace against) {
		switch(against) {
		case EAST:
			block.setType(Blocks.get(block.getTypeId(), 0));
			break;
		case WEST:
			block.setType(Blocks.get(block.getTypeId(), 1));
			break;
		case NORTH:
			block.setType(Blocks.get(block.getTypeId(), 2));
			break;
		case SOUTH:
			block.setType(Blocks.get(block.getTypeId(), 3));
			break;
		case DOWN:
		case UP:
			BlockFace face = BlockFace.WEST;
			float yaw = OpenClassic.getClient().getPlayer().getPosition().getYaw();
			if((yaw >= 315 && yaw <= 360) || (yaw >= 0 && yaw <= 45) || (yaw <= 0 && yaw >= -45) || (yaw <= -315 && yaw >= -360)) face = BlockFace.WEST;
			if((yaw >= 45 && yaw <= 135) || (yaw >= -315 && yaw <= -225)) face = BlockFace.NORTH;
			if((yaw >= 135 && yaw <= 225) || (yaw >= -225 && yaw <= -135)) face = BlockFace.EAST;
			if((yaw >= 225 && yaw <= 315) || (yaw >= -135 && yaw <= -45)) face = BlockFace.SOUTH;
			this.onPlace(block, face);
			break;
		}
	}

	@Override
	public boolean onInteracted(ItemStack item, Block block) {
		Chest chest = (Chest) block.getPosition().getLevel().getComplexBlock(block.getPosition());
		OpenClassic.getClient().setCurrentScreen(new ChestScreen(chest));
		return true;
	}

	@Override
	public void update(Block block) {
	}

	@Override
	public boolean canPlace(Block block) {
		return true;
	}

	@Override
	public void onBreak(Block block) {
	}

	@Override
	public void onNeighborChange(Block block, Block neighbor) {
	}

	@Override
	public boolean canHarvest(Item item) {
		return true;
	}

}
