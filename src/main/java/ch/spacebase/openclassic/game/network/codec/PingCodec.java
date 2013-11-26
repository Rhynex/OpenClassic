package ch.spacebase.openclassic.game.network.codec;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.game.network.MessageCodec;
import ch.spacebase.openclassic.game.network.msg.PingMessage;

public class PingCodec extends MessageCodec<PingMessage> {

	public PingCodec() {
		super(PingMessage.class, (byte) 0x01);
	}

	@Override
	public ChannelBuffer encode(PingMessage message) throws IOException {
		return ChannelBuffers.EMPTY_BUFFER;
	}

	@Override
	public PingMessage decode(ChannelBuffer buffer) throws IOException {
		return new PingMessage();
	}

}
