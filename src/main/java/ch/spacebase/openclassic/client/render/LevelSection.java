package ch.spacebase.openclassic.client.render;

import ch.spacebase.openclassic.api.block.model.EmptyModel;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.client.level.ClientLevel;
import static org.lwjgl.opengl.GL11.*;

public class LevelSection {

	private Level level;
	private int x;
	private int y;
	private int z;
	private int size;
	private int list = -1;
	private boolean empty = true;
	
	public LevelSection(Level level, int x, int y, int z, int size) {
		this.level = level;
		this.x = x;
		this.y = y;
		this.z = z;
		this.size = size;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public int getZ() {
		return this.z;
	}
	
	public int getSize() {
		return this.size;
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
		for(int x = this.x; x <= this.x + this.size; x++) {
			for(int y = this.y; y <= this.y + this.size; y++) {
				for(int z = this.z; z <= this.z + this.size; z++) {
					if(!(this.level.getBlockTypeAt(x, y, z).getModel() instanceof EmptyModel)) {
						this.empty = false;
						this.level.getBlockTypeAt(x, y, z).getModel().render(x, y, z, ((ClientLevel) this.level).getBrightness(x, y, z));
					}
				}
			}
		}
		
		glEndList();
	}
	
}
