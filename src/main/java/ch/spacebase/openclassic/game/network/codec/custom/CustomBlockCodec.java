package ch.spacebase.openclassic.game.network.codec.custom;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.network.msg.custom.block.CustomBlockMessage;
import ch.spacebase.openclassic.game.network.codec.MessageCodec;
import ch.spacebase.openclassic.game.util.ChannelBufferUtils;

public class CustomBlockCodec extends MessageCodec<CustomBlockMessage> {

	public CustomBlockCodec() {
		super(CustomBlockMessage.class, (byte) 0x11);
	}

	@Override
	public ChannelBuffer encode(CustomBlockMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		buffer.writeByte(message.getBlock().getId());
		buffer.writeByte(message.getBlock().getData());
		buffer.writeByte(message.getBlock().isOpaque() ? 1 : 0);
		ChannelBufferUtils.writeString(buffer, message.getBlock().getStepSound().name());
		buffer.writeByte(message.getBlock().isLiquid() ? 1 : 0);
		buffer.writeInt(message.getBlock().getTickDelay());
		buffer.writeFloat(message.getBlock().getBrightness());
		
		return buffer;
	}

	@Override
	public CustomBlockMessage decode(ChannelBuffer buffer) throws IOException {
		byte id = buffer.readByte();
		byte data = buffer.readByte();
		boolean opaque = buffer.readByte() == 1;
		StepSound sound = StepSound.valueOf(ChannelBufferUtils.readString(buffer));
		boolean liquid = buffer.readByte() == 1;
		int delay = buffer.readInt();
		float brightness = buffer.readFloat();
		
		BlockType block = new BlockType(id, data, sound, null);
		block.setLiquid(liquid);
		block.setOpaque(opaque);
		block.setTickDelay(delay);
		block.setBrightness(brightness);
		
		return new CustomBlockMessage(block);
	}
	
}
