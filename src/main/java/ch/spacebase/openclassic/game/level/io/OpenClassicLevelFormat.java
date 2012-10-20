package ch.spacebase.openclassic.game.level.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.game.level.column.ClassicChunk;
import ch.spacebase.openclassic.game.level.column.ClassicColumn;
import ch.spacebase.opennbt.TagBuilder;
import ch.spacebase.opennbt.stream.NBTInputStream;
import ch.spacebase.opennbt.stream.NBTOutputStream;
import ch.spacebase.opennbt.tag.ByteArrayTag;
import ch.spacebase.opennbt.tag.CompoundTag;
import ch.spacebase.opennbt.tag.FloatTag;
import ch.spacebase.opennbt.tag.LongTag;
import ch.spacebase.opennbt.tag.StringTag;

public class OpenClassicLevelFormat extends LevelFormat {

	private File dir;
	
	public OpenClassicLevelFormat(File dir) {
		this.dir = dir;
	}
	
	@Override
	public boolean exists() {
		return this.dir.exists() && this.getDataFile().exists();
	}
	
	@Override
	public boolean exists(int x, int z) {
		return this.getChunkFile(x, z).exists();
	}

	@Override
	public void loadData() throws IOException {
		File file = this.getDataFile();
		if(!file.exists()) return;
		NBTInputStream in = new NBTInputStream(new FileInputStream(file));
		CompoundTag root = (CompoundTag) in.readTag();
		CompoundTag info = (CompoundTag) root.get("Info");
		CompoundTag spawn = (CompoundTag) root.get("Spawn");
		this.getLevel().setName(((StringTag) info.get("Name")).getValue());
		this.getLevel().setAuthor(((StringTag) info.get("Author")).getValue());
		this.getLevel().setCreationTime(((LongTag) info.get("CreationTime")).getValue());
		this.getLevel().setSeed(((LongTag) info.get("Seed")).getValue());
		this.getLevel().setGenerator(OpenClassic.getGame().getGenerator(((StringTag) info.get("Generator")).getValue()));
		
		float x = ((FloatTag) spawn.get("x")).getValue();
		float y = ((FloatTag) spawn.get("y")).getValue();
		float z = ((FloatTag) spawn.get("z")).getValue();
		float yaw = ((FloatTag) spawn.get("yaw")).getValue();
		float pitch = ((FloatTag) spawn.get("pitch")).getValue();
		this.getLevel().setSpawn(new Position(this.getLevel(), x, y, z, yaw, pitch));
		in.close();
	}
	
	@Override
	public void saveData() throws IOException {
		File file = this.getDataFile();
		if(!file.exists()) {
			if(!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			
			file.createNewFile();
		}
		
		NBTOutputStream out = new NBTOutputStream(new FileOutputStream(file));
		TagBuilder root = new TagBuilder("Level");
		TagBuilder info = new TagBuilder("Info");
		info.append("Name", this.getLevel().getName());
		info.append("Author", this.getLevel().getAuthor());
		info.append("CreationTime", this.getLevel().getCreationTime());
		info.append("Generator", this.getLevel().getGenerator().getName());
		info.append("Seed", this.getLevel().getSeed());
		
		TagBuilder spawn = new TagBuilder("Spawn");
		spawn.append("x", this.getLevel().getSpawn().getX());
		spawn.append("y", this.getLevel().getSpawn().getY());
		spawn.append("z", this.getLevel().getSpawn().getZ());
		spawn.append("yaw", this.getLevel().getSpawn().getYaw());
		spawn.append("pitch", this.getLevel().getSpawn().getPitch());
		root.append(info.toCompoundTag());
		root.append(spawn.toCompoundTag());
		out.writeTag(root.toCompoundTag());
		out.close();
	}
	
	@Override
	public ClassicColumn load(int x, int z) throws IOException {
		File file = this.getChunkFile(x, z);
		if(!file.exists()) {
			return null;
		}
		
		ClassicColumn column = new ClassicColumn(this.getLevel(), x, z);
		NBTInputStream in = new NBTInputStream(new FileInputStream(file));
		CompoundTag root = (CompoundTag) in.readTag();
		for(ClassicChunk chunk : column.getChunks()) {
			CompoundTag tag = (CompoundTag) root.get(String.valueOf(chunk.getY()));
			chunk.setBlocks(((ByteArrayTag) tag.get("Blocks")).getValue(), false);
		}
		
		in.close();
		return column;
	}

	@Override
	public void save(ClassicColumn column) throws IOException {
		File file = this.getChunkFile(column.getX(), column.getZ());
		if(!file.exists()) {
			if(!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			
			file.createNewFile();
		}
		
		NBTOutputStream out = new NBTOutputStream(new FileOutputStream(file));
		TagBuilder root = new TagBuilder("Chunk");
		for(ClassicChunk chunk : column.getChunks()) {
			TagBuilder build = new TagBuilder(String.valueOf(chunk.getY()));
			build.append("Blocks", chunk.getBlocks());
			root.append(build.toCompoundTag());
		}
		
		out.writeTag(root.toCompoundTag());
		out.close();
	}
	
	private File getChunkFile(int x, int z) {
		return new File(this.dir, "columns/" + x + "-" + z + ".column");
	}
	
	private File getDataFile() {
		return new File(this.dir, "level.map");
	}

}
