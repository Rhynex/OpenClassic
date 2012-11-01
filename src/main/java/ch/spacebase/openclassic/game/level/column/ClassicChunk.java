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
import ch.spacebase.openclassic.api.level.generator.biome.Biome;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.api.util.storage.BlockStore;

public class ClassicChunk implements Chunk {
	
	private ClassicColumn column;
	private BlockStore blocks = new BlockStore(Constants.CHUNK_WIDTH, Constants.CHUNK_HEIGHT, Constants.CHUNK_DEPTH);
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
		
		return this.blocks.getBlock(x, y, z);
	}
	
	public byte getData(int x, int y, int z) {
		if(x < this.getWorldX() || y < this.getWorldY() || z < this.getWorldZ() || x >= this.getWorldX() + Constants.CHUNK_WIDTH || y >= this.getWorldY() + Constants.CHUNK_HEIGHT || z >= this.getWorldZ() + Constants.CHUNK_DEPTH)
			return this.getColumn().getData(x, y, z);
		
		return this.blocks.getData(x, y, z);
	}
	
	public void setBlockAt(int x, int y, int z, BlockType type) {
		if(type == null) return;
		if(x < this.getWorldX() || y < this.getWorldY() || z < this.getWorldZ() || x >= this.getWorldX() + Constants.CHUNK_WIDTH || y >= this.getWorldY() + Constants.CHUNK_HEIGHT || z >= this.getWorldZ() + Constants.CHUNK_DEPTH) {
			this.getColumn().setBlockAt(x, y, z, type);
			return;
		}
		
		if(this.empty) return;
		this.blocks.set(x, y, z, type.getId(), type.getData());
		this.column.updateHeightMap(x - 1, z - 1, x + 1, z + 1);
	}
	
	public int blockIndex(int x, int y, int z) {
		return this.blocks.index(x, y, z);
	}
	
	public BlockStore getBlockStore() {
		return this.blocks;
	}
	
	public void setStore(byte blocks[], byte data[]) {
		this.blocks.setBlocks(blocks);
		this.blocks.setData(data);
		this.column.updateHeightMap(this.getWorldX(), this.getWorldZ(), this.getWorldX() + Constants.CHUNK_WIDTH - 1, this.getWorldZ() + Constants.CHUNK_DEPTH - 1);
	}
	
	public void generated() {
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
			this.list = glGenLists(2);
		}
		
		this.empty = true;
		for(int pass = 0; pass < 2; pass++) {
			glNewList(this.list + pass, GL_COMPILE);
			for(int x = this.getWorldX(); x < this.getWorldX() + Constants.CHUNK_WIDTH; x++) {
				for(int y = this.getWorldY(); y < this.getWorldY() + Constants.CHUNK_HEIGHT; y++) {
					for(int z = this.getWorldZ(); z < this.getWorldZ() + Constants.CHUNK_DEPTH; z++) {
						BlockType type = Blocks.get(this.getBlockAt(x, y, z), this.getData(x, y, z));
						if(pass == 0 && !type.isOpaque()) continue;
						if(pass == 1 && type.isOpaque()) continue;
						if(!(type.getModel() instanceof EmptyModel) && type.getModel().render(type, x, y, z, this.column.getBrightness(x, y, z))) {
							this.empty = false;
						}
					}
				}
			}
			
			glEndList();
		}
	}
	
	public void dispose() {
		if(this.list > -1) {
			glDeleteLists(this.list, 2);
			this.list = -1;
		}
	}
	
	public void update() {
		// TODO
	}
	
	@Override
	public Biome getBiome(int x, int y, int z) {
		return this.getColumn().getLevel().getBiome(x, y, z);
	}
	
	@Override
	public String toString() {
		return "Chunk{x=" + this.column.getX() + ",y=" + this.y + ",z=" + this.column.getZ() + "}";
	}
	
}
