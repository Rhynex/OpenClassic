package ch.spacebase.openclassic.game.level.column;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.level.column.Column;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.game.level.ClassicLevel;

public class ClassicColumn implements Column {

	private ClassicLevel level;
	private int x;
	private int z;
	private ClassicChunk chunks[] = new ClassicChunk[Constants.COLUMN_HEIGHT >> 4];
	
	public ClassicColumn(ClassicLevel level, int x, int z) {
		this.level = level;
		this.x = x;
		this.z = z;
		for(int index = 0; index < this.chunks.length; index++) {
			this.chunks[index] = new ClassicChunk(this, index);
		}
	}
	
	public ClassicLevel getLevel() {
		return this.level;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getZ() {
		return this.z;
	}
	
	public int getWorldX() {
		return this.x << 4;
	}
	
	public int getWorldZ() {
		return this.z << 4;
	}
	
	public byte getBlockAt(int x, int y, int z) {
		if(this.getChunkFromBlock(y) == null) return 0;
		return this.getChunkFromBlock(y).getBlockAt(x, y, z);
	}
	
	public void setBlockAt(int x, int y, int z, byte id) {
		if(this.getChunkFromBlock(y) == null) return;
		this.getChunkFromBlock(y).setBlockAt(x, y, z, id);
	}
	
	public int getHighestOpaque(int x, int z) {
		for(int y = Constants.COLUMN_HEIGHT - 1; y >= 0; y--) {
			BlockType type = this.getBlockAt(x, y, z) >= 0 ? Blocks.fromId(this.getBlockAt(x, y, z)) : null;
			if(type != null && type.isOpaque()) return y;
		}
		
		return -1;
	}
	
	public ClassicChunk getChunk(int y) {
		if(y < 0 || y >= this.chunks.length) return null;
		return this.chunks[y];
	}
	
	public ClassicChunk getChunkFromBlock(int y) {
		return this.getChunk(y >> 4);
	}
	
	public List<ClassicChunk> getChunks() {
		return new ArrayList<ClassicChunk>(Arrays.asList(this.chunks));
	}
	
	public void save() {
		try {
			this.level.getFormat().save(this);
		} catch (IOException e) {
			OpenClassic.getLogger().severe("Failed to save column (" + this.x + ", " + this.z + ")");
			e.printStackTrace();
		}
	}
	
	public void dispose() {
		for(ClassicChunk chunk : this.chunks) {
			chunk.dispose();
		}
	}
	
	public void update() {
		for(ClassicChunk chunk : this.chunks) {
			chunk.update();
		}
		
		// TODO
	}
	
	@Override
	public String toString() {
		return "Column{x=" + this.x + ",z=" + this.z + "}";
	}
	
}
