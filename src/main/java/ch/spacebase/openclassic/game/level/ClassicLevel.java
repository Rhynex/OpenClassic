package ch.spacebase.openclassic.game.level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import ch.spacebase.openclassic.api.Client;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.complex.ComplexBlock;
import ch.spacebase.openclassic.api.block.physics.FallingBlockPhysics;
import ch.spacebase.openclassic.api.block.physics.FlowerPhysics;
import ch.spacebase.openclassic.api.block.physics.GrassPhysics;
import ch.spacebase.openclassic.api.block.physics.LiquidPhysics;
import ch.spacebase.openclassic.api.block.physics.MushroomPhysics;
import ch.spacebase.openclassic.api.block.physics.SaplingPhysics;
import ch.spacebase.openclassic.api.block.physics.SpongePhysics;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.event.block.BlockPhysicsEvent;
import ch.spacebase.openclassic.api.event.level.SpawnChangeEvent;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.level.column.Column;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.api.level.generator.biome.Biome;
import ch.spacebase.openclassic.api.level.generator.biome.BiomeGenerator;
import ch.spacebase.openclassic.api.level.generator.biome.BiomeManager;
import ch.spacebase.openclassic.api.network.msg.BlockChangeMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerDespawnMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.api.util.storage.TripleIntHashMap;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;
import ch.spacebase.openclassic.game.level.column.ClassicColumn;
import ch.spacebase.openclassic.game.level.column.ColumnManager;
import ch.spacebase.openclassic.game.level.io.LevelFormat;
import ch.spacebase.openclassic.game.level.io.OpenClassicLevelFormat;

public abstract class ClassicLevel implements Level {

	private Random rand = new Random();
	private long creationTime;
	private Position spawn;
	private String name;
	private String author = "unknown";
	private Generator generator;
	private long seed;
	private int skyColor = 10079487;
	private int fogColor = 16777215;
	private int cloudColor = 16777215;
	private List<Player> players = new ArrayList<Player>();
	protected boolean generating = false;
	
	private LevelFormat format;
	private ColumnManager columns = new ColumnManager(this);
	private NBTData data;
	
	private List<Explosion> explosions = new CopyOnWriteArrayList<Explosion>();
	private boolean physics = OpenClassic.getGame().getConfig().getBoolean("physics.enabled", true);
	private TripleIntHashMap<Integer> physicsQueue = new TripleIntHashMap<Integer>();
	protected final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
    	private int nextId = 0;
    	
