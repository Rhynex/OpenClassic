package ch.spacebase.openclassic.game.component.player;

import ch.spacebase.openclassic.api.component.Component;

public class PlaceModeComponent extends Component {

	private byte placemode = 0;
	
	public byte getPlaceMode() {
		return this.placemode;
	}
	
	public void setPlaceMode(byte placemode) {
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
