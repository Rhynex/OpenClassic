package ch.spacebase.openclassic.client.block.physics;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.physics.BlockPhysics;
import ch.spacebase.openclassic.client.level.ClientLevel;

import com.mojang.minecraft.entity.object.PrimedTnt;

public class TNTPhysics implements BlockPhysics {

	@Override
	public void update(Block block) {
	}

	@Override
	public void onPlace(Block block) {
	}

	@Override
	public void onBreak(Block block) {
		if(OpenClassic.getClient().isInSurvival()) {
			((ClientLevel) block.getLevel()).addEntity(new PrimedTnt((ClientLevel) block.getLevel(), block.getPosition().getBlockX() + 0.5F, block.getPosition().getBlockY() + 0.5F, block.getPosition().getBlockZ() + 0.5F));
		}
	}

	@Override
	public void onNeighborChange(Block block, Block neighbor) {
	}

}
