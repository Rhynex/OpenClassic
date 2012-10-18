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

public class ClassicChunk implements Chunk {
	
	private ClassicColumn column;
	private byte blocks[] = new byte[Constants.CHUNK_WIDTH * Constants.CHUNK_HEIGHT * Constants.CHUNK_DEPTH];
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
			return this.column.getLevel().getBlockIdAt(x, y, z);
		
		return this.blocks[this.coordsToBlockIndex(x, y, z)];
	}
	
	public void setBlockAt(int x, int y, int z, byte id) {
		if(x < this.getWorldX() || y < this.getWorldY() || z < this.getWorldZ() || x >= this.getWorldX() + Constants.CHUNK_WIDTH || y >= this.getWorldY() + Constants.CHUNK_HEIGHT || z >= this.getWorldZ() + Constants.CHUNK_DEPTH)
			this.column.getLevel().setBlockIdAt(x, y, z, id);
		
		this.blocks[this.coordsToBlockIndex(x, y, z)] = id;
	}
	
	public byte[] getBlocks() {
		return this.blocks;
	}
	
	public void setBlocks(byte blocks[]) {
		this.blocks = blocks;
	}
	
	public int coordsToBlockIndex(int x, int y, int z) {
		x -= this.getWorldX();
		y -= this.getWorldY();
		z -= this.getWorldZ();
		return x + (z * Constants.CHUNK_WIDTH) + (y * Constants.CHUNK_WIDTH * Constants.CHUNK_DEPTH);
	}
	
	public float getBrightness(int x, int y, int z) {
		return 1; // TODO: rework lighting
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
						type.getModel().render(type, x, y, z, this.getBrightness(x, y, z));
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
