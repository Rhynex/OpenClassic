package ch.spacebase.openclassic.client.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.util.SectionSorter;

import static org.lwjgl.opengl.GL11.*;

public class LevelRenderer {

	private static final int CHUNK_SIZE = 16;
	
	private List<LevelSection> queue = new ArrayList<LevelSection>();
	private int outerList = -1;
	private int skyList = -1;
	private LevelSection sections[];
	private Level level;
	private int xcount;
	private int ycount;
	private int zcount;
	private float currentSkyColor;
	
	public LevelRenderer(Level level) {
		this.level = level;
		this.xcount = this.level.getWidth() / 16;
		this.ycount = this.level.getHeight() / 16;
		this.zcount = this.level.getDepth() / 16;
		this.sections = new LevelSection[this.xcount * this.ycount * this.zcount];
		for(int x = 0; x < this.xcount; x++) {
			for(int y = 0; y < this.ycount; y++) {
				for(int z = 0; z < this.zcount; z++) {
					this.sections[(z * this.ycount + y) * this.xcount + x] = new LevelSection(this.level, x << 4, y << 4, z << 4, CHUNK_SIZE);
					
				}
			}
		}
		
		this.refresh(0, 0, 0, this.level.getWidth(), this.level.getHeight(), this.level.getDepth());
	}
	
	public void refresh(int x1, int y1, int z1, int x2, int y2, int z2) {
		x1 /= 16;
		z1 /= 16;
		y1 /= 16;
		x2 /= 16;
		z2 /= 16;
		y2 /= 16;
		if (x1 < 0) {
			x1 = 0;
		}
		
		if (y1 < 0) {
			y1 = 0;
		}

		if (z1 < 0) {
			z1 = 0;
		}

		if (x2 > this.xcount - 1) {
			x2 = this.xcount - 1;
		}

		if (y2 > this.ycount - 1) {
			y2 = this.ycount - 1;
		}
		
		if (z2 > this.zcount - 1) {
			z2 = this.zcount - 1;
		}

		for (int x = x1; x <= x2; x++) {
			for (int z = z1; z <= z2; z++) {
				for (int y = y1; y <= y2; y++) {
					LevelSection sec = this.sections[(z * this.ycount + y) * this.xcount + x];
					if(this.queue.contains(sec)) continue;
					this.queue.add(sec);
				}
			}
		}
	}
	
	public void queueAll() {
		for(LevelSection sec : this.sections) {
			if(this.queue.contains(sec)) continue;
			this.queue.add(sec);
		}
	}
	
	public void update() {
		try {
			Collections.sort(this.queue, new SectionSorter());
		} catch(IllegalArgumentException e) {
			// silently catch comparison error...
		}
		
		if(this.queue.size() > 0) {
			int chunks = this.queue.size() < 3 ? this.queue.size() : 3;
			for(int count = 0; count < chunks; count++) {
				LevelSection sec = this.queue.remove(Math.max(this.queue.size() - count - 1, 0));
				glDeleteLists(sec.getList(), 1);
				sec.render();
			}
		}
	}
	
	private void renderSky(float x, float y, float z) {
		glPushMatrix();
		glTranslatef(x, y, z);
		int rgb = OpenClassic.getClient().getLevel().getSkyColor();
		glColor3f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
		if (this.skyList == -1 || OpenClassic.getClient().getLevel().getSkyColor() != this.currentSkyColor) {
			this.skyList = glGenLists(1);
			glNewList(this.skyList, GL_COMPILE_AND_EXECUTE);
			glBegin(GL_TRIANGLE_FAN);
			glVertex3f(0, 0, 0);

			for (int i = 0; i <= 32; ++i) {
				float angle = MathHelper.f_2PI / 32 * i;
				float xx = MathHelper.cos(angle) * 500;
				float zz = MathHelper.sin(angle) * 500;
				glVertex3f(xx, -15, zz);
			}

			glEnd();
			glEndList();
			this.currentSkyColor = OpenClassic.getClient().getLevel().getSkyColor();
		} else {
			glCallList(this.skyList);
		}

		glPopMatrix();
	}
	
	public void render() {
        glDisable(GL_TEXTURE_2D);
        ClientPlayer player = (ClientPlayer) OpenClassic.getClient().getPlayer();
        this.renderSky(player.getPosition().getX(), Math.max(this.level.getHeight() + 27, player.getPosition().getY() + this.level.getHeight()), player.getPosition().getZ());
		glEnable(GL_TEXTURE_2D);
        
		for(LevelSection section : this.sections) {
			if(!section.isEmpty() && section.getList() > 0 && Frustrum.isBoxInFrustrum(section.getX(), section.getY(), section.getZ(), section.getX() + section.getSize(), section.getY() + section.getSize(), section.getZ() + section.getSize())) {
				glCallList(section.getList());
			}
		}
		
		if(this.outerList == -1) {
			this.createOuterList();
		}
		
		glCallList(this.outerList);
		glCallList(this.outerList + 1);
		
		RenderHelper.getHelper().bindTexture("/clouds.png", true);
		ArrayRenderer.begin();
		ArrayRenderer.color(this.level.getCloudColor());

		for (int x = -2048; x < this.level.getWidth() + 2048; x += 512) {
			for (int z = -2048; z < this.level.getDepth() + 2048; z += 512) {
				ArrayRenderer.vertuv(x, this.level.getHeight() + 2, z + 512, x * 0.0004f + 0.000012f, (z + 512) * 0.0004f);
				ArrayRenderer.vertuv(x + 512, this.level.getHeight() + 2, z + 512, (x + 512) * 0.0004f + 0.000012f, (z + 512) * 0.0004f);
				ArrayRenderer.vertuv(x + 512, this.level.getHeight() + 2, z, (x + 512) * 0.0004f + 0.000012f, z * 0.0004f);
				ArrayRenderer.vertuv(x, this.level.getHeight() + 2, z, x * 0.0004f + 0.000012f, z * 0.0004f);
				ArrayRenderer.vertuv(x, this.level.getHeight() + 2, z, x * 0.0004f + 0.000012f, z * 0.0004f);
				ArrayRenderer.vertuv(x + 512, this.level.getHeight() + 2, z, (x + 512) * 0.0004f + 0.000012f, z * 0.0004f);
				ArrayRenderer.vertuv(x + 512, this.level.getHeight() + 2, z + 512, (x + 512) * 0.0004f + 0.000012f, (z + 512) * 0.0004f);
				ArrayRenderer.vertuv(x, this.level.getHeight() + 2, z + 512, x * 0.0004f + 0.000012f, (z + 512) * 0.0004f);
			}
		}

		ArrayRenderer.end();
	}
	
