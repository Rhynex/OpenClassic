package ch.spacebase.openclassic.game.level.column;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.level.generator.Populator;
import ch.spacebase.openclassic.api.level.generator.biome.BiomeGenerator;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.api.util.storage.DoubleIntHashMap;
import ch.spacebase.openclassic.api.util.storage.TripleIntByteArray;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.game.level.ClassicLevel;

public class ColumnManager { // TODO: finish up inf worlds, brooler lighting, data values for states (ex. water), finite liquid physics

	private static final int TINY_CHUNKS = 7;
	private static final int SHORT_CHUNKS = 11;
	private static final int NORMAL_CHUNKS = 19;
	private static final int FAR_CHUNKS = 27;
	
	private final DoubleIntHashMap<ClassicColumn> loaded = new DoubleIntHashMap<ClassicColumn>();
	private final DoubleIntHashMap<ClassicColumn> unloadQueue = new DoubleIntHashMap<ClassicColumn>();
	
	private final ClassicLevel level;
	
	public ColumnManager(ClassicLevel level) {
		this.level = level;
	}
	
	public void update() {
		// process unload queue
		if(this.unloadQueue.size() > 0) {
			Collection<ClassicColumn> queue = this.unloadQueue.values();
			int count = 0;
			for(ClassicColumn column : queue) {
				if(count == 6) break;
				column.save();
				column.dispose();
				this.unloadQueue.remove(column.getX(), column.getZ());
				count++;
			}
		}
		
		// unload out of range columns, update in range columns
		Player player = OpenClassic.getClient().getPlayer();
		int dist = getChunkDistance();
		for(ClassicColumn column : this.loaded.values()) {
			int coldist = distanceSquared(column.getX(), column.getZ(), player.getPosition().getBlockX() >> 4, player.getPosition().getBlockZ() >> 4);
			if(coldist > dist) {
				this.unloadColumn(column.getX(), column.getZ());
				continue;
			}
			
			column.update();
		}
		
		// load new in range columns
		int count = 0;
		int loaddist = (int) Math.sqrt(dist);
		for(int x = (player.getPosition().getBlockX() >> 4) - loaddist; x < (player.getPosition().getBlockX() >> 4) + loaddist; x++) {
			if(count > 5) break;
			for(int z = (player.getPosition().getBlockZ() >> 4) - loaddist; z < (player.getPosition().getBlockZ() >> 4) + loaddist; z++) {
				if(count > 5) break;
				int coldist = distanceSquared(x, z, player.getPosition().getBlockX() >> 4, player.getPosition().getBlockZ() >> 4);
				if(coldist <= dist && !this.isColumnLoaded(x, z)) {
					this.loadColumn(x, z);
					count++;
				}
			}
		}
	}
	
	public boolean isColumnLoaded(int x, int z) {
		return this.loaded.containsKey(x, z);
	}
	
	public boolean isUnloading(int x, int z) {
		return this.unloadQueue.containsKey(x, z);
	}
	
	public ClassicColumn loadColumn(int x, int z) {
		if(this.isColumnLoaded(x, z)) return this.loaded.get(x, z);
		if(this.isUnloading(x, z)) {
			ClassicColumn column = this.unloadQueue.remove(x, z);
			this.loaded.put(x, z, column);
			return column;
		}
		
		ClassicColumn column = null;
		if(this.level.getFormat().exists(x, z)) {
			try {
				column = this.level.getFormat().load(x, z);
			} catch(IOException e) {
				OpenClassic.getLogger().severe("Failed to load column (" + x + ", " + z + ")");
				e.printStackTrace();
			}
		}
		
		if(column == null) {
			Random rand = new Random();
			rand.setSeed(this.level.getSeed());
			//System.out.println("Generating (" + x + ", " + z + ")");
			this.level.setGenerating(true);
			column = new ClassicColumn(this.level, x, z);
			if(this.level.getGenerator() instanceof BiomeGenerator) {
				BiomeGenerator generator = (BiomeGenerator) this.level.getGenerator();
				column.setBiomeManager(generator.generateBiomes(this.level, x, z));
			}
			
			List<ClassicChunk> chunks = column.getChunks();
			for(int count = chunks.size() - 1; count >= 0; count--) {
				ClassicChunk chunk = chunks.get(count);
				TripleIntByteArray blocks = new TripleIntByteArray(Constants.CHUNK_WIDTH, Constants.CHUNK_HEIGHT, Constants.CHUNK_DEPTH);
				this.level.getGenerator().generate(this.level, chunk.getWorldX(), chunk.getWorldY(), chunk.getWorldZ(), blocks, rand);
				chunk.setBlocks(blocks.get(), true);
			}
			
			this.loaded.put(x, z, column);
			for(ClassicChunk chunk : column.getChunks()) {
				for(Populator pop : this.level.getGenerator().getPopulators(this.level)) {
					pop.populate(this.level, chunk, rand);
				}
			}
			
			this.level.setGenerating(false);	
			//column.save();
		} else {
			this.loaded.put(x, z, column);
		}
		
		if(this.level instanceof ClientLevel && ((ClientLevel) this.level).getRenderer() != null) {
			((ClientLevel) this.level).getRenderer().queue(column);
		}
		
		if(this.level.getSpawn().getBlockX() >= x << 4 && this.level.getSpawn().getBlockX() <= (x << 4) + Constants.CHUNK_WIDTH && this.level.getSpawn().getBlockZ() >= z << 4 && this.level.getSpawn().getBlockZ() <= (z << 4) + Constants.CHUNK_DEPTH) {
			this.level.setSpawn(this.level.getGenerator().adjustSpawn(this.level));
		}
		
		return column;
	}
	
	public void unloadColumn(int x, int z) {
		if(!this.isColumnLoaded(x, z)) return;
		ClassicColumn column = this.loaded.get(x, z);
		this.unloadQueue.put(x, z, column);
		this.loaded.remove(x, z);
		if(this.level instanceof ClientLevel) {
			((ClientLevel) this.level).getRenderer().remove(column);
		}
	}
	
	public ClassicColumn getColumn(int x, int z) {
		if(!this.isColumnLoaded(x, z)) return this.loadColumn(x, z);
		return this.loaded.get(x, z);
	}
	
	public List<ClassicColumn> getAll() {
		List<ClassicColumn> result = new ArrayList<ClassicColumn>();
		result.addAll(this.loaded.values());
		result.removeAll(this.unloadQueue.values());
		return result;
	}
	
	private static int getChunkDistance() {
		switch(OpenClassic.getClient().getConfig().getInteger("options.view-distance")) {
			case 0:
				return FAR_CHUNKS * FAR_CHUNKS;
			case 1:
				return NORMAL_CHUNKS * NORMAL_CHUNKS;
			case 2:
				return SHORT_CHUNKS * SHORT_CHUNKS;
			case 3:
				return TINY_CHUNKS * TINY_CHUNKS;
			default:
				return FAR_CHUNKS * FAR_CHUNKS;
		}
	}
	
    private static int distanceSquared(float x1, float z1, float x2, float z2) {
        return (int) (Math.pow(x1 - x2, 2) + Math.pow(z1 - z2, 2));
    }
	
}
