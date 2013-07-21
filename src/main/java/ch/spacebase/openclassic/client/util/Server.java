package ch.spacebase.openclassic.client.util;

import ch.spacebase.openclassic.api.util.Constants;

public class Server {

	public String name = "Unnamed";
	public int users = 0;
	public int max = 0;
	public String serverId = "";

	public Server(String name, int users, int max, String id) {
		this.name = name;
		this.users = users;
		this.max = max;
		this.serverId = id;
	}

	public String getUrl() {
		return Constants.MINECRAFT_URL_HTTPS + "classic/play/" + this.serverId;
	}

}
