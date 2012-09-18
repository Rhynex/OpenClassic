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
		double xChange = buffer.readByte() / 32.0;
		double yChange = buffer.readByte() / 32.0;
		double zChange = buffer.readByte() / 32.0;
		
		return new PlayerPositionMessage(playerId, xChange, yChange, zChange);
	}

}
