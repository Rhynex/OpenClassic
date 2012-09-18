package ch.spacebase.openclassic.game.network;

import java.util.HashMap;
import java.util.Map;


import ch.spacebase.openclassic.api.network.msg.*;
import ch.spacebase.openclassic.api.network.msg.custom.*;
import ch.spacebase.openclassic.api.network.msg.custom.audio.*;
import ch.spacebase.openclassic.api.network.msg.custom.block.*;
import ch.spacebase.openclassic.game.network.handler.*;
import ch.spacebase.openclassic.game.network.handler.custom.*;

public final class HandlerLookupService {

	private static final Map<Class<? extends Message>, MessageHandler<?>> handlers = new HashMap<Class<? extends Message>, MessageHandler<?>>();
	
	static {
		try {
			bind(IdentificationMessage.class, IdentificationMessageHandler.class);
			bind(LevelInitializeMessage.class, LevelInitializeMessageHandler.class);
			bind(LevelDataMessage.class, LevelDataMessageHandler.class);
			bind(LevelFinalizeMessage.class, LevelFinalizeMessageHandler.class);
			bind(PlayerSetBlockMessage.class, PlayerSetBlockMessageHandler.class);
			bind(BlockChangeMessage.class, BlockChangeMessageHandler.class);
			bind(PlayerSpawnMessage.class, PlayerSpawnMessageHandler.class);
			bind(PlayerTeleportMessage.class, PlayerTeleportMessageHandler.class);
			bind(PlayerPositionRotationMessage.class, PlayerPositionRotationMessageHandler.class);
			bind(PlayerPositionMessage.class, PlayerPositionMessageHandler.class);
			bind(PlayerRotationMessage.class, PlayerRotationMessageHandler.class);
			bind(PlayerDespawnMessage.class, PlayerDespawnMessageHandler.class);
			bind(PlayerChatMessage.class, PlayerChatMessageHandler.class);
			bind(PlayerDisconnectMessage.class, PlayerDisconnectMessageHandler.class);
			bind(PlayerOpMessage.class, PlayerOpMessageHandler.class);
			
			// Custom
			bind(GameInfoMessage.class, ClientInfoMessageHandler.class);
			bind(CustomBlockMessage.class, CustomBlockMessageHandler.class);
			bind(BlockModelMessage.class, BlockModelMessageHandler.class);
			bind(QuadMessage.class, QuadMessageHandler.class);
			bind(KeyChangeMessage.class, KeyChangeMessageHandler.class);
			bind(LevelColorMessage.class, LevelColorMessageHandler.class);
			bind(AudioRegisterMessage.class, AudioRegisterMessageHandler.class);
			bind(AudioPlayMessage.class, AudioPlayMessageHandler.class);
			bind(MusicStopMessage.class, MusicStopMessageHandler.class);
			bind(PluginMessage.class, PluginMessageHandler.class);
			bind(CustomMessage.class, CustomMessageHandler.class);
		} catch(Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private static <T extends Message> void bind(Class<T> clazz, Class<? extends MessageHandler<T>> handlerClass) throws InstantiationException, IllegalAccessException {
		MessageHandler<T> handler = handlerClass.newInstance();
		handlers.put(clazz, handler);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Message> MessageHandler<T> find(Class<T> clazz) {
		return (MessageHandler<T>) handlers.get(clazz);
	}

	private HandlerLookupService() {
	}
	
}
