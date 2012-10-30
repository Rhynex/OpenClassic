package ch.spacebase.openclassic.game.component.player;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.component.Component;

public class PlaceModeComponent extends Component {

	private BlockType placemode = null;
	
	public BlockType getPlaceMode() {
		return this.placemode;
	}
	
	public void setPlaceMode(BlockType placemode) {
		this.placemode = placemode;
	}
	
	@Override
	public boolean canDetach() {
		return false;
	}
	
	@Override
	public boolean canTick() {
		return false;
	}
	
}
