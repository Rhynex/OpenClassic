package ch.spacebase.openclassic.game.util;

public class InternalConstants {

	/**
	 * The protocol version.
	 */
	public static final byte PROTOCOL_VERSION = 7;
	
	/**
	 * The OpenClassic protocol version.
	 */
	public static final byte OPENCLASSIC_PROTOCOL_VERSION = 8;
	
	/**
	 * Number of ticks per second
	 */
	public static final int TICKS_PER_SECOND = 20;
	
	/**
	 * Number of physics ticks per second.
	 */
	public static final int PHYSICS_PER_SECOND = 10;
	
	/**
	 * Represents the packet code for a player who isn't an op.
	 */
	public static final byte NOT_OP = 0x00;
	
	/**
	 * Represents the packet code for a player who is an op.
	 */
	public static final byte OP = 0x64;
	
	/**
	 * The URL of the Minecraft website using https.
	 */
	public static final String MINECRAFT_URL_HTTPS = "https://minecraft.net/";
	
	/**
	 * The URL of the Minecraft website using http.
	 */
	public static final String MINECRAFT_URL_HTTP = "http://minecraft.net/";
	
	/**
	 * Sensitivity values for different client sensitivity settings.
	 */
	public static final double[] SENSITIVITY_VALUE = new double[] { 0.05D, 0.15D, 0.3D, 0.5D, 0.75D };
	
}
