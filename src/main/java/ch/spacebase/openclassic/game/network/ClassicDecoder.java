package ch.spacebase.openclassic.game.network;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;
import org.jboss.netty.handler.codec.replay.VoidEnum;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.game.network.codec.MessageCodec;
import ch.spacebase.openclassic.server.network.ServerSession;

public class ClassicDecoder extends ReplayingDecoder<VoidEnum> {

	private int previousOpcode = -1;

	@Override
	protected Object decode(ChannelHandlerContext context, Channel channel, ChannelBuffer buffer, VoidEnum state) throws Exception {
		int opcode = buffer.readUnsignedByte();
		MessageCodec<?> codec = CodecLookup.find(opcode);

		if(codec == null) {
			OpenClassic.getLogger().warning("Invalid packet ID " + opcode + "! (previous ID = " + this.previousOpcode + ") Disconnecting user...");
			if(context.getAttachment() instanceof ServerSession) {
				((ServerSession) context.getAttachment()).disconnect("Invalid packet ID " + opcode + "!");
			} else {
				channel.disconnect();
			}

			return null;
		}

		this.previousOpcode = opcode;
		return codec.decode(buffer);
	}

}
