package ch.spacebase.openclassic.game.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;


import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.level.generator.FlatLandGenerator;
import ch.spacebase.openclassic.api.util.io.IOUtils;
import ch.spacebase.openclassic.game.exception.LevelLoadException;
import ch.spacebase.openclassic.game.level.ClassicLevel;

import ch.spacebase.opennbt.TagBuilder;
import ch.spacebase.opennbt.stream.NBTInputStream;
import ch.spacebase.opennbt.stream.NBTOutputStream;
import ch.spacebase.opennbt.tag.ByteArrayTag;
import ch.spacebase.opennbt.tag.ByteTag;
import ch.spacebase.opennbt.tag.CompoundTag;
import ch.spacebase.opennbt.tag.DoubleTag;
import ch.spacebase.opennbt.tag.LongTag;
import ch.spacebase.opennbt.tag.ShortTag;
import ch.spacebase.opennbt.tag.StringTag;

public class OpenClassicLevelFormat {
	
	public static Level load(String name, Class<? extends ClassicLevel> type, boolean create) throws LevelLoadException {
		if(!(new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".map").exists())) {
			if(new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".mclevel").exists()) {
				OpenClassic.getLogger().info("Minecraft Indev map detected! Reading...");
				return IndevLevelFormat.read("levels/" + name + ".mclevel", type);
			}
			
			if(new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".mine").exists()) {
				OpenClassic.getLogger().info("Minecraft Classic map detected! Reading...");
				return MinecraftLevelFormat.read("levels/" + name + ".mine", type);
			}
			
			if(new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".dat").exists()) {
				OpenClassic.getLogger().info("Minecraft Classic map detected! Reading...");
				return MinecraftLevelFormat.read("levels/" + name + ".dat", type);
			}
			
			if(new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".lvl").exists()) {
				OpenClassic.getLogger().info("MCSharp map detected! Reading...");
				return MinecraftLevelFormat.read("levels/" + name + ".lvl", type);
			}
			
			if(new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".oclvl").exists()) {
				OpenClassic.getLogger().info("Old OpenClassic map detected! Reading...");
				return readOld("levels/" + name + ".oclvl", type);
			}
		}

		try {
			ClassicLevel level = type.newInstance();
			
			File levelFile = new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".map");
			if(!levelFile.exists()) {
				if(create) {
					OpenClassic.getLogger().info("Level \"" + name + "\" was not found. Creating with default settings...");
					return OpenClassic.getGame().createLevel(new LevelInfo(name, new Position(null, 64, 33, 64), (short) 128, (short) 64, (short) 128), new FlatLandGenerator());
				} else {
					return null;
				}
			}
			
			FileInputStream in = new FileInputStream(levelFile);
			NBTInputStream nbt = new NBTInputStream(in);
			CompoundTag root = (CompoundTag) nbt.readTag();
			CompoundTag info = (CompoundTag) root.get("Info");
			CompoundTag spawn = (CompoundTag) root.get("Spawn");
			CompoundTag map = (CompoundTag) root.get("Map");
			
			level.setName(((StringTag) info.get("Name")).getValue());
			level.setAuthor(((StringTag) info.get("Author")).getValue());
			level.setCreationTime(((LongTag) info.get("CreationTime")).getValue());
			
			double spawnX = ((DoubleTag) spawn.get("x")).getValue();
			double spawnY = ((DoubleTag) spawn.get("y")).getValue();
			double spawnZ = ((DoubleTag) spawn.get("z")).getValue();
			byte spawnYaw = ((ByteTag) spawn.get("yaw")).getValue();
			byte spawnPitch = ((ByteTag) spawn.get("pitch")).getValue();
			level.setSpawn(new Position(level, (float) spawnX, (float) spawnY, (float) spawnZ, spawnYaw, spawnPitch));
			
			short width = ((ShortTag) map.get("Width")).getValue();
			short height = ((ShortTag) map.get("Height")).getValue();
			short depth = ((ShortTag) map.get("Depth")).getValue();
			byte blocks[] = ((ByteArrayTag) map.get("Blocks")).getValue();
			level.setWorldData(width, height, depth, blocks);
	
			nbt.close();
			return level;
		} catch(Exception e) {
			throw new LevelLoadException(e);
		}
	}
	
	public static Level readOld(String file, Class<? extends ClassicLevel> type) throws LevelLoadException {
		try {
			ClassicLevel level = type.newInstance();
			
			FileInputStream in = new FileInputStream(new File(OpenClassic.getGame().getDirectory(), file));
			GZIPInputStream gzipIn = new GZIPInputStream(in);
			DataInputStream data = new DataInputStream(gzipIn);
			
			level.setName(IOUtils.readString(data));
			level.setAuthor(IOUtils.readString(data));
			level.setCreationTime(data.readLong());
			
			level.setSpawn(new Position(level, (float) data.readDouble(), (float) data.readDouble(), (float) data.readDouble(), data.readByte(), data.readByte()));
			
			short width = data.readShort();
			short height = data.readShort();
			short depth = data.readShort();
			
			byte[] blocks = new byte[width * depth * height];
			data.read(blocks);
	
			level.setWorldData(width, height, depth, blocks);
			
			data.close();
			return level;
		} catch(Exception e) {
			throw new LevelLoadException(e);
		}
	}
	
	public static void save(Level level) throws IOException {
		FileOutputStream out = new FileOutputStream(new File(OpenClassic.getGame().getDirectory(), "levels/" + level.getName() + ".map"));
		NBTOutputStream nbt = new NBTOutputStream(out);
		
		TagBuilder root = new TagBuilder("Level");
		
		TagBuilder info = new TagBuilder("Info");
		info.append("Name", level.getName());
		info.append("Author", level.getAuthor());
		info.append("CreationTime", level.getCreationTime());
		root.append(info);
		
		TagBuilder spawn = new TagBuilder("Spawn");
		spawn.append("x", (double) level.getSpawn().getX());
		spawn.append("y", (double) level.getSpawn().getY());
		spawn.append("z", (double) level.getSpawn().getZ());
		spawn.append("yaw", (byte) level.getSpawn().getYaw());
		spawn.append("pitch", (byte) level.getSpawn().getPitch());
		root.append(spawn);
		
		TagBuilder map = new TagBuilder("Map");
		map.append("Width", level.getWidth());
		map.append("Height", level.getHeight());
		map.append("Depth", level.getDepth());
		map.append("Blocks", level.getBlocks());
		root.append(map);
		
		nbt.writeTag(root.toCompoundTag());
		nbt.close();
		if(level.getData() != null) level.getData().save(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.getName() + ".nbt");
	}
	
	private OpenClassicLevelFormat() {
	}
	
}