	private void createOuterList() {
		this.outerList = glGenLists(2);
		glNewList(this.outerList, GL_COMPILE);
		RenderHelper.getHelper().bindTexture("/rock.png", true);
		glColor4f(0.5f, 0.5f, 0.5f, 1);
		float ground = this.level.getWaterLevel() - 2;
		int area = 128;
		if (area > this.level.getWidth()) {
			area = this.level.getWidth();
		}

		if (area > this.level.getDepth()) {
			area = this.level.getDepth();
		}

		ArrayRenderer.begin();

		for (int x = -2048; x < this.level.getWidth() + 2048; x += area) {
			for (int z = -2048; z < this.level.getDepth() + 2048; z += area) {
				float y = ground;
				if (x >= 0 && z >= 0 && x < this.level.getWidth() && z < this.level.getDepth()) {
					y = 0;
				}

				ArrayRenderer.vertuv(x, y, (z + area), 0, area);
				ArrayRenderer.vertuv((x + area), y, (z + area), area, area);
				ArrayRenderer.vertuv((x + area), y, z, area, 0);
				ArrayRenderer.vertuv(x, y, z, 0, 0);
			}
		}

		ArrayRenderer.end();
		glColor3f(0.8f, 0.8f, 0.8f);
		ArrayRenderer.begin();

		for (int x = 0; x < this.level.getWidth(); x += area) {
			ArrayRenderer.vertuv(x, 0, 0, 0, 0);
			ArrayRenderer.vertuv((x + area), 0, 0, area, 0);
			ArrayRenderer.vertuv((x + area), ground, 0, area, ground);
			ArrayRenderer.vertuv(x, ground, 0, 0, ground);
			ArrayRenderer.vertuv(x, ground, this.level.getDepth(), 0, ground);
			ArrayRenderer.vertuv((x + area), ground, this.level.getDepth(), area, ground);
			ArrayRenderer.vertuv((x + area), 0, this.level.getDepth(), area, 0);
			ArrayRenderer.vertuv(x, 0, this.level.getDepth(), 0, 0);
		}

		glColor3f(0.6f, 0.6f, 0.6f);

		for (int z = 0; z < this.level.getDepth(); z += area) {
			ArrayRenderer.vertuv(0, ground, z, 0, 0);
			ArrayRenderer.vertuv(0, ground, (z + area), area, 0);
			ArrayRenderer.vertuv(0, 0, (z + area), area, ground);
			ArrayRenderer.vertuv(0, 0, z, 0, ground);
			ArrayRenderer.vertuv(this.level.getWidth(), 0, z, 0, ground);
			ArrayRenderer.vertuv(this.level.getWidth(), 0, (z + area), area, ground);
			ArrayRenderer.vertuv(this.level.getWidth(), ground, (z + area), area, 0);
			ArrayRenderer.vertuv(this.level.getWidth(), ground, z, 0, 0);
		}

		ArrayRenderer.end();
		glEndList();
		glNewList(this.outerList + 1, GL_COMPILE);
		RenderHelper.getHelper().bindTexture("/water.png", true);
		glColor3f(1, 1, 1);
		float sea = this.level.getWaterLevel();
		ArrayRenderer.begin();

		for (int x = -2048; x < this.level.getWidth() + 2048; x += area) {
			for (int z = -2048; z < this.level.getDepth() + 2048; z += area) {
				float y = sea - 0.1f;
				if (x < 0 || z < 0 || x >= this.level.getWidth() || z >= this.level.getDepth()) {
					ArrayRenderer.vertuv(x, y, (z + area), 0, area);
					ArrayRenderer.vertuv((x + area), y, (z + area), area, area);
					ArrayRenderer.vertuv((x + area), y, z, area, 0);
					ArrayRenderer.vertuv(x, y, z, 0, 0);
					ArrayRenderer.vertuv(x, y, z, 0, 0);
					ArrayRenderer.vertuv((x + area), y, z, area, 0);
					ArrayRenderer.vertuv((x + area), y, (z + area), area, area);
					ArrayRenderer.vertuv(x, y, (z + area), 0, area);
				}
			}
		}

		ArrayRenderer.end();
		glEndList();
	}
	
}
