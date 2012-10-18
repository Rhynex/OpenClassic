package ch.spacebase.openclassic.game.level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.physics.FallingBlockPhysics;
import ch.spacebase.openclassic.api.block.physics.FlowerPhysics;
import ch.spacebase.openclassic.api.block.physics.GrassPhysics;
import ch.spacebase.openclassic.api.block.physics.LiquidPhysics;
import ch.spacebase.openclassic.api.block.physics.MushroomPhysics;
import ch.spacebase.openclassic.api.block.physics.SaplingPhysics;
import ch.spacebase.openclassic.api.block.physics.SpongePhysics;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.entity.BlockEntity;
import ch.spacebase.openclassic.api.event.block.BlockPhysicsEvent;
import ch.spacebase.openclassic.api.event.entity.EntityDeathEvent;
import ch.spacebase.openclassic.api.event.level.SpawnChangeEvent;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.api.network.msg.BlockChangeMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerDespawnMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.api.util.map.TripleIntHashMap;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.game.level.column.ClassicColumn;
import ch.spacebase.openclassic.game.level.column.ColumnManager;
import ch.spacebase.openclassic.game.level.io.LevelFormat;
import ch.spacebase.openclassic.game.level.io.OpenClassicLevelFormat;

public abstract class ClassicLevel implements Level {

	private static final Random rand = new Random();
	
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
	private List<BlockEntity> entities = new ArrayList<BlockEntity>();
	
	private LevelFormat format; // TODO: changeable
	private ColumnManager columns = new ColumnManager(this);
	private NBTData data;

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
		
		this.spawn = info.getSpawn() != null ? info.getSpawn() : info.getGenerator().findSpawn(this);
		if(this.spawn != null) this.spawn.setLevel(this);
		
		this.format = new OpenClassicLevelFormat(new File(OpenClassic.getClient().getDirectory(), "levels/" + this.name));
        this.format.setLevel(this);
		try {
			this.format.saveData();
		} catch (IOException e) {
			OpenClassic.getLogger().severe("Failed to save data for level " + this.name + "!");
			e.printStackTrace();
		}
		
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
	
	public void dispose() {
		this.executor.shutdown();
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
		ClassicColumn col = this.getColumnFromBlock(x, z);
		return col.getBlockAt(x, y, z);
	}
	
	public BlockType getBlockTypeAt(Position pos) {
		return this.getBlockTypeAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}
	
	public BlockType getBlockTypeAt(int x, int y, int z) {
		int type = this.getBlockIdAt(x, y, z);
		return Blocks.fromId(type >= 0 ? type : 0);
	}
	
	public Block getBlockAt(Position pos) {
		return new Block(pos);
	}
	
	public Block getBlockAt(int x, int y, int z) {
		return this.getBlockAt(new Position(this, x, y, z));
	}
	
	public boolean setBlockIdAt(Position pos, byte type) {
		return this.setBlockIdAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), type);
	}
	
	public boolean setBlockIdAt(Position pos, byte type, boolean physics) {
		return this.setBlockIdAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), type, physics);
	}
	
	public boolean setBlockIdAt(int x, int y, int z, byte type) {
		return this.setBlockIdAt(x, y, z, type, true);
	}
	
	public boolean setBlockIdAt(int x, int y, int z, byte type, boolean physics) {
		this.getColumnFromBlock(x, z).setBlockAt(x, y, z, type);
		this.sendToAll(new BlockChangeMessage((short) x, (short) y, (short) z, type));
		
		if(physics) {
			for(BlockFace face : BlockFace.values()) {
				Block block = this.getBlockAt(x + face.getModX(), y + face.getModY(), z + face.getModZ());
				if(block != null && block.getType() != null && block.getType().getPhysics() != null) {
					block.getType().getPhysics().onNeighborChange(block, this.getBlockAt(x, y, z));
				}
			}
		}
		
		return true;
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
		return this.setBlockIdAt(x, y, z, type.getId(), physics);
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
	
	public List<BlockEntity> getBlockEntities() {
		return new ArrayList<BlockEntity>(this.entities);
	}
	
	public BlockEntity getBlockEntityFromId(int id) {
		for(BlockEntity entity : this.entities) {
			if(entity.getEntityId() == id) return entity;
		}
		
		return null;
	}
	
	public BlockEntity getBlockEntity(Position pos) {
		for(BlockEntity entity : this.entities) {
			if(entity.getPosition().equals(pos)) return entity;
		}
		
		return null;
	}
	
	public BlockEntity spawnBlockEntity(BlockEntity entity, Position pos) {
		this.entities.add(entity);
		entity.setPosition(pos);
		
		return entity;
	}
	
	public void removeBlockEntity(BlockEntity entity) {
		this.removeBlockEntity(entity.getEntityId());
	}
	
	public void removeBlockEntity(int id) {
		for(BlockEntity entity : this.entities) {
			if(entity.getEntityId() == id) {
				if(entity.getController() != null) entity.getController().onDeath();
				OpenClassic.getGame().getEventManager().dispatch(new EntityDeathEvent(entity));
				this.entities.remove(entity);
			}
		}
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
	public void delayTick(Position pos, byte id) {
		this.updatePhysics(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}
	
	public boolean growTree(int x, int y, int z) {
		int logHeight = rand.nextInt(3) + 4;
		boolean freespace = true;

		for (int currY = y; currY <= y + 1 + logHeight; currY++) {
			byte leaf = 1;
			if (currY == y) {
				leaf = 0;
			}

			if (currY >= y + 1 + logHeight - 2) {
				leaf = 2;
			}

			for (int currX = x - leaf; currX <= x + leaf && freespace; ++currX) {
				for (int currZ = z - leaf; currZ <= z + leaf && freespace; ++currZ) {
					if (this.getBlockTypeAt(currX, currY, currZ) != VanillaBlock.AIR) {
						freespace = false;
					}
				}
			}
		}

		if (!freespace) {
			return false;
		} else if (this.getBlockTypeAt(x, y, z) == VanillaBlock.GRASS && y < Constants.COLUMN_HEIGHT - logHeight - 1) {
			this.setBlockAt(x, y - 1, z, VanillaBlock.DIRT);
			for (int count = y - 3 + logHeight; count <= y + logHeight; ++count) {
				int var8 = count - (y + logHeight);
				int leafMax = 1 - var8 / 2;

				for (int currX = x - leafMax; currX <= x + leafMax; ++currX) {
					int diffX = currX - x;

					for (int currZ = z - leafMax; currZ <= z + leafMax; ++currZ) {
						int diffZ = currZ - z;
						if (Math.abs(diffX) != leafMax || Math.abs(diffZ) != leafMax || rand.nextInt(2) != 0 && var8 != 0) {
							this.setBlockAt(currX, count, currZ, VanillaBlock.LEAVES);
						}
					}
				}
			}

			for (int count = 0; count < logHeight; ++count) {
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
		return this.columns.getColumn(x, z);
	}
	
	public ClassicColumn getColumnFromBlock(int x, int z) {
		return this.getColumn(x >> 4, z >> 4);
	}
	
	public boolean isColumnLoaded(int x, int z) {
		return this.columns.isColumnLoaded(x, z);
	}

	public List<ClassicColumn> getColumns() {
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
		
		for(ClassicColumn column : this.columns.getAll()) {
			column.save();
		}
	}
	
}
