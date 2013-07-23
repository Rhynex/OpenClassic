package ch.spacebase.openclassic.server.ui;

import ch.spacebase.openclassic.api.command.Console;

public interface ConsoleManager {

	public static final Console SENDER = new Console();

	public void stop();

	public void setup();

	public String formatOutput(String message);

}
