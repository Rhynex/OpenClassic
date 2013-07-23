package ch.spacebase.openclassic.game.network.codec;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.network.msg.PlayerPositionMessage;

public class PlayerPositionCodec extends MessageCodec<PlayerPositionMessage> {

	public PlayerPositionCodec() {
		super(PlayerPositionMessage.class, (byte) 0x0a);
	}

	@Override
	public ChannelBuffer encode(PlayerPositionMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.buffer(7);

		buffer.writeByte(message.getPlayerId());
		buffer.writeShort((short) (message.getXChange() * 32));
		buffer.writeShort((short) (message.getYChange() * 32));
		buffer.writeShort((short) (message.getZChange() * 32));

		return buffer;
	}

	@Override
	public PlayerPositionMessage decode(ChannelBuffer buffer) throws IOException {
		byte playerId = buffer.readByte();
		float xChange = buffer.readShort() / 32f;
		float yChange = buffer.readShort() / 32f;
		float zChange = buffer.readShort() / 32f;

		return new PlayerPositionMessage(playerId, xChange, yChange, zChange);
	}

}
