package ch.spacebase.openclassic.game.block.physics;

import java.util.Random;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.physics.BlockPhysics;

public class StationaryWaterPhysics implements BlockPhysics {

	private Random random = new Random();
	private int delay = 200;
	
	@Override
	public void update(Block block) {
		this.delay++;
		int rand = this.random.nextInt(1000);
		if(this.delay >= 200 && rand < 100) {
			this.delay = 0;
			OpenClassic.getGame().getAudioManager().playSound(block.getLevel(), "random.water", block.getPosition().getX() + 0.5f, block.getPosition().getY() + 0.5f, block.getPosition().getZ() + 0.5f, this.random.nextFloat() * 0.25f + 0.75f, this.random.nextFloat() + 0.5f);
		}
	}

	@Override
	public void onPlace(Block block) {
	}

	@Override
	public void onBreak(Block block) {
	}

	@Override
	public void onNeighborChange(Block block, Block neighbor) {
	}

}
