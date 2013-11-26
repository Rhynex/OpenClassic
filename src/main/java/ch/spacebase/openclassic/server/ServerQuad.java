package ch.spacebase.openclassic.server;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;

import ch.spacebase.openclassic.api.block.model.Model;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.block.model.Vertex;

public class ServerQuad implements Quad {

	protected int id;
	private Vertex vertices[] = new Vertex[4];
	private SubTexture texture;
	private Model parent;
	
	public ServerQuad(int id, SubTexture texture) {
		this.texture = texture;
		this.id = id;
	}
	
	public ServerQuad(int id, SubTexture texture, Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
		this(id, texture);
		this.addVertex(0, v1);
		this.addVertex(1, v2);
		this.addVertex(2, v3);
		this.addVertex(3, v4);
	}
	
	@Override
	public int getId() {
		return this.id;
	}
	
	@Override
	public void addVertex(int id, Vertex vertex) {
		Validate.isTrue(id >= 0 && id <= 3, "Quad can only have 4 vertices with IDs 0 - 3!");
		this.vertices[id] = vertex;
	}
	
	@Override
	public void addVertex(int id, float x, float y, float z) {
		this.addVertex(id, new Vertex(x, y, z));
	}
	
	@Override
	public void removeVertex(int id) {
		Validate.isTrue(id >= 0 && id <= 3, "Quad can only have 4 vertices with IDs 0 - 3!");
		this.vertices[id] = null;
	}
	
	@Override
	public Vertex getVertex(int id) {
		Validate.isTrue(id >= 0 && id <= 3, "Quad can only have 4 vertices with IDs 0 - 3!");
		return this.vertices[id];
	}
	
	@Override
	public List<Vertex> getVertices() {
		return Arrays.asList(this.vertices);
	}
	
	@Override
	public SubTexture getTexture() {
		return this.texture;
	}
	
	@Override
	public void render(float x, float y, float z, float brightness) {
	}
	
	@Override
	public void render(float x, float y, float z, float brightness, boolean batch) {
	}
	
	@Override
	public void render(float x, float y, float z, float brightness, boolean batch, boolean cull) {
	}
	
	@Override
	public void renderScaled(float x, float y, float z, float scale, float brightness) {
	}

	@Override
	public Model getParent() {
		return this.parent;
	}
	
	@Override
	public void setParent(Model parent) {
		this.parent = parent;
	}
	
}
