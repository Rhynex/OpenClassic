package ch.spacebase.openclassic.client.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

import org.lwjgl.opengl.Display;

import ch.spacebase.openclassic.api.OpenClassic;

public class Projection {

	public static final float FOV = 70;
	public static final float NEAR = 0.05f;
	public static final float FAR = 1000f;
	
	public static void ortho() {
		int width = Display.getWidth();// * 240 / Display.getHeight();
		int height = Display.getHeight();// * 240 / Display.getHeight();
		glClear(GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, height, 0, NEAR, FAR);
		glMatrixMode(GL_MODELVIEW);
		glTranslatef(0, 0, -0.45f);
	}
	
	public static void perspective() {
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective(FOV, (float) Display.getWidth() / (float) Display.getHeight(), NEAR, 256 >> OpenClassic.getGame().getConfig().getInteger("options.view-distance", 0));
		glMatrixMode(GL_MODELVIEW);
	}
	
}
