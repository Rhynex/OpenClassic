package ch.spacebase.openclassic.client.util;

public class LoginInfo {

	private static String name = "Player";
	private static String key;
	
	public static String getName() {
		return name;
	}
	
	public static void setName(String name) {
		LoginInfo.name = name;
	}
	
	public static String getKey() {
		return key;
	}
	
	public static void setKey(String key) {
		LoginInfo.key = key;
	}
	
}
