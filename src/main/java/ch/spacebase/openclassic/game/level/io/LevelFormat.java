package ch.spacebase.openclassic.game.level.io;

import java.io.IOException;

import ch.spacebase.openclassic.game.level.ClassicLevel;
import ch.spacebase.openclassic.game.level.column.ClassicColumn;

public abstract class LevelFormat {
	
	private ClassicLevel level;
	
	public ClassicLevel getLevel() {
		return this.level;
	}
	
	public void setLevel(ClassicLevel level) {
		this.level = level;
	}
	
	public abstract boolean exists();
	
	public abstract boolean exists(int x, int z);
	
	public abstract void loadData() throws IOException;
	
	public abstract void saveData() throws IOException;
	
	public abstract ClassicColumn load(int x, int z) throws IOException;
	
	public abstract void save(ClassicColumn column) throws IOException;
	
}
