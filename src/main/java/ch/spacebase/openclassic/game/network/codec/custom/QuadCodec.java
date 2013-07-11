package ch.spacebase.openclassic.game.network.codec.custom;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.asset.AssetSource;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.block.model.Vertex;
import ch.spacebase.openclassic.api.network.msg.custom.block.QuadMessage;
import ch.spacebase.openclassic.game.network.codec.MessageCodec;
import ch.spacebase.openclassic.game.util.ChannelBufferUtils;

// TODO: rewrite
public class QuadCodec extends MessageCodec<QuadMessage> {

	public QuadCodec() {
		super(QuadMessage.class, (byte) 0x13);
	}

	@Override
	public ChannelBuffer encode(QuadMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		buffer.writeByte(message.getBlock());
		buffer.writeInt(message.getQuad().getId());
		for(Vertex vertex : message.getQuad().getVertices()) {
			buffer.writeFloat(vertex.getX());
			buffer.writeFloat(vertex.getY());
			buffer.writeFloat(vertex.getZ());
		}
			
		ChannelBufferUtils.writeString(buffer, message.getQuad().getTexture().getParent().getFile());
		buffer.writeByte(message.getQuad().getTexture().getParent().getSource() == AssetSource.JAR ? 1 : 0);
		buffer.writeInt(message.getQuad().getTexture().getParent().getWidth());
		buffer.writeInt(message.getQuad().getTexture().getParent().getHeight());
		buffer.writeFloat(message.getQuad().getTexture().getWidth());
		buffer.writeFloat(message.getQuad().getTexture().getHeight());
		buffer.writeFloat(message.getQuad().getTexture().getX1());
		buffer.writeFloat(message.getQuad().getTexture().getY1());
		
		return buffer;
	}

	@Override
	public QuadMessage decode(ChannelBuffer buffer) throws IOException {
		byte block = buffer.readByte();
		int id = buffer.readInt();
			
		Vertex vertices[] = new Vertex[4];
		for(int vCount = 0; vCount < 4; vCount++) {
			float x = buffer.readFloat();
			float y = buffer.readFloat();
			float z = buffer.readFloat();
				
			vertices[vCount] = new Vertex(x, y, z);
		}
			
		/* String texture = ChannelBufferUtils.readString(buffer);
		boolean jar = buffer.readByte() == 1;
		int width = buffer.readInt();
		int height = buffer.readInt();
		int swidth = buffer.readInt();
		int sheight = buffer.readInt();
			
		Texture t = new Texture(texture, jar, width, height, swidth, sheight); */
		Quad quad = new Quad(id, null, vertices[0], vertices[1], vertices[2], vertices[3]);

		return new QuadMessage(block, quad);
	}
	
}
