package ch.spacebase.openclassic.client.render;

import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.block.model.QuadFactory;
import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.block.model.Vertex;

public class ClientQuadFactory extends QuadFactory {

	@Override
	public Quad newQuad(int id, SubTexture texture) {
		return new ClientQuad(id, texture);
	}

	@Override
	public Quad newQuad(int id, SubTexture texture, Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
		return new ClientQuad(id, texture, v1, v2, v3, v4);
	}

}
