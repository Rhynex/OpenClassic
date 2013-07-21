package ch.spacebase.openclassic.client.network;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.*;
import ch.spacebase.openclassic.api.network.msg.custom.*;
import ch.spacebase.openclassic.api.network.msg.custom.audio.*;
import ch.spacebase.openclassic.api.network.msg.custom.block.*;
import ch.spacebase.openclassic.game.network.HandlerLookup;
import ch.spacebase.openclassic.client.network.handler.*;
import ch.spacebase.openclassic.client.network.handler.custom.*;

public class ClientHandlerLookup extends HandlerLookup {

	public ClientHandlerLookup() {
		try {
			this.bind(IdentificationMessage.class, IdentificationMessageHandler.class);
			this.bind(LevelInitializeMessage.class, LevelInitializeMessageHandler.class);
			this.bind(LevelDataMessage.class, LevelDataMessageHandler.class);
			this.bind(LevelFinalizeMessage.class, LevelFinalizeMessageHandler.class);
			this.bind(BlockChangeMessage.class, BlockChangeMessageHandler.class);
			this.bind(PlayerSpawnMessage.class, PlayerSpawnMessageHandler.class);
			this.bind(PlayerTeleportMessage.class, PlayerTeleportMessageHandler.class);
			this.bind(PlayerPositionRotationMessage.class, PlayerPositionRotationMessageHandler.class);
			this.bind(PlayerPositionMessage.class, PlayerPositionMessageHandler.class);
			this.bind(PlayerRotationMessage.class, PlayerRotationMessageHandler.class);
			this.bind(PlayerDespawnMessage.class, PlayerDespawnMessageHandler.class);
			this.bind(PlayerChatMessage.class, PlayerChatMessageHandler.class);
			this.bind(PlayerDisconnectMessage.class, PlayerDisconnectMessageHandler.class);
			this.bind(PlayerOpMessage.class, PlayerOpMessageHandler.class);
			
			// Custom
			this.bind(GameInfoMessage.class, GameInfoMessageHandler.class);
			this.bind(CustomBlockMessage.class, CustomBlockMessageHandler.class);
			this.bind(LevelColorMessage.class, LevelColorMessageHandler.class);
			this.bind(AudioRegisterMessage.class, AudioRegisterMessageHandler.class);
			this.bind(AudioPlayMessage.class, AudioPlayMessageHandler.class);
			this.bind(MusicStopMessage.class, MusicStopMessageHandler.class);
			this.bind(PluginMessage.class, PluginMessageHandler.class);
			this.bind(CustomMessage.class, CustomMessageHandler.class);
		} catch(Exception e) {
			OpenClassic.getLogger().severe("Failed to register network messages!");
			e.printStackTrace();
		}
	}
	
}
