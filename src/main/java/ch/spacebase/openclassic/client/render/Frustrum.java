package ch.spacebase.openclassic.client.render;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;

public class Frustrum {
	
	public static float[][] frustrum = new float[16][16];
	public static float[] projection = new float[16];
	public static float[] modelview = new float[16];
	public static float[] clipping = new float[16];
	
	private static FloatBuffer projectionbuf = BufferUtils.createFloatBuffer(16);
	private static FloatBuffer modelviewbuf = BufferUtils.createFloatBuffer(16);

	public static void update() {
		projectionbuf.rewind();
		modelviewbuf.rewind();
		glGetFloat(GL_PROJECTION_MATRIX, projectionbuf);
		glGetFloat(GL_MODELVIEW_MATRIX, modelviewbuf);
		projectionbuf.get(projection);
		modelviewbuf.get(modelview);
		
		clipping[0] = modelview[0] * projection[0] + modelview[1] * projection[4] + modelview[2] * projection[8] + modelview[3] * projection[12];
		clipping[1] = modelview[0] * projection[1] + modelview[1] * projection[5] + modelview[2] * projection[9] + modelview[3] * projection[13];
		clipping[2] = modelview[0] * projection[2] + modelview[1] * projection[6] + modelview[2] * projection[10] + modelview[3] * projection[14];
		clipping[3] = modelview[0] * projection[3] + modelview[1] * projection[7] + modelview[2] * projection[11] + modelview[3] * projection[15];
		clipping[4] = modelview[4] * projection[0] + modelview[5] * projection[4] + modelview[6] * projection[8] + modelview[7] * projection[12];
		clipping[5] = modelview[4] * projection[1] + modelview[5] * projection[5] + modelview[6] * projection[9] + modelview[7] * projection[13];
		clipping[6] = modelview[4] * projection[2] + modelview[5] * projection[6] + modelview[6] * projection[10] + modelview[7] * projection[14];
		clipping[7] = modelview[4] * projection[3] + modelview[5] * projection[7] + modelview[6] * projection[11] + modelview[7] * projection[15];
		clipping[8] = modelview[8] * projection[0] + modelview[9] * projection[4] + modelview[10] * projection[8] + modelview[11] * projection[12];
		clipping[9] = modelview[8] * projection[1] + modelview[9] * projection[5] + modelview[10] * projection[9] + modelview[11] * projection[13];
		clipping[10] = modelview[8] * projection[2] + modelview[9] * projection[6] + modelview[10] * projection[10] + modelview[11] * projection[14];
		clipping[11] = modelview[8] * projection[3] + modelview[9] * projection[7] + modelview[10] * projection[11] + modelview[11] * projection[15];
		clipping[12] = modelview[12] * projection[0] + modelview[13] * projection[4] + modelview[14] * projection[8] + modelview[15] * projection[12];
		clipping[13] = modelview[12] * projection[1] + modelview[13] * projection[5] + modelview[14] * projection[9] + modelview[15] * projection[13];
		clipping[14] = modelview[12] * projection[2] + modelview[13] * projection[6] + modelview[14] * projection[10] + modelview[15] * projection[14];
		clipping[15] = modelview[12] * projection[3] + modelview[13] * projection[7] + modelview[14] * projection[11] + modelview[15] * projection[15];
		frustrum[0][0] = clipping[3] - clipping[0];
		frustrum[0][1] = clipping[7] - clipping[4];
		frustrum[0][2] = clipping[11] - clipping[8];
		frustrum[0][3] = clipping[15] - clipping[12];
		normalize(frustrum[0]);
		frustrum[1][0] = clipping[3] + clipping[0];
		frustrum[1][1] = clipping[7] + clipping[4];
		frustrum[1][2] = clipping[11] + clipping[8];
		frustrum[1][3] = clipping[15] + clipping[12];
		normalize(frustrum[1]);
		frustrum[2][0] = clipping[3] + clipping[1];
		frustrum[2][1] = clipping[7] + clipping[5];
		frustrum[2][2] = clipping[11] + clipping[9];
		frustrum[2][3] = clipping[15] + clipping[13];
		normalize(frustrum[2]);
		frustrum[3][0] = clipping[3] - clipping[1];
		frustrum[3][1] = clipping[7] - clipping[5];
		frustrum[3][2] = clipping[11] - clipping[9];
		frustrum[3][3] = clipping[15] - clipping[13];
		normalize(frustrum[3]);
		frustrum[4][0] = clipping[3] - clipping[2];
		frustrum[4][1] = clipping[7] - clipping[6];
		frustrum[4][2] = clipping[11] - clipping[10];
		frustrum[4][3] = clipping[15] - clipping[14];
		normalize(frustrum[4]);
		frustrum[5][0] = clipping[3] + clipping[2];
		frustrum[5][1] = clipping[7] + clipping[6];
		frustrum[5][2] = clipping[11] + clipping[10];
		frustrum[5][3] = clipping[15] + clipping[14];
		normalize(frustrum[5]);
	}

	private static void normalize(float[] plane) {
		float sqrt = (float) Math.sqrt(plane[0] * plane[0] + plane[1] * plane[1] + plane[2] * plane[2]);
		plane[0] /= sqrt;
		plane[1] /= sqrt;
		plane[2] /= sqrt;
		plane[3] /= sqrt;
	}
	
	public static boolean isBoxInFrustrum(float x1, float y1, float z1, float x2, float y2, float z2) {
		for (int plane = 0; plane < 6; plane++) {
			if (frustrum[plane][0] * x1 + frustrum[plane][1] * y1 + frustrum[plane][2] * z1 + frustrum[plane][3] <= 0 && frustrum[plane][0] * x2 + frustrum[plane][1] * y1 + frustrum[plane][2] * z1 + frustrum[plane][3] <= 0 && frustrum[plane][0] * x1 + frustrum[plane][1] * y2 + frustrum[plane][2] * z1 + frustrum[plane][3] <= 0 && frustrum[plane][0] * x2 + frustrum[plane][1] * y2 + frustrum[plane][2] * z1 + frustrum[plane][3] <= 0 && frustrum[plane][0] * x1 + frustrum[plane][1] * y1 + frustrum[plane][2] * z2 + frustrum[plane][3] <= 0 && frustrum[plane][0] * x2 + frustrum[plane][1] * y1 + frustrum[plane][2] * z2 + frustrum[plane][3] <= 0 && frustrum[plane][0] * x1 + frustrum[plane][1] * y2 + frustrum[plane][2] * z2 + frustrum[plane][3] <= 0 && frustrum[plane][0] * x2 + frustrum[plane][1] * y2 + frustrum[plane][2] * z2 + frustrum[plane][3] <= 0) {
				return false;
			}
		}

		return true;
	}

}
