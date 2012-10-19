package ch.spacebase.openclassic.game.level.column;

import static org.lwjgl.opengl.GL11.GL_COMPILE;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.lwjgl.opengl.GL11.glDeleteLists;

import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.model.EmptyModel;
import ch.spacebase.openclassic.api.level.column.Chunk;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.api.util.storage.TripleIntByteArray;
import ch.spacebase.openclassic.api.util.storage.TripleIntNibbleArray;
import ch.spacebase.openclassic.game.level.LightType;

public class ClassicChunk implements Chunk {
	
	private ClassicColumn column;
	private TripleIntByteArray blocks = new TripleIntByteArray(Constants.CHUNK_WIDTH, Constants.CHUNK_HEIGHT, Constants.CHUNK_DEPTH);
	private TripleIntNibbleArray blocklight = new TripleIntNibbleArray(Constants.CHUNK_WIDTH, Constants.CHUNK_HEIGHT, Constants.CHUNK_DEPTH);
	private TripleIntNibbleArray skylight = new TripleIntNibbleArray(Constants.CHUNK_WIDTH, Constants.CHUNK_HEIGHT, Constants.CHUNK_DEPTH);
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
		this.refreshLight();
	}
	
	private void clearLight() {
		this.skylight.fill(0);
		this.blocklight.fill(0);
	}
	
	private void light(int x, int y, int z, BlockType type) {
		boolean sky = true;
		boolean block = true;
		if(this.getLightLevel(x, y + 1, z, LightType.SKY) == 15) {
			this.setLightLevel(x, y, z, LightType.SKY, 15);
			sky = false;
		}
		
		if(type.getLightLevel() > 0) {
			this.setLightLevel(x, y, z, LightType.BLOCK, type.getLightLevel());
			block = false;
		}
		
		if(sky || block) {
			int skylight = 0;
			int blocklight = 0;
			for(BlockFace face : BlockFace.values()) {
				if(sky) {
					int light = this.getLightLevel(x + face.getModX(), y + face.getModY(), z + face.getModZ(), LightType.SKY);
					if(light > 0 && light - 1 > skylight) {
						skylight = light - 1;
					}
				}
				
				if(block) {
					int light = this.getLightLevel(x + face.getModX(), y + face.getModY(), z + face.getModZ(), LightType.BLOCK);
					if(light > 0 && light - 1 > blocklight) {
						blocklight = light - 1;
					}
				}
			}
			
			if(sky) this.setLightLevel(x, y, z, LightType.SKY, skylight);
			if(block) this.setLightLevel(x, y, z, LightType.BLOCK, blocklight);
		}
	}
	
	public void refreshLight() {
		this.clearLight();
		for(int y = this.getWorldY() + Constants.CHUNK_HEIGHT - 1; y >= this.getWorldY(); y--) {
			for(int x = this.getWorldX(); x < this.getWorldX() + Constants.CHUNK_WIDTH; x++) {
				for(int z = this.getWorldZ(); z < this.getWorldZ() + Constants.CHUNK_DEPTH; z++) {
					BlockType type = Blocks.fromId(this.getBlockAt(x, y, z));
					if(!type.isOpaque() || type.getLightLevel() != 0) {
						this.light(x, y, z, type);
						if(this.getLightLevel(x, y, z) > 0) {
							this.updateNeighborLight(x, y, z, this.getLightLevel(x, y, z));
						}
					}
				}
			}
		}
	}
	
	private void updateNeighborLight(int x, int y, int z, int light) {
		for(int xx = x - light; xx <= x + light; xx++) {
			if(xx < this.getWorldX()) x = this.getWorldX();
			if(xx >= this.getWorldX() + Constants.CHUNK_WIDTH) return;
			BlockType t = Blocks.fromId(this.getBlockAt(xx, y, z));
			if(!t.isOpaque() || t.getLightLevel() != 0) {
				this.light(xx, y, z, t);
			}
		}
	}
	
	public int getLightLevel(int x, int y, int z) {
		return Math.max(this.getLightLevel(x, y, z, LightType.SKY), this.getLightLevel(x, y, z, LightType.BLOCK));
	}
	
	public int getLightLevel(int x, int y, int z, LightType type) {
		if(x < this.getWorldX() || y < this.getWorldY() || z < this.getWorldZ() || x >= this.getWorldX() + Constants.CHUNK_WIDTH || y >= this.getWorldY() + Constants.CHUNK_HEIGHT || z >= this.getWorldZ() + Constants.CHUNK_DEPTH)
			return this.getColumn().getLightLevel(x, y, z, type);
		
		if(type == LightType.SKY) {
			return this.skylight.get(x - this.getWorldX(), y - this.getWorldY(), z - this.getWorldZ());
		} else {
			return this.blocklight.get(x - this.getWorldX(), y - this.getWorldY(), z - this.getWorldZ());
		}
	}
	
	public void setLightLevel(int x, int y, int z, LightType type, int light) {
		if(x < this.getWorldX() || y < this.getWorldY() || z < this.getWorldZ() || x >= this.getWorldX() + Constants.CHUNK_WIDTH || y >= this.getWorldY() + Constants.CHUNK_HEIGHT || z >= this.getWorldZ() + Constants.CHUNK_DEPTH)
			return;
		
		if(type == LightType.SKY) {
			this.skylight.set(x - this.getWorldX(), y - this.getWorldY(), z - this.getWorldZ(), light);
		} else {
			this.blocklight.set(x - this.getWorldX(), y - this.getWorldY(), z - this.getWorldZ(), light);
		}
	}
	
	public int blockIndex(int x, int y, int z) {
		return this.blocks.toIndex(x, y, z);
	}
	
	public byte[] getBlocks() {
		return this.blocks.get();
	}
	
	public byte[] getSkyLight() {
		return this.skylight.get();
	}
	
	public byte[] getBlockLight() {
		return this.blocklight.get();
	}
	
	public void setBlocks(byte blocks[], boolean calc) {
		this.blocks.set(blocks);
		// TODO: slows loading a lot.
		//if(calc) this.refreshLight();
	}
	
	public void setLight(byte sky[], byte block[]) {
		this.skylight.set(sky);
		this.blocklight.set(block);
	}
	
	public float getBrightness(int x, int y, int z) {
		return Math.max(this.getLightLevel(x, y, z) / 15f, 0.1f);
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
