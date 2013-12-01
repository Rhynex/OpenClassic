package ch.spacebase.openclassic.game.network.codec.custom;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.game.network.MessageCodec;
import ch.spacebase.openclassic.game.network.msg.custom.GameInfoMessage;
import ch.spacebase.openclassic.server.util.ChannelBufferUtils;

public class GameInfoCodec extends MessageCodec<GameInfoMessage> {

	public GameInfoCodec() {
		super(GameInfoMessage.class, (byte) 0x10);
	}

	@Override
	public ChannelBuffer encode(GameInfoMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		ChannelBufferUtils.writeExtendedString(buffer, message.getVersion());
		ChannelBufferUtils.writeExtendedString(buffer, message.getLanguage());

		return buffer;
	}

	@Override
	public GameInfoMessage decode(ChannelBuffer buffer) throws IOException {
		String version = ChannelBufferUtils.readExtendedString(buffer);
		String language = ChannelBufferUtils.readExtendedString(buffer);
		return new GameInfoMessage(version, language);
	}

}