    	@Override
		public Thread newThread(Runnable r) {
    		this.nextId++;
			return new Thread(r, (ClassicLevel.this instanceof ClientLevel ? "Client" : "Server") + "-Physics-" + this.nextId);
		}
    });
	
	public ClassicLevel(String name) {
		this(name, null);
	}
	
	public ClassicLevel(String name, LevelFormat format) {
		this(name, format, false);
	}
	
	public ClassicLevel(String name, boolean remote) {
		this(name, null, remote);
	}
	
	public ClassicLevel(String name, LevelFormat format, boolean remote) {
		if(!remote) {
			this.format = format != null ? format : new OpenClassicLevelFormat(new File(OpenClassic.getClient().getDirectory(), "levels/" + name));
	        this.format.setLevel(this);
			try {
				this.format.loadData();
			} catch (IOException e) {
				OpenClassic.getLogger().severe("Failed to load data for level " + name + "!");
				e.printStackTrace();
			}
        
			this.columns.loadColumn(this.spawn.getBlockX() >> 4, this.spawn.getBlockZ() >> 4);
			this.setSpawn(this.getGenerator().adjustSpawn(this));
			this.executor.scheduleAtFixedRate(new Runnable() {
	            public void run() {
	                try {
	                    physics();
	                } catch (Exception e) {
	                    OpenClassic.getLogger().severe("Error while ticking physics: " + e);
	                    e.printStackTrace();
	                }
	            }
	        }, 0, 1000 / Constants.PHYSICS_PER_SECOND, TimeUnit.MILLISECONDS);
		}
	}

	public ClassicLevel(LevelInfo info) {
		this.name = info.getName();
		this.author = "";
		this.creationTime = System.currentTimeMillis();
		this.generator = info.getGenerator();
		this.seed = new Random().nextLong();
		
		this.spawn = info.getSpawn() != null ? info.getSpawn() : info.getGenerator().findInitialSpawn(this);
		if(this.spawn != null) this.spawn.setLevel(this);
		
		this.format = new OpenClassicLevelFormat(new File(OpenClassic.getClient().getDirectory(), "levels/" + this.name));
        this.format.setLevel(this);
		try {
			this.format.saveData();
		} catch (IOException e) {
			OpenClassic.getLogger().severe("Failed to save data for level " + this.name + "!");
			e.printStackTrace();
		}
		
		this.columns.loadColumn(this.spawn.getBlockX() >> 4, this.spawn.getBlockZ() >> 4);
		this.setSpawn(this.getGenerator().adjustSpawn(this));
		
		this.data = new NBTData(this.name);
		this.data.load(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + this.name + "/data.nbt");
        this.executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    physics();
                } catch (Exception e) {
                    OpenClassic.getLogger().severe("Error while ticking physics: " + e);
                    e.printStackTrace();
                }
            }
        }, 0, 1000 / Constants.PHYSICS_PER_SECOND, TimeUnit.MILLISECONDS);
	}
	
	public void update() {
		this.columns.update();
		List<Explosion> remove = new ArrayList<Explosion>();
		for(Explosion e : this.explosions) {
			e.decrementTime();
			if(e.getTime() <= 0) {
				this.explode(e.getX(), e.getY(), e.getZ(), e.getPower());
				remove.add(e);
			}
		}
		
		this.explosions.removeAll(remove);
	}
	
	public abstract void render(float delta);
	
	public boolean getPhysicsEnabled() {
		return this.physics;
	}
	
	public void setPhysicsEnabled(boolean enabled) {
		this.physics = enabled;
	}
	
	public void addPlayer(Player player) {
		this.players.add(player);
	}
	
	public void removePlayer(String name) {
		for(Player player : this.getPlayers()) {
			if(player.getName().equalsIgnoreCase(name)) {
				this.players.remove(player);
				this.sendToAllExcept(player, new PlayerDespawnMessage(player.getPlayerId()));
			}
		}
	}
	
	public void removePlayer(byte id) {
		for(Player player : this.getPlayers()) {
			if(player.getPlayerId() == id) {
				this.players.remove(player);
				this.sendToAllExcept(player, new PlayerDespawnMessage(player.getPlayerId()));
			}
		}
	}
	
	public List<Player> getPlayers() {
		return new ArrayList<Player>(this.players);
	}
	
	// TODO: Idle physics like grass, flower, etc
	public void physics() {
		if(this.physics) {
			String updates[];
			Integer ticks[];
			synchronized(this.physicsQueue) {
				updates = this.physicsQueue.keySet().toArray(new String[this.physicsQueue.size()]);
				ticks = this.physicsQueue.values().toArray(new Integer[this.physicsQueue.size()]);
				this.physicsQueue.clear();
			}
			
			for (int count = 0; count < updates.length; count++) {
				int x = TripleIntHashMap.key1(updates[count]);
				int y = TripleIntHashMap.key2(updates[count]);
				int z = TripleIntHashMap.key3(updates[count]);
				
				Block block = this.getBlockAt(x, y, z);
				int tick = ticks[count];
				tick--;
				if(tick > 0) {
					synchronized(this.physicsQueue) {
						this.physicsQueue.put(x, y, z, tick);
					}
					
					continue;
				}
				
				if(physicsAllowed(block)) {
					block.getType().getPhysics().update(block);
				}
			}
		}
	}
	
	public void clearPhysics() {
		synchronized(this.physicsQueue) {
			this.physicsQueue.clear();
		}
	}
	
	public boolean isLit(int x, int y, int z) {
		ClassicColumn column = this.getColumn(x >> 4, z >> 4, false);
		return column != null ? column.isLit(x, y, z) : false;
	}
	
	public float getBrightness(int x, int y, int z) {
		ClassicColumn column = this.getColumn(x >> 4, z >> 4, false);
		return column != null ? column.getBrightness(x, y, z) : 0.6f;
	}
	
	public void dispose() {
		this.executor.shutdown();
		this.columns.dispose();
	}
	
	public void updatePhysics(int x, int y, int z) {
		if(!this.physics) return;
		
		synchronized(this.physicsQueue) {
			this.physicsQueue.put(x, y, z, this.getBlockTypeAt(x, y, z).getTickDelay());
		}
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if(this.name != null && !this.name.equals("")) return;
		
		this.name = name;
		this.data = new NBTData(this.name);
		this.data.load(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + this.name + ".nbt");
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		if(this.author != null && !this.author.equals("")) return;
		
		this.author = author;
	}

	public long getCreationTime() {
		return this.creationTime;
	}

	public void setCreationTime(long time) {
		if(this.creationTime != 0) return;
		
		this.creationTime = time;
	}

	public Position getSpawn() {
		return this.spawn;
	}

	public void setSpawn(Position spawn) {
		Position old = this.spawn;
		this.spawn = spawn;
		OpenClassic.getGame().getEventManager().dispatch(new SpawnChangeEvent(this, old));
	}

	public byte getBlockIdAt(Position pos) {
		return this.getBlockIdAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}
	
	public byte getBlockIdAt(int x, int y, int z) {
		ClassicColumn col = this.getColumn(x >> 4, z >> 4, !this.generating);
		if(col == null) return 0;
		return col.getBlockAt(x, y, z);
	}
	
	public BlockType getBlockTypeAt(Position pos) {
		return this.getBlockTypeAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}
	
	public BlockType getBlockTypeAt(int x, int y, int z) {
		ClassicColumn col = this.getColumn(x >> 4, z >> 4, !this.generating);
		if(col == null) return VanillaBlock.AIR;
		byte type = col.getBlockAt(x, y, z);
		byte data = col.getData(x, y, z);
		return Blocks.get(type >= 0 ? type : 0, data >= 0 ? data : 0);
	}
	
	public Block getBlockAt(Position pos) {
		return new Block(pos);
	}
	
	public Block getBlockAt(int x, int y, int z) {
		return this.getBlockAt(new Position(this, x, y, z));
	}
	
	public byte getData(Position pos) {
		return this.getData(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}
	
	public byte getData(int x, int y, int z) {
		ClassicColumn col = this.getColumn(x >> 4, z >> 4, !this.generating);
		if(col == null) return 0;
		return col.getData(x, y, z);
	}
	
	public boolean setBlockAt(Position pos, BlockType type) {
		return this.setBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), type);
	}
	
	public boolean setBlockAt(Position pos, BlockType type, boolean physics) {
		return this.setBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), type, physics);
	}
	
	public boolean setBlockAt(int x, int y, int z, BlockType type) {
		return this.setBlockAt(x, y, z, type, true);
	}
	
	public boolean setBlockAt(int x, int y, int z, BlockType type, boolean physics) {
		ClassicColumn column = this.getColumn(x >> 4, z >> 4, !this.generating);
		if(column == null) return false;
		column.setBlockAt(x, y, z, type);
		this.sendToAll(new BlockChangeMessage((short) x, (short) y, (short) z, type.getId())); // TODO: adjust for data
		
		if(physics) {
			Block b = this.getBlockAt(x, y, z);
			for(BlockFace face : BlockFace.values()) {
				Block block = this.getBlockAt(x + face.getModX(), y + face.getModY(), z + face.getModZ());
				if(this.isColumnLoaded((x + face.getModX()) >> 4, (z + face.getModZ()) >> 4) && block.getType() != null && block.getType().getPhysics() != null) {
					block.getType().getPhysics().onNeighborChange(block, b);
				}
			}
		}
		
		return true;
	}
	
	@Override
	public int getHighestBlockY(int x, int z) {
		return this.getHighestBlockY(x, z, Constants.COLUMN_HEIGHT);
	}
	
	@Override
	public int getHighestBlockY(int x, int z, int max) {
		for(int y = max; y >= 0; y--) {
			if(this.getBlockIdAt(x, y, z) != 0) return y;
		}
		
		return -1;
	}

	@Override
	public boolean isHighest(int x, int y, int z) {
		if(this.getHighestBlockY(x, z) <= y) return true;
		return false;
	}
	
	private static boolean physicsAllowed(Block block) {
		if(block.getType().getPhysics() == null) return false;
		
		BlockPhysicsEvent event = OpenClassic.getGame().getEventManager().dispatch(new BlockPhysicsEvent(block));
		if(block.getType().getPhysics() instanceof FallingBlockPhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.falling", true) && !event.isCancelled();
		}
		
		if(block.getType().getPhysics() instanceof FlowerPhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.flower", true) && !event.isCancelled();
		}
		
		if(block.getType().getPhysics() instanceof MushroomPhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.mushroom", true) && !event.isCancelled();
		}
		
		if(block.getType().getPhysics() instanceof SaplingPhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.trees", true) && !event.isCancelled();
		}
	
		if(block.getType().getPhysics() instanceof SpongePhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.sponge", true) && !event.isCancelled();
		}
		
		if(block.getType().getPhysics() instanceof LiquidPhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.liquid", true) && !event.isCancelled();
		}
		
		if(block.getType().getPhysics() instanceof GrassPhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.grass", true) && !event.isCancelled();
		}
		
		return true;
	}

	@Override
	public void delayTick(Position pos, BlockType type) {
		this.updatePhysics(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}
	
	public boolean growTree(int x, int y, int z) {
		int logHeight = this.rand.nextInt(3) + 4;
		boolean freespace = true;

		for(int currY = y; currY <= y + 1 + logHeight; currY++) {
			byte leaf = 1;
			if(currY == y) {
				leaf = 0;
			}

			if(currY >= y + 1 + logHeight - 2) {
				leaf = 2;
			}

			for(int currX = x - leaf; currX <= x + leaf && freespace; currX++) {
				for(int currZ = z - leaf; currZ <= z + leaf && freespace; currZ++) {
					if(this.getBlockTypeAt(currX, currY, currZ) != VanillaBlock.AIR) {
						freespace = false;
					}
				}
			}
		}

		if(!freespace) {
			return false;
		} else if(this.getBlockTypeAt(x, y - 1, z) == VanillaBlock.GRASS && y < Constants.COLUMN_HEIGHT - logHeight - 1) {
			this.setBlockAt(x, y - 1, z, VanillaBlock.DIRT);
			for(int count = y - 3 + logHeight; count <= y + logHeight; count++) {
				int var8 = count - (y + logHeight);
				int leafMax = 1 - var8 / 2;

				for(int currX = x - leafMax; currX <= x + leafMax; currX++) {
					int diffX = currX - x;

					for(int currZ = z - leafMax; currZ <= z + leafMax; currZ++) {
						int diffZ = currZ - z;
						if(Math.abs(diffX) != leafMax || Math.abs(diffZ) != leafMax || this.rand.nextInt(2) != 0 && var8 != 0) {
							this.setBlockAt(currX, count, currZ, VanillaBlock.LEAVES);
						}
					}
				}
			}

			for(int count = 0; count < logHeight; count++) {
				this.setBlockAt(x, y + count, z, VanillaBlock.LOG);
			}

			return true;
		} else {
			return false;
		}
	}
	
	public NBTData getData() {
		return this.data;
	}

	@Override
	public int getSkyColor() {
		return this.skyColor;
	}
	
	@Override
	public int getFogColor() {
		return this.fogColor;
	}
	
	@Override
	public int getCloudColor() {
		return this.cloudColor;
	}
	
	@Override
	public void setSkyColor(int color) {
		this.skyColor = color;
	}

	@Override
	public void setFogColor(int color) {
		this.fogColor = color;
	}

	@Override
	public void setCloudColor(int color) {
		this.cloudColor = color;
	}
	
	public ClassicColumn getColumn(int x, int z) {
		return this.columns.getColumn(x, z);
	}
	
	public ClassicColumn getColumn(int x, int z, boolean load) {
		if(!load && !this.isColumnLoaded(x, z)) return null;
		return this.columns.getColumn(x, z);
	}
	
	public ClassicColumn getColumnFromBlock(int x, int z) {
		return this.getColumn(x >> 4, z >> 4);
	}
	
	public boolean isColumnLoaded(int x, int z) {
		return this.columns.isColumnLoaded(x, z);
	}

	public List<Column> getColumns() {
		return this.columns.getAll();
	}
	
	public LevelFormat getFormat() {
		return this.format;
	}
	
	public Generator getGenerator() {
		return this.generator;
	}
	
	public void setGenerator(Generator gen) {
		this.generator = gen;
	}
	
	public long getSeed() {
		return this.seed;
	}
	
	public void setSeed(long seed) {
		this.seed = seed;
	}
	
	public void save() {
		try {
			this.format.saveData();
		} catch (IOException e) {
			OpenClassic.getLogger().severe("Failed to save data for level " + this.name + "!");
			e.printStackTrace();
		}
		
		int count = 0;
		int total = this.columns.getAll().size();
		for(Column column : this.columns.getAll()) {
			column.save();
			count++;
			OpenClassic.getClient().getProgressBar().setProgress((int) (count / (float) total * 100));
		}
	}
	
	public void explode(int x, int y, int z, int power) {
		if(OpenClassic.getGame() instanceof Client) {
			OpenClassic.getClient().getAudioManager().playSound("generic.explode", x, y, z, 1, 1);
		}
		
		this.setBlockAt(x, y, z, VanillaBlock.AIR);
		int x1 = x - power - 1;
		int x2 = x + power + 1;
		int y1 = y - power - 1;
		int y2 = y + power + 1;
		int z1 = z - power - 1;
		int z2 = z + power + 1;

		for(int xx = x1; xx < x2; xx++) {
			for(int yy = y2 - 1; yy >= y1; yy--) {
				for(int zz = z1; zz < z2; zz++) {
					float distx = xx + 0.5F - x;
					float disty = yy + 0.5F - y;
					float distz = zz + 0.5F - z;
					BlockType block = this.getBlockTypeAt(xx, yy, zz);
					if(block != null && block != VanillaBlock.AIR && block != VanillaBlock.BEDROCK && block != VanillaBlock.OBSIDIAN && distx * distx + disty * disty + distz * distz < power * power) {
						if(block == VanillaBlock.TNT) {
							boolean found = false;
							for(Explosion e : this.explosions) {
								if(e.getX() == xx && e.getY() == yy && e.getZ() == zz) {
									found = true;
								}
							}
							
							if(!found) {
								this.explosions.add(new Explosion(xx, yy, zz, 4));
							}
							
							continue;
						}
						
						this.setBlockAt(xx, yy, zz, VanillaBlock.AIR);
						if(this instanceof ClientLevel && this.rand.nextInt(50) < 10) {
							ClientRenderHelper.getHelper().spawnDestructionParticles(block, (ClientLevel) this, new Position(this, xx, yy, zz));
						}
					}
				}
			}
		}
	}

	public void setGenerating(boolean gen) {
		this.generating = gen;
	}
	
	public Biome getBiome(int x, int y, int z) {
		if (y < 0 || y > Constants.COLUMN_HEIGHT) return null;
		if (!(this.generator instanceof BiomeGenerator)) return null;
		ClassicColumn column = this.getColumn(x >> 4, z >> 4, true);
		BiomeManager manager = column.getBiomeManager();
		if(manager != null) {
			Biome biome = column.getBiomeManager().getBiome(x & 0xf, y & 0xf, z & 0xf);
			if(biome != null) {
				return biome;
			}
		}
		
		return ((BiomeGenerator) this.generator).getBiome(x, y, z, this.seed);
	}
	
	public BiomeManager getBiomeManager(int x, int z) {
		return this.getBiomeManager(x, z, false);
	}
	
	public BiomeManager getBiomeManager(int x, int z, boolean load) {
		ClassicColumn column = this.getColumn(x >> 4, z >> 4, load);
		return column != null ? column.getBiomeManager() : null;
	}
	
	public boolean isComplex(Position pos) {
		return this.isComplex(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}

	public boolean isComplex(int x, int y, int z) {
		ClassicColumn col = this.getColumn(x >> 4, z >> 4, !this.generating);
		if(col == null) return false;
		return col.isComplex(x, y, z);
	}

	public ComplexBlock getComplexBlock(Position pos) {
		return this.getComplexBlock(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}

	public ComplexBlock getComplexBlock(int x, int y, int z) {
		ClassicColumn col = this.getColumn(x >> 4, z >> 4, !this.generating);
		if(col == null) return null;
		return col.getComplexBlock(x, y, z);
	}

	public void setComplexBlock(Position pos, ComplexBlock complex) {
		this.setComplexBlock(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), complex);
	}

	public void setComplexBlock(int x, int y, int z, ComplexBlock complex) {
		ClassicColumn col = this.getColumn(x >> 4, z >> 4, !this.generating);
		if(col == null) return;
		col.setComplexBlock(x, y, z, complex);
	}
	
}
