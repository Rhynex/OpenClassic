package ch.spacebase.openclassic.client.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.util.glu.Sphere;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.level.column.Chunk;
import ch.spacebase.openclassic.api.level.column.Column;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.util.ChunkSorter;
import ch.spacebase.openclassic.game.level.ClassicLevel;
import ch.spacebase.openclassic.game.level.column.ClassicChunk;
import ch.spacebase.openclassic.game.level.column.ClassicColumn;

import static org.lwjgl.opengl.GL11.*;

public class LevelRenderer {
	
	private List<ClassicChunk> queue = new ArrayList<ClassicChunk>();
	private int skyList = -1;
	private ClassicLevel level;
	
	public LevelRenderer(ClassicLevel level) {
		this.level = level;
	}
	
	public void queue(int x, int z) {
		ClassicColumn column = this.level.getColumn(x, z);
		if(column != null) {
			this.queue(column);
		}
	}
	
	public void queue(int x, int y, int z) {
		ClassicColumn column = this.level.getColumn(x, z);
		if(column != null) {
			ClassicChunk chunk = column.getChunk(y);
			if(chunk != null) {
				this.queue(chunk);
			}
		}
	}
	
	public void queue(ClassicColumn column) {
		for(Chunk chunk : column.getChunks()) {
			this.queue((ClassicChunk) chunk);
		}
	}
	
	public void queue(ClassicChunk chunk) {
		if(!this.queue.contains(chunk)) {
			this.queue.add(chunk);
		}
	}
	
	public void queue(int x1, int y1, int z1, int x2, int y2, int z2) {
		x1 = x1 >> 4;
		y1 = y1 >> 4;
		z1 = z1 >> 4;
		x2 = x2 >> 4;
		y2 = y2 >> 4;
		z2 = z2 >> 4;
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				for (int z = z1; z <= z2; z++) {
					this.queue(x, y, z);
				}
			}
		}
	}
	
	public void queueAll() {
		for(Column column : this.level.getColumns()) {
			this.queue((ClassicColumn) column);
		}
	}
	
	public void remove(ClassicColumn column) {
		for(Chunk chunk : column.getChunks()) {
			this.remove((ClassicChunk) chunk);
		}
	}
	
	public void remove(ClassicChunk chunk) {
		if(this.queue.contains(chunk)) {
			this.queue.remove(chunk);
		}
	}
	
	public void update() {
		if(this.queue.size() > 0) {
			try {
				Collections.sort(this.queue, new ChunkSorter());
			} catch(IllegalArgumentException e) {
				// silently catch comparison error...
			}
			
			int chunks = this.queue.size() < 6 ? this.queue.size() : 6;
			for(int count = 0; count < chunks; count++) {
				ClassicChunk chunk = this.queue.remove(Math.max(this.queue.size() - count - 1, 0));
				if(chunk.getList() != -1) glDeleteLists(chunk.getList(), 2);
				chunk.render();
			}
		}
	}
	
	// TODO: fix sky
	private void renderSky(float x, float y, float z) {
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_FOG);
		int rgb = OpenClassic.getClient().getLevel().getSkyColor();
		glColor4f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f, 1);
        if (this.skyList == -1) {
        	this.skyList = glGenLists(1);

            Sphere sphere = new Sphere();
            glNewList(this.skyList, GL_COMPILE);
            sphere.draw(16, 16, 128);
            glEndList();
        }

        glPushMatrix();
        glTranslatef(x, y, z);
        glCallList(this.skyList);
        glPopMatrix();
        glEnable(GL_FOG);
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_TEXTURE_2D);
	}
	
	public void render() {
        glDisable(GL_TEXTURE_2D);
        ClientPlayer player = (ClientPlayer) OpenClassic.getClient().getPlayer();
        this.renderSky(player.getPosition().getX(), Math.max(Constants.COLUMN_HEIGHT + 27, player.getPosition().getY() + Constants.COLUMN_HEIGHT), player.getPosition().getZ());
		glEnable(GL_TEXTURE_2D);

		for(Column column : this.level.getColumns()) {
			for(Chunk chunk : column.getChunks()) {
				ClassicChunk classic = (ClassicChunk) chunk;
				if(!classic.isEmpty() && classic.getList() > 0 && Frustrum.isBoxInFrustrum(chunk.getWorldX(), chunk.getWorldY(), chunk.getWorldZ(), chunk.getWorldX() + Constants.CHUNK_WIDTH, chunk.getWorldY() + Constants.CHUNK_HEIGHT, chunk.getWorldZ() + Constants.CHUNK_DEPTH)) {
					glCallList(classic.getList());
					glCallList(classic.getList() + 1);
				}
			}
		}
	}
	
}
