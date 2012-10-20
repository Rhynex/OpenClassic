package ch.spacebase.openclassic.game.level.column;

import static org.lwjgl.opengl.GL11.GL_COMPILE;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.lwjgl.opengl.GL11.glDeleteLists;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.model.EmptyModel;
import ch.spacebase.openclassic.api.level.column.Chunk;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.api.util.storage.TripleIntByteArray;

public class ClassicChunk implements Chunk {
	
	private ClassicColumn column;
	private TripleIntByteArray blocks = new TripleIntByteArray(Constants.CHUNK_WIDTH, Constants.CHUNK_HEIGHT, Constants.CHUNK_DEPTH);
	private int y;
	
	private int list = -1;
	private boolean empty = true;
	
	public ClassicChunk(ClassicColumn column, int y) {
		this.column = column;
		this.y = y;
	}
	
	public ClassicColumn getColumn() {
		return this.column;
	}
	
	public int getX() {
		return this.column.getX();
	}
	
	public int getY() {
		return this.y;
	}
	
	public int getZ() {
		return this.column.getZ();
	}
	
	public int getWorldX() {
		return this.getX() << 4;
	}
	
	public int getWorldY() {
		return this.getY() << 4;
	}
	
	public int getWorldZ() {
		return this.getZ() << 4;
	}
	
	public byte getBlockAt(int x, int y, int z) {
		if(x < this.getWorldX() || y < this.getWorldY() || z < this.getWorldZ() || x >= this.getWorldX() + Constants.CHUNK_WIDTH || y >= this.getWorldY() + Constants.CHUNK_HEIGHT || z >= this.getWorldZ() + Constants.CHUNK_DEPTH)
			return this.getColumn().getBlockAt(x, y, z);
		
		return this.blocks.get(x - this.getWorldX(), y - this.getWorldY(), z - this.getWorldZ());
	}
	
	public void setBlockAt(int x, int y, int z, byte id) {
		if(x < this.getWorldX() || y < this.getWorldY() || z < this.getWorldZ() || x >= this.getWorldX() + Constants.CHUNK_WIDTH || y >= this.getWorldY() + Constants.CHUNK_HEIGHT || z >= this.getWorldZ() + Constants.CHUNK_DEPTH) {
			this.getColumn().setBlockAt(x, y, z, id);
			return;
		}
		
		this.blocks.set(x - this.getWorldX(), y - this.getWorldY(), z - this.getWorldZ(), id);
		this.column.updateHeightMap(x - 1, z - 1, x + 1, z + 1);
	}
	
	public int blockIndex(int x, int y, int z) {
		return this.blocks.toIndex(x, y, z);
	}
	
	public byte[] getBlocks() {
		return this.blocks.get();
	}
	
	public void setBlocks(byte blocks[], boolean calc) {
		this.blocks.set(blocks);
		this.column.updateHeightMap(this.getWorldX(), this.getWorldZ(), this.getWorldX() + Constants.CHUNK_WIDTH - 1, this.getWorldZ() + Constants.CHUNK_DEPTH - 1);
	}
	
	public int getList() {
		return this.list;
	}
	
	public boolean isEmpty() {
		return this.empty;
	}
	
	public void render() {
		if(this.list == -1) {
			this.list = glGenLists(1);
		}
		
		this.empty = true;
		glNewList(this.list, GL_COMPILE);
		for(int x = this.getWorldX(); x < this.getWorldX() + Constants.CHUNK_WIDTH; x++) {
			for(int y = this.getWorldY(); y < this.getWorldY() + Constants.CHUNK_HEIGHT; y++) {
				for(int z = this.getWorldZ(); z < this.getWorldZ() + Constants.CHUNK_DEPTH; z++) {
					BlockType type = Blocks.fromId(this.getBlockAt(x, y, z));
					if(!(type.getModel() instanceof EmptyModel)) {
						this.empty = false;
						type.getModel().render(type, x, y, z, this.column.getBrightness(x, y, z));
					}
				}
			}
		}
		
		glEndList();
	}
	
	public void dispose() {
		glDeleteLists(this.list, 1);
	}
	
	public void update() {
		// TODO
	}
	
	@Override
	public String toString() {
		return "Chunk{x=" + this.column.getX() + ",y=" + this.y + ",z=" + this.column.getZ() + "}";
	}
	
}
