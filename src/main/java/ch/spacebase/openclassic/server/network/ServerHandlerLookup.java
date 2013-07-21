package ch.spacebase.openclassic.server.network;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.*;
import ch.spacebase.openclassic.api.network.msg.custom.*;
import ch.spacebase.openclassic.game.network.HandlerLookup;
import ch.spacebase.openclassic.server.network.handler.*;
import ch.spacebase.openclassic.server.network.handler.custom.*;

public class ServerHandlerLookup extends HandlerLookup {

	public ServerHandlerLookup() {
		try {
			this.bind(IdentificationMessage.class, IdentificationMessageHandler.class);
			this.bind(PlayerSetBlockMessage.class, PlayerSetBlockMessageHandler.class);
			this.bind(PlayerTeleportMessage.class, PlayerTeleportMessageHandler.class);
			this.bind(PlayerChatMessage.class, PlayerChatMessageHandler.class);
			
			// Custom
			this.bind(GameInfoMessage.class, GameInfoMessageHandler.class);
			this.bind(KeyChangeMessage.class, KeyChangeMessageHandler.class);
			this.bind(PluginMessage.class, PluginMessageHandler.class);
			this.bind(CustomMessage.class, CustomMessageHandler.class);
		} catch(Exception e) {
			OpenClassic.getLogger().severe("Failed to register network messages!");
			e.printStackTrace();
		}
	}
	
}
