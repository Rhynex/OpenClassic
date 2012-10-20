package ch.spacebase.openclassic.client.render;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;

public class ArrayRenderer {
	
	private static FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(524288);
	private static int vertices = 0;
	private static float u;
	private static float v;
	private static float nx;
	private static float ny;
	private static float nz;
	private static float r;
	private static float g;
	private static float b;
	private static float a;
	private static boolean colors = false;
	private static boolean textures = false;
	private static boolean alpha = false;
	private static boolean normal = false;
	private static int vertexSize = 3;
	private static int length = 0;

	public static void begin() {
		clear();
	}

	public static void end() {
		if (vertices > 0) {
			vertexBuffer.flip();

			if (textures && colors) {
				if(normal) {
					glInterleavedArrays(GL_T2F_C4F_N3F_V3F, 0, vertexBuffer);
				} else {
					glInterleavedArrays(GL_T2F_C3F_V3F, 0, vertexBuffer);
				}
			} else if (textures) {
				if(normal) {
					glInterleavedArrays(GL_T2F_N3F_V3F, 0, vertexBuffer);
				} else {
					glInterleavedArrays(GL_T2F_V3F, 0, vertexBuffer);
				}
			} else if (colors) {
				if(normal) {
					glInterleavedArrays(GL_C4F_N3F_V3F, 0, vertexBuffer);
				} else {
					glInterleavedArrays(GL_C3F_V3F, 0, vertexBuffer);
				}
			} else {
				glInterleavedArrays(GL_V3F, 0, vertexBuffer);
			}

			glEnableClientState(GL_VERTEX_ARRAY);
			if (textures) {
				glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			}

			if (colors) {
				glEnableClientState(GL_COLOR_ARRAY);
			}

			if(normal) {
				glEnableClientState(GL_NORMAL_ARRAY);
			}

			glDrawArrays(GL_QUADS, 0, vertices);
			glDisableClientState(GL_VERTEX_ARRAY);
			if (textures) {
				glDisableClientState(GL_TEXTURE_COORD_ARRAY);
			}

			if (colors) {
				glDisableClientState(GL_COLOR_ARRAY);
			}
		}

		clear();
	}

	private static void clear() {
		vertices = 0;
		length = 0;
		vertexSize = 0;
		colors = false;
		textures = false;
		alpha = false;
		normal = false;
		vertexBuffer.clear();
	}

	public static void vertuv(float x, float y, float z, float u, float v) {
		if(!textures) {
			vertexSize += 2;
		}

		textures = true;
		ArrayRenderer.u = u;
		ArrayRenderer.v = v;
		vert(x, y, z);
	}

	public static void normal(float x, float y, float z) {
		if(!normal) {
			vertexSize += 4; // TODO: alpha :/
		}

		normal = true;
		if(!alpha) {
			ArrayRenderer.a = 1 / 255f;
		}

		ArrayRenderer.nx = x;
		ArrayRenderer.ny = y;
		ArrayRenderer.nz = z;
	}

	public static void vert(float x, float y, float z) {
		if (textures) {
			vertexBuffer.put(length++, u);
			vertexBuffer.put(length++, v);
		}

		if (colors) {
			vertexBuffer.put(length++, r);
			vertexBuffer.put(length++, g);
			vertexBuffer.put(length++, b);
		}

		if(normal) {
			vertexBuffer.put(length++, a);
			vertexBuffer.put(length++, nx);
			vertexBuffer.put(length++, ny);
			vertexBuffer.put(length++, nz);
		}

		vertexBuffer.put(length++, x);
		vertexBuffer.put(length++, y);
		vertexBuffer.put(length++, z);
		vertices++;

		if (vertices % 4 == 0 && length >= 524288 - (vertexSize << 2)) {
			end();	
		}
	}

	public static void color(int color) {
		color(color, false);
	}

	public static void color(int color, boolean useAlpha) {
		byte red = (byte) (color >> 16 & 255);
		byte green = (byte) (color >> 8 & 255);
		byte blue = (byte) (color & 255);

		if(useAlpha) {
			byte alpha = (byte) (color >>> 24);
			color((red & 255) / 255f, (green & 255) / 255f, (blue & 255) / 255f, (alpha & 255) / 255f);
		} else {
			color((red & 255) / 255f, (green & 255) / 255f, (blue & 255) / 255f);
		}
	}

	public static void color(float r, float g, float b, float a) {
		if(!alpha) {
			alpha = true;
			normal(0, 0, 1);
		}

		alpha = true;
		ArrayRenderer.a = a;
		color(r, g, b);
	}

	public static void color(float r, float g, float b) {	
		if(!colors) {
			vertexSize += 3;
		}

		colors = true;
		ArrayRenderer.r = r;
		ArrayRenderer.g = g;
		ArrayRenderer.b = b;
	}

}
