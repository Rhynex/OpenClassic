package ch.spacebase.openclassic.game.level.column;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.level.column.Column;
import ch.spacebase.openclassic.api.level.generator.biome.BiomeManager;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.game.level.ClassicLevel;

public class ClassicColumn implements Column {

	private ClassicLevel level;
	private int x;
	private int z;
	private ClassicChunk chunks[] = new ClassicChunk[Constants.COLUMN_HEIGHT >> 4];
	private BiomeManager biomes;
	private int heightmap[] = new int[Constants.CHUNK_AREA];
	
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
		if(x < this.getWorldX() || z < this.getWorldZ() || x >= this.getWorldX() + Constants.CHUNK_WIDTH || z >= this.getWorldZ() + Constants.CHUNK_DEPTH) {
			if(this.level.isColumnLoaded(x >> 4, z >> 4)) {
				return this.level.getBlockIdAt(x, y, z);
			}

			return 0;
		}
		
		if(this.getChunkFromBlock(y) == null) return 0;
		return this.getChunkFromBlock(y).getBlockAt(x, y, z);
	}
	
	public byte getData(int x, int y, int z) {
		if(x < this.getWorldX() || z < this.getWorldZ() || x >= this.getWorldX() + Constants.CHUNK_WIDTH || z >= this.getWorldZ() + Constants.CHUNK_DEPTH) {
			if(this.level.isColumnLoaded(x >> 4, z >> 4)) {
				return this.level.getData(x, y, z);
			}

			return 0;
		}
		
		if(this.getChunkFromBlock(y) == null) return 0;
		return this.getChunkFromBlock(y).getData(x, y, z);
	}
	
	public void setBlockAt(int x, int y, int z, BlockType type) {
		if(x < this.getWorldX() || z < this.getWorldZ() || x >= this.getWorldX() + Constants.CHUNK_WIDTH || z >= this.getWorldZ() + Constants.CHUNK_DEPTH) {
			if(this.level.isColumnLoaded(x >> 4, z >> 4)) {
				this.level.setBlockAt(x, y, z, type);
			}
			
			return;
		}
		
		if(this.getChunkFromBlock(y) == null) return;
		this.getChunkFromBlock(y).setBlockAt(x, y, z, type);
	}
	
	public int getHighestOpaque(int x, int z) {
		if(x < this.getWorldX() || z < this.getWorldZ() || x >= this.getWorldX() + Constants.CHUNK_WIDTH || z >= this.getWorldZ() + Constants.CHUNK_DEPTH)
			return -1;
		
		for(int y = Constants.COLUMN_HEIGHT - 1; y >= 0; y--) {
			BlockType type = this.getBlockAt(x, y, z) >= 0 ? Blocks.get(this.getBlockAt(x, y, z), this.getData(x, y, z)) : null;
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
	
	public boolean isLit(int x, int y, int z) {
		return this.heightmap[(x - this.getWorldX()) + (z - this.getWorldZ()) * Constants.CHUNK_WIDTH] < y;
	}
	
	public float getBrightness(int x, int y, int z) {
		BlockType type = Blocks.get(this.getBlockAt(x, y, z), this.getData(x, y, z));
		float brightness = this.isLit(x, y, z) ? 1 : 0.6f;
		return type.getBrightness() > brightness ? type.getBrightness() : brightness;
	}
	
	public void updateHeightMap(int x1, int z1, int x2, int z2) {
		/* TODO: make better
		for(int x = x1; x <= x2; x++) {
			for(int z = z1; z <= z2; z++) {
				if(x < this.getWorldX() || z < this.getWorldZ() || x >= this.getWorldX() + Constants.CHUNK_WIDTH || z >= this.getWorldZ() + Constants.CHUNK_DEPTH) {
					ClassicColumn column = this.level.getColumn(x >> 4, z >> 4, false);
					if(column != null) column.updateHeightMap(x, z, x, z);
					continue;
				}
				
				int current = this.getHighestOpaque(x, z);
				if(current < 0) current = 0;
				this.heightmap[(x - this.getWorldX()) + (z - this.getWorldZ()) * Constants.CHUNK_WIDTH] = current;
			}
		} */
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
	public BiomeManager getBiomeManager() {
		return this.biomes;
	}

	@Override
	public void setBiomeManager(BiomeManager manager) {
		this.biomes = manager;
	}
	
	@Override
	public String toString() {
		return "Column{x=" + this.x + ",z=" + this.z + "}";
	}
	
}
