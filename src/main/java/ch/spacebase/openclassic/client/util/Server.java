package ch.spacebase.openclassic.client.util;

@SuppressWarnings("unused")
public class Server {

	private String name = "Unnamed";
	private int users = 0;
	private int max = 0;
	private String serverId = "";

	public Server(String name, int users, int max, String id) {
		this.name = name;
		this.users = users;
		this.max = max;
		this.serverId = id;
	}
	
	public String getName() {
		return this.name;
	}

	public String getUrl() {
		return "http://minecraft.net/classic/play/" + this.serverId;
	}

}
