package ch.spacebase.openclassic.game.level;

import ch.spacebase.openclassic.api.level.Level;

public interface ClassicLevel extends Level {

	public void setName(String name);
	
	public void setAuthor(String name);
	
	public void setCreationTime(long time);
	
	public void setData(int width, int height, int depth, byte blocks[]);
	
}
