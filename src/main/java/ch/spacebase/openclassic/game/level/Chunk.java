package ch.spacebase.openclassic.game.level;

import static org.lwjgl.opengl.GL11.GL_COMPILE;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;

import java.util.Arrays;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.EmptyModel;
import ch.spacebase.openclassic.api.level.Level;

public class Chunk {

	private static final int WIDTH = 16;
	private static final int HEIGHT = 16;
	private static final int DEPTH = 16;
	
	private Level level;
	
	private int x;
	private int y;
	private int z;
	private int worldX;
	private int worldY;
	private int worldZ;
	
	private byte blocks[] = new byte[WIDTH * HEIGHT * DEPTH];
	private int blockers[] = new int[WIDTH * DEPTH];
	
	private int list = -1;
	private boolean empty = true;
	
	public Chunk(Level level, int x, int y, int z) {
		this.level = level;
		this.x = x;
		this.y = y;
		this.z = z;
		
		this.worldX = x << 4;
		this.worldY = y << 4;
		this.worldZ = z << 4;
	}
	
	public Level getLevel() {
		return this.level;
	}
	
	public int getChunkX() {
		return this.x;
	}
	
	public int getChunkY() {
		return this.y;
	}
	
	public int getChunkZ() {
		return this.z;
	}
	
	public int getWorldX() {
		return this.worldX;
	}
	
	public int getWorldY() {
		return this.worldY;
	}
	
	public int getWorldZ() {
		return this.worldZ;
	}
	
	public byte getBlockAt(int x, int y, int z) {
		if (x < this.worldX || this.worldY < 0 || z < this.worldZ || x >= this.worldX + WIDTH || y >= this.worldY + HEIGHT || z >= this.worldZ + DEPTH)
			return -1;
		
		return this.blocks[this.coordsToBlockIndex(x, y, z)];
	}
	
	public void setBlockAt(int x, int y, int z, byte id) {
		if (x < this.worldX || this.worldY < 0 || z < this.worldZ || x >= this.worldX + WIDTH || y >= this.worldY + HEIGHT || z >= this.worldZ + DEPTH)
			return;
		
		this.blocks[this.coordsToBlockIndex(x, y, z)] = id;
	}
	
	public boolean isLit(int x, int y, int z) {
		x -= this.worldX;
		y -= this.worldY;
		z -= this.worldZ;
		return y >= this.blockers[x + z * WIDTH];
	}
	
	public void calcLight(int x, int z, int width, int depth) {
		for(int xx = x; xx < x + width; xx++) {
			for(int zz = z; zz < z + depth; zz++) {
				int current = this.getHighestOpaque(xx, zz);
				
				if(current < 0) current = 0;
				this.blockers[(xx - this.worldX) + (zz - this.worldZ) * WIDTH] = current;
			}
		}
	}
	
	public int getHighestOpaque(int x, int z) {
		for(int y = this.worldY + HEIGHT; y >= this.worldY; y--) {
			BlockType type = this.getBlockAt(x, y, z) >= 0 ? Blocks.fromId(this.getBlockAt(x, y, z)) : null;
			if(type != null && type.isOpaque()) return y;
		}
		
		return -1;
	}
	
	public float getBrightness(int x, int y, int z) {
		if (x < this.worldX || this.worldY < 0 || z < this.worldZ || x >= this.worldX + WIDTH || y >= this.worldY + HEIGHT || z >= this.worldZ + DEPTH) return 1;
		BlockType block = Blocks.fromId(this.getBlockAt(x, y, z));
		return block == VanillaBlock.LAVA || block == VanillaBlock.STATIONARY_LAVA ? 100 : this.isLit(x, y, z) ? 1 : 0.6f;
	}
	
	public byte[] getBlocks() {
		return Arrays.copyOf(this.blocks, this.blocks.length);
	}
	
	public void setBlocks(byte blocks[]) {
		this.blocks = blocks;
		this.blockers = new int[WIDTH * DEPTH];
	}
	
	public int coordsToBlockIndex(int x, int y, int z) {
		x -= this.worldX;
		y -= this.worldY;
		z -= this.worldZ;
		if (x < 0 || 0 < 0 || z < 0 || x >=  WIDTH || y >= HEIGHT || z >= DEPTH)
			return -1;

		return x + (z * WIDTH) + (y * WIDTH * DEPTH);
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
		for(int x = this.worldX; x <= this.worldX + WIDTH; x++) {
			for(int y = this.worldY; y <= this.worldY + HEIGHT; y++) {
				for(int z = this.worldZ; z <= this.worldZ + DEPTH; z++) {
					BlockType type = Blocks.fromId(this.getBlockAt(x, y, z));
					if(!(type.getModel() instanceof EmptyModel)) {
						this.empty = false;
						type.getModel().render(x, y, z, this.getBrightness(x, y, z));
					}
				}
			}
		}
		
		glEndList();
	}
	
}
