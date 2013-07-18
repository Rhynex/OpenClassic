package ch.spacebase.openclassic.game.util;

public class EmptyMessageFormatter implements MessageFormatter {

	@Override
	public String format(String message) {
		return message;
	}

}
