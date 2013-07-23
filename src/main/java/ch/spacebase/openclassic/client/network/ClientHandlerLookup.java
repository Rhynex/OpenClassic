package ch.spacebase.openclassic.client.network;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.BlockChangeMessage;
import ch.spacebase.openclassic.api.network.msg.IdentificationMessage;
import ch.spacebase.openclassic.api.network.msg.LevelDataMessage;
import ch.spacebase.openclassic.api.network.msg.LevelFinalizeMessage;
import ch.spacebase.openclassic.api.network.msg.LevelInitializeMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerChatMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerDespawnMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerDisconnectMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerOpMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerPositionMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerPositionRotationMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerRotationMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerSpawnMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.api.network.msg.custom.CustomMessage;
import ch.spacebase.openclassic.api.network.msg.custom.GameInfoMessage;
import ch.spacebase.openclassic.api.network.msg.custom.LevelColorMessage;
import ch.spacebase.openclassic.api.network.msg.custom.PluginMessage;
import ch.spacebase.openclassic.api.network.msg.custom.audio.AudioPlayMessage;
import ch.spacebase.openclassic.api.network.msg.custom.audio.AudioRegisterMessage;
import ch.spacebase.openclassic.api.network.msg.custom.audio.MusicStopMessage;
import ch.spacebase.openclassic.api.network.msg.custom.block.CustomBlockMessage;
import ch.spacebase.openclassic.client.network.handler.BlockChangeMessageHandler;
import ch.spacebase.openclassic.client.network.handler.IdentificationMessageHandler;
import ch.spacebase.openclassic.client.network.handler.LevelDataMessageHandler;
import ch.spacebase.openclassic.client.network.handler.LevelFinalizeMessageHandler;
import ch.spacebase.openclassic.client.network.handler.LevelInitializeMessageHandler;
import ch.spacebase.openclassic.client.network.handler.PlayerChatMessageHandler;
import ch.spacebase.openclassic.client.network.handler.PlayerDespawnMessageHandler;
import ch.spacebase.openclassic.client.network.handler.PlayerDisconnectMessageHandler;
import ch.spacebase.openclassic.client.network.handler.PlayerOpMessageHandler;
import ch.spacebase.openclassic.client.network.handler.PlayerPositionMessageHandler;
import ch.spacebase.openclassic.client.network.handler.PlayerPositionRotationMessageHandler;
import ch.spacebase.openclassic.client.network.handler.PlayerRotationMessageHandler;
import ch.spacebase.openclassic.client.network.handler.PlayerSpawnMessageHandler;
import ch.spacebase.openclassic.client.network.handler.PlayerTeleportMessageHandler;
import ch.spacebase.openclassic.client.network.handler.custom.AudioPlayMessageHandler;
import ch.spacebase.openclassic.client.network.handler.custom.AudioRegisterMessageHandler;
import ch.spacebase.openclassic.client.network.handler.custom.CustomBlockMessageHandler;
import ch.spacebase.openclassic.client.network.handler.custom.CustomMessageHandler;
import ch.spacebase.openclassic.client.network.handler.custom.GameInfoMessageHandler;
import ch.spacebase.openclassic.client.network.handler.custom.LevelColorMessageHandler;
import ch.spacebase.openclassic.client.network.handler.custom.MusicStopMessageHandler;
import ch.spacebase.openclassic.client.network.handler.custom.PluginMessageHandler;
import ch.spacebase.openclassic.game.network.HandlerLookup;

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
