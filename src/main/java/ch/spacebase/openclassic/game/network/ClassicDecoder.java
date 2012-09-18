package ch.spacebase.openclassic.game.network;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;
import org.jboss.netty.handler.codec.replay.VoidEnum;


import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.game.network.codec.MessageCodec;

public class ClassicDecoder extends ReplayingDecoder<VoidEnum> {

	private List<Message> previous = new ArrayList<Message>();

	@Override
	protected Object decode(ChannelHandlerContext context, Channel channel, ChannelBuffer buffer, VoidEnum state) throws Exception {
		int opcode = buffer.readUnsignedByte();
		MessageCodec<?> codec = CodecLookupService.find(opcode);
		
		if (codec == null) {
			OpenClassic.getLogger().warning("Invalid packet ID " + opcode + "! (previous = " + this.previous + ") Disconnecting user...");
			
			if(context.getAttachment() instanceof ClientSession) {
				((ClientSession) context.getAttachment()).disconnect("Invalid packet ID from server: " + opcode);
			}
			
			channel.disconnect();
			return null;
		}

		Message msg = codec.decode(buffer);
		if(this.previous.size() >= 16) this.previous.remove(this.previous.size() - 1);
		this.previous.add(msg);
		
		return msg;
	}
	
}
