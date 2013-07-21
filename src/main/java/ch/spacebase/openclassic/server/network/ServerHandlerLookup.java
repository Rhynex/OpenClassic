package ch.spacebase.openclassic.server.network;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.IdentificationMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerChatMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerSetBlockMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.api.network.msg.custom.CustomMessage;
import ch.spacebase.openclassic.api.network.msg.custom.GameInfoMessage;
import ch.spacebase.openclassic.api.network.msg.custom.KeyChangeMessage;
import ch.spacebase.openclassic.api.network.msg.custom.PluginMessage;
import ch.spacebase.openclassic.game.network.HandlerLookup;
import ch.spacebase.openclassic.server.network.handler.IdentificationMessageHandler;
import ch.spacebase.openclassic.server.network.handler.PlayerChatMessageHandler;
import ch.spacebase.openclassic.server.network.handler.PlayerSetBlockMessageHandler;
import ch.spacebase.openclassic.server.network.handler.PlayerTeleportMessageHandler;
import ch.spacebase.openclassic.server.network.handler.custom.CustomMessageHandler;
import ch.spacebase.openclassic.server.network.handler.custom.GameInfoMessageHandler;
import ch.spacebase.openclassic.server.network.handler.custom.KeyChangeMessageHandler;
import ch.spacebase.openclassic.server.network.handler.custom.PluginMessageHandler;

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
