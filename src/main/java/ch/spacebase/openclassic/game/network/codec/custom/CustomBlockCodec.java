package ch.spacebase.openclassic.game.network.codec.custom;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.model.CubeModel;
import ch.spacebase.openclassic.api.block.model.CuboidModel;
import ch.spacebase.openclassic.api.block.model.EmptyModel;
import ch.spacebase.openclassic.api.block.model.LiquidModel;
import ch.spacebase.openclassic.api.block.model.Model;
import ch.spacebase.openclassic.api.block.model.PlantModel;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.api.block.model.Vertex;
import ch.spacebase.openclassic.api.math.BoundingBox;
import ch.spacebase.openclassic.api.network.msg.custom.block.CustomBlockMessage;
import ch.spacebase.openclassic.game.network.MessageCodec;
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
		buffer.writeFloat(message.getBlock().getBrightness());
		buffer.writeFloat(message.getBlock().getSpeedModifier());
		buffer.writeInt(message.getBlock().getFogRed());
		buffer.writeInt(message.getBlock().getFogGreen());
		buffer.writeInt(message.getBlock().getFogBlue());
		buffer.writeFloat(message.getBlock().getFogDensity());
		ChannelBufferUtils.writeString(buffer, message.getBlock().getModel().getNetworkClass().getName());

		buffer.writeByte(message.getBlock().getModel().getDefaultCollisionBox() != null ? (byte) 1 : (byte) 0);
		if(message.getBlock().getModel().getDefaultCollisionBox() != null) {
			buffer.writeFloat(message.getBlock().getModel().getDefaultCollisionBox().getX1());
			buffer.writeFloat(message.getBlock().getModel().getDefaultCollisionBox().getX2());
			buffer.writeFloat(message.getBlock().getModel().getDefaultCollisionBox().getY1());
			buffer.writeFloat(message.getBlock().getModel().getDefaultCollisionBox().getY2());
			buffer.writeFloat(message.getBlock().getModel().getDefaultCollisionBox().getZ1());
			buffer.writeFloat(message.getBlock().getModel().getDefaultCollisionBox().getZ2());
		}

		buffer.writeByte(message.getBlock().getModel().getDefaultSelectionBox() != null ? (byte) 1 : (byte) 0);
		if(message.getBlock().getModel().getDefaultSelectionBox() != null) {
			buffer.writeFloat(message.getBlock().getModel().getDefaultSelectionBox().getX1());
			buffer.writeFloat(message.getBlock().getModel().getDefaultSelectionBox().getX2());
			buffer.writeFloat(message.getBlock().getModel().getDefaultSelectionBox().getY1());
			buffer.writeFloat(message.getBlock().getModel().getDefaultSelectionBox().getY2());
			buffer.writeFloat(message.getBlock().getModel().getDefaultSelectionBox().getZ1());
			buffer.writeFloat(message.getBlock().getModel().getDefaultSelectionBox().getZ2());
		}

		buffer.writeInt(message.getBlock().getModel().getQuads().size());
		for(Quad quad : message.getBlock().getModel().getQuads()) {
			buffer.writeInt(quad.getId());
			buffer.writeInt(quad.getVertices().size());
			for(Vertex vertex : quad.getVertices()) {
				buffer.writeFloat(vertex.getX());
				buffer.writeFloat(vertex.getY());
				buffer.writeFloat(vertex.getZ());
			}

			ChannelBufferUtils.writeString(buffer, quad.getTexture().getParent().getTexture());
			buffer.writeByte(quad.getTexture().getParent().isInJar() ? 1 : 0);
			buffer.writeInt(quad.getTexture().getParent().getWidth());
			buffer.writeInt(quad.getTexture().getParent().getHeight());
			buffer.writeInt(quad.getTexture().getParent().getSubTextureWidth());
			buffer.writeInt(quad.getTexture().getParent().getSubTextureHeight());
			buffer.writeInt(quad.getTexture().getId());
		}

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
		float brightness = buffer.readFloat();
		float speedModifier = buffer.readFloat();
		int fogRed = buffer.readInt();
		int fogGreen = buffer.readInt();
		int fogBlue = buffer.readInt();
		float fogDensity = buffer.readFloat();

		String type = ChannelBufferUtils.readString(buffer);
		Model model = type.equals(EmptyModel.class.getName()) ? new EmptyModel() : type.equals(LiquidModel.class.getName()) ? new LiquidModel("/terrain.png", 16) : (type.equals(CuboidModel.class.getName()) ? new CuboidModel("/terrain.png", 16, 0, 0, 0, 1, 1, 1) : (type.equals(CubeModel.class.getName()) ? new CubeModel("/terrain.png", 16) : type.equals(PlantModel.class.getName()) ? new PlantModel("/terrain.png", 16) : new Model()));
		model.clearQuads();

		if(buffer.readByte() == 1) {
			float x1 = buffer.readFloat();
			float x2 = buffer.readFloat();
			float y1 = buffer.readFloat();
			float y2 = buffer.readFloat();
			float z1 = buffer.readFloat();
			float z2 = buffer.readFloat();
			model.setCollisionBox(new BoundingBox(x1, y1, z1, x2, y2, z2));
		}

		if(buffer.readByte() == 1) {
			float sx1 = buffer.readFloat();
			float sx2 = buffer.readFloat();
			float sy1 = buffer.readFloat();
			float sy2 = buffer.readFloat();
			float sz1 = buffer.readFloat();
			float sz2 = buffer.readFloat();
			model.setSelectionBox(new BoundingBox(sx1, sy1, sz1, sx2, sy2, sz2));
		}

		int quads = buffer.readInt();
		for(int i = 0; i < quads; i++) {
			int qid = buffer.readInt();
			Vertex vertices[] = new Vertex[buffer.readInt()];
			for(int ind = 0; ind < vertices.length; ind++) {
				float x = buffer.readFloat();
				float y = buffer.readFloat();
				float z = buffer.readFloat();

				vertices[ind] = new Vertex(x, y, z);
			}

			String texture = ChannelBufferUtils.readString(buffer);
			boolean jar = buffer.readByte() == 1;
			int width = buffer.readInt();
			int height = buffer.readInt();
			int swidth = buffer.readInt();
			int sheight = buffer.readInt();

			Texture t = new Texture(texture, jar, width, height, swidth, sheight);
			model.addQuad(new Quad(qid, t.getSubTexture(buffer.readInt()), vertices[0], vertices[1], vertices[2], vertices[3]));
		}

		BlockType block = new BlockType(id, sound, model);
		block.setOpaque(opaque);
		block.setLiquid(liquid);
		block.setSelectable(selectable);
		block.setTickDelay(delay);
		block.setPreventsRendering(preventsRendering);
		block.setPlaceIn(placeIn);
		block.setGas(gas);
		block.setPreventsOwnRendering(preventsOwnRendering);
		block.setBrightness(brightness);
		block.setSpeedModifier(speedModifier);
		block.setFogRed(fogRed);
		block.setFogGreen(fogGreen);
		block.setFogBlue(fogBlue);
		block.setFogDensity(fogDensity);

		return new CustomBlockMessage(block);
	}

}
