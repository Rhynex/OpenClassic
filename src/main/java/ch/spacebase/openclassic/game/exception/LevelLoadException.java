package ch.spacebase.openclassic.game.exception;

import java.io.IOException;

public class LevelLoadException extends IOException {

	private static final long serialVersionUID = 1L;
	
	public LevelLoadException(Throwable t) {
		super(t);
	}

}
