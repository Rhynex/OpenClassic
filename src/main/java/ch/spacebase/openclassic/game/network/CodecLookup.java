package ch.spacebase.openclassic.game.network;

import java.util.HashMap;
import java.util.Map;

import ch.spacebase.openclassic.game.network.codec.BlockChangeCodec;
import ch.spacebase.openclassic.game.network.codec.IdentificationCodec;
import ch.spacebase.openclassic.game.network.codec.LevelDataCodec;
import ch.spacebase.openclassic.game.network.codec.LevelFinalizeCodec;
import ch.spacebase.openclassic.game.network.codec.LevelInitializeCodec;
import ch.spacebase.openclassic.game.network.codec.PingCodec;
import ch.spacebase.openclassic.game.network.codec.PlayerChatCodec;
import ch.spacebase.openclassic.game.network.codec.PlayerDespawnCodec;
import ch.spacebase.openclassic.game.network.codec.PlayerDisconnectCodec;
import ch.spacebase.openclassic.game.network.codec.PlayerOpCodec;
import ch.spacebase.openclassic.game.network.codec.PlayerPositionCodec;
import ch.spacebase.openclassic.game.network.codec.PlayerPositionRotationCodec;
import ch.spacebase.openclassic.game.network.codec.PlayerRotationCodec;
import ch.spacebase.openclassic.game.network.codec.PlayerSetBlockCodec;
import ch.spacebase.openclassic.game.network.codec.PlayerSpawnCodec;
import ch.spacebase.openclassic.game.network.codec.PlayerTeleportCodec;
import ch.spacebase.openclassic.game.network.codec.custom.AudioPlayCodec;
import ch.spacebase.openclassic.game.network.codec.custom.AudioRegisterCodec;
import ch.spacebase.openclassic.game.network.codec.custom.CustomBlockCodec;
import ch.spacebase.openclassic.game.network.codec.custom.CustomCodec;
import ch.spacebase.openclassic.game.network.codec.custom.GameInfoCodec;
import ch.spacebase.openclassic.game.network.codec.custom.KeyChangeCodec;
import ch.spacebase.openclassic.game.network.codec.custom.LevelColorCodec;
import ch.spacebase.openclassic.game.network.codec.custom.MusicStopCodec;
import ch.spacebase.openclassic.game.network.codec.custom.PluginCodec;
import ch.spacebase.openclassic.game.network.msg.Message;

public final class CodecLookup {

	private static final MessageCodec<?>[] opcodeTable = new MessageCodec<?>[256];
	private static final Map<Class<? extends Message>, MessageCodec<?>> classTable = new HashMap<Class<? extends Message>, MessageCodec<?>>();

	static {
		try {
			bind(IdentificationCodec.class);
			bind(PingCodec.class);
			bind(LevelInitializeCodec.class);
			bind(LevelDataCodec.class);
			bind(LevelFinalizeCodec.class);
			bind(PlayerSetBlockCodec.class);
			bind(BlockChangeCodec.class);
			bind(PlayerSpawnCodec.class);
			bind(PlayerTeleportCodec.class);
			bind(PlayerPositionRotationCodec.class);
			bind(PlayerPositionCodec.class);
			bind(PlayerRotationCodec.class);
			bind(PlayerDespawnCodec.class);
			bind(PlayerChatCodec.class);
			bind(PlayerDisconnectCodec.class);
			bind(PlayerOpCodec.class);

			// Custom
			bind(GameInfoCodec.class);
			bind(CustomBlockCodec.class);
			bind(KeyChangeCodec.class);
			bind(LevelColorCodec.class);
			bind(AudioRegisterCodec.class);
			bind(AudioPlayCodec.class);
			bind(MusicStopCodec.class);
			bind(PluginCodec.class);
			bind(CustomCodec.class);
		} catch(Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private static <T extends Message, C extends MessageCodec<T>> void bind(Class<C> clazz) throws InstantiationException, IllegalAccessException {
		MessageCodec<T> codec = clazz.newInstance();
		opcodeTable[codec.getOpcode()] = codec;
		classTable.put(codec.getType(), codec);
	}

	public static MessageCodec<?> find(int opcode) {
		return opcodeTable[opcode];
	}

	@SuppressWarnings("unchecked")
	public static <T extends Message> MessageCodec<T> find(Class<T> clazz) {
		return (MessageCodec<T>) classTable.get(clazz);
	}

	private CodecLookup() {
	}

}
