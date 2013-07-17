package ch.spacebase.openclassic.server.network.codec.custom;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.model.Model;
import ch.spacebase.openclassic.api.network.msg.custom.block.CustomBlockMessage;
import ch.spacebase.openclassic.server.network.codec.MessageCodec;
import ch.spacebase.openclassic.server.util.ChannelBufferUtils;

public class CustomBlockCodec extends MessageCodec<CustomBlockMessage> {

	public CustomBlockCodec() {
		super(CustomBlockMessage.class, (byte) 0x11);
	}

	@Override
	public ChannelBuffer encode(CustomBlockMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		buffer.writeByte(message.getBlock().getId());
		buffer.writeByte(message.getBlock().isOpaque() ? 1 : 0);
		buffer.writeByte(message.getBlock().isSelectable() ? 1 : 0);
		ChannelBufferUtils.writeString(buffer, message.getBlock().getStepSound().name());
		buffer.writeByte(message.getBlock().isLiquid() ? 1 : 0);
		buffer.writeInt(message.getBlock().getTickDelay());
		buffer.writeByte(message.getBlock().getPreventsRendering() ? 1 : 0);
		buffer.writeByte(message.getBlock().canPlaceIn() ? 1 : 0);
		buffer.writeByte(message.getBlock().isGas() ? 1 : 0);
		buffer.writeByte(message.getBlock().getPreventsOwnRenderingRaw() ? 1 : 0);
		
		return buffer;
	}

	@Override
	public CustomBlockMessage decode(ChannelBuffer buffer) throws IOException {
		byte id = buffer.readByte();
		boolean opaque = buffer.readByte() == 1;
		boolean selectable = buffer.readByte() == 1;
		StepSound sound = StepSound.valueOf(ChannelBufferUtils.readString(buffer));
		boolean liquid = buffer.readByte() == 1;
		int delay = buffer.readInt();
		boolean preventsRendering = buffer.readByte() == 1;
		boolean placeIn = buffer.readByte() == 1;
		boolean gas = buffer.readByte() == 1;
		boolean preventsOwnRendering = buffer.readByte() == 1;
		
		BlockType block = new BlockType(id, sound, (Model) null);
		block.setOpaque(opaque);
		block.setLiquid(liquid);
		block.setSelectable(selectable);
		block.setTickDelay(delay);
		block.setPreventsRendering(preventsRendering);
		block.setPlaceIn(placeIn);
		block.setGas(gas);
		block.setPreventsOwnRendering(preventsOwnRendering);
		
		return new CustomBlockMessage(block);
	}
	
}
