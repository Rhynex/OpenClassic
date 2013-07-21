package com.mojang.minecraft.render;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class Frustum {

	private static Frustum instance = new Frustum();
	
	public float[][] frustum = new float[16][16];
	public float[] projectionMatrix = new float[16];
	public float[] modelviewMatrix = new float[16];
	public float[] clippingMatrix = new float[16];
	
	private FloatBuffer projectionMatrixBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer modelviewMatrixBuffer = BufferUtils.createFloatBuffer(16);

	public static Frustum getInstance() {
		instance.projectionMatrixBuffer.clear();
		instance.modelviewMatrixBuffer.clear();
		GL11.glGetFloat(2983, instance.projectionMatrixBuffer);
		GL11.glGetFloat(2982, instance.modelviewMatrixBuffer);
		instance.projectionMatrixBuffer.flip().limit(16);
		instance.projectionMatrixBuffer.get(instance.projectionMatrix);
		instance.modelviewMatrixBuffer.flip().limit(16);
		instance.modelviewMatrixBuffer.get(instance.modelviewMatrix);
		instance.clippingMatrix[0] = instance.modelviewMatrix[0] * instance.projectionMatrix[0] + instance.modelviewMatrix[1] * instance.projectionMatrix[4] + instance.modelviewMatrix[2] * instance.projectionMatrix[8] + instance.modelviewMatrix[3] * instance.projectionMatrix[12];
		instance.clippingMatrix[1] = instance.modelviewMatrix[0] * instance.projectionMatrix[1] + instance.modelviewMatrix[1] * instance.projectionMatrix[5] + instance.modelviewMatrix[2] * instance.projectionMatrix[9] + instance.modelviewMatrix[3] * instance.projectionMatrix[13];
		instance.clippingMatrix[2] = instance.modelviewMatrix[0] * instance.projectionMatrix[2] + instance.modelviewMatrix[1] * instance.projectionMatrix[6] + instance.modelviewMatrix[2] * instance.projectionMatrix[10] + instance.modelviewMatrix[3] * instance.projectionMatrix[14];
		instance.clippingMatrix[3] = instance.modelviewMatrix[0] * instance.projectionMatrix[3] + instance.modelviewMatrix[1] * instance.projectionMatrix[7] + instance.modelviewMatrix[2] * instance.projectionMatrix[11] + instance.modelviewMatrix[3] * instance.projectionMatrix[15];
		instance.clippingMatrix[4] = instance.modelviewMatrix[4] * instance.projectionMatrix[0] + instance.modelviewMatrix[5] * instance.projectionMatrix[4] + instance.modelviewMatrix[6] * instance.projectionMatrix[8] + instance.modelviewMatrix[7] * instance.projectionMatrix[12];
		instance.clippingMatrix[5] = instance.modelviewMatrix[4] * instance.projectionMatrix[1] + instance.modelviewMatrix[5] * instance.projectionMatrix[5] + instance.modelviewMatrix[6] * instance.projectionMatrix[9] + instance.modelviewMatrix[7] * instance.projectionMatrix[13];
		instance.clippingMatrix[6] = instance.modelviewMatrix[4] * instance.projectionMatrix[2] + instance.modelviewMatrix[5] * instance.projectionMatrix[6] + instance.modelviewMatrix[6] * instance.projectionMatrix[10] + instance.modelviewMatrix[7] * instance.projectionMatrix[14];
		instance.clippingMatrix[7] = instance.modelviewMatrix[4] * instance.projectionMatrix[3] + instance.modelviewMatrix[5] * instance.projectionMatrix[7] + instance.modelviewMatrix[6] * instance.projectionMatrix[11] + instance.modelviewMatrix[7] * instance.projectionMatrix[15];
		instance.clippingMatrix[8] = instance.modelviewMatrix[8] * instance.projectionMatrix[0] + instance.modelviewMatrix[9] * instance.projectionMatrix[4] + instance.modelviewMatrix[10] * instance.projectionMatrix[8] + instance.modelviewMatrix[11] * instance.projectionMatrix[12];
		instance.clippingMatrix[9] = instance.modelviewMatrix[8] * instance.projectionMatrix[1] + instance.modelviewMatrix[9] * instance.projectionMatrix[5] + instance.modelviewMatrix[10] * instance.projectionMatrix[9] + instance.modelviewMatrix[11] * instance.projectionMatrix[13];
		instance.clippingMatrix[10] = instance.modelviewMatrix[8] * instance.projectionMatrix[2] + instance.modelviewMatrix[9] * instance.projectionMatrix[6] + instance.modelviewMatrix[10] * instance.projectionMatrix[10] + instance.modelviewMatrix[11] * instance.projectionMatrix[14];
		instance.clippingMatrix[11] = instance.modelviewMatrix[8] * instance.projectionMatrix[3] + instance.modelviewMatrix[9] * instance.projectionMatrix[7] + instance.modelviewMatrix[10] * instance.projectionMatrix[11] + instance.modelviewMatrix[11] * instance.projectionMatrix[15];
		instance.clippingMatrix[12] = instance.modelviewMatrix[12] * instance.projectionMatrix[0] + instance.modelviewMatrix[13] * instance.projectionMatrix[4] + instance.modelviewMatrix[14] * instance.projectionMatrix[8] + instance.modelviewMatrix[15] * instance.projectionMatrix[12];
		instance.clippingMatrix[13] = instance.modelviewMatrix[12] * instance.projectionMatrix[1] + instance.modelviewMatrix[13] * instance.projectionMatrix[5] + instance.modelviewMatrix[14] * instance.projectionMatrix[9] + instance.modelviewMatrix[15] * instance.projectionMatrix[13];
		instance.clippingMatrix[14] = instance.modelviewMatrix[12] * instance.projectionMatrix[2] + instance.modelviewMatrix[13] * instance.projectionMatrix[6] + instance.modelviewMatrix[14] * instance.projectionMatrix[10] + instance.modelviewMatrix[15] * instance.projectionMatrix[14];
		instance.clippingMatrix[15] = instance.modelviewMatrix[12] * instance.projectionMatrix[3] + instance.modelviewMatrix[13] * instance.projectionMatrix[7] + instance.modelviewMatrix[14] * instance.projectionMatrix[11] + instance.modelviewMatrix[15] * instance.projectionMatrix[15];
		instance.frustum[0][0] = instance.clippingMatrix[3] - instance.clippingMatrix[0];
		instance.frustum[0][1] = instance.clippingMatrix[7] - instance.clippingMatrix[4];
		instance.frustum[0][2] = instance.clippingMatrix[11] - instance.clippingMatrix[8];
		instance.frustum[0][3] = instance.clippingMatrix[15] - instance.clippingMatrix[12];
		normalize(instance.frustum, 0);
		instance.frustum[1][0] = instance.clippingMatrix[3] + instance.clippingMatrix[0];
		instance.frustum[1][1] = instance.clippingMatrix[7] + instance.clippingMatrix[4];
		instance.frustum[1][2] = instance.clippingMatrix[11] + instance.clippingMatrix[8];
		instance.frustum[1][3] = instance.clippingMatrix[15] + instance.clippingMatrix[12];
		normalize(instance.frustum, 1);
		instance.frustum[2][0] = instance.clippingMatrix[3] + instance.clippingMatrix[1];
		instance.frustum[2][1] = instance.clippingMatrix[7] + instance.clippingMatrix[5];
		instance.frustum[2][2] = instance.clippingMatrix[11] + instance.clippingMatrix[9];
		instance.frustum[2][3] = instance.clippingMatrix[15] + instance.clippingMatrix[13];
		normalize(instance.frustum, 2);
		instance.frustum[3][0] = instance.clippingMatrix[3] - instance.clippingMatrix[1];
		instance.frustum[3][1] = instance.clippingMatrix[7] - instance.clippingMatrix[5];
		instance.frustum[3][2] = instance.clippingMatrix[11] - instance.clippingMatrix[9];
		instance.frustum[3][3] = instance.clippingMatrix[15] - instance.clippingMatrix[13];
		normalize(instance.frustum, 3);
		instance.frustum[4][0] = instance.clippingMatrix[3] - instance.clippingMatrix[2];
		instance.frustum[4][1] = instance.clippingMatrix[7] - instance.clippingMatrix[6];
		instance.frustum[4][2] = instance.clippingMatrix[11] - instance.clippingMatrix[10];
		instance.frustum[4][3] = instance.clippingMatrix[15] - instance.clippingMatrix[14];
		normalize(instance.frustum, 4);
		instance.frustum[5][0] = instance.clippingMatrix[3] + instance.clippingMatrix[2];
		instance.frustum[5][1] = instance.clippingMatrix[7] + instance.clippingMatrix[6];
		instance.frustum[5][2] = instance.clippingMatrix[11] + instance.clippingMatrix[10];
		instance.frustum[5][3] = instance.clippingMatrix[15] + instance.clippingMatrix[14];
		normalize(instance.frustum, 5);
		return instance;
	}

	private static void normalize(float[][] frustum, int plane) {
		float var2 = (float) Math.sqrt(frustum[plane][0] * frustum[plane][0] + frustum[plane][1] * frustum[plane][1] + frustum[plane][2] * frustum[plane][2]);
		frustum[plane][0] /= var2;
		frustum[plane][1] /= var2;
		frustum[plane][2] /= var2;
		frustum[plane][3] /= var2;
	}
	
	public boolean isBoxInFrustum(float x1, float y1, float z1, float x2, float y2, float z2) {
		for (int plane = 0; plane < 6; ++plane) {
			if (this.frustum[plane][0] * x1 + this.frustum[plane][1] * y1 + this.frustum[plane][2] * z1 + this.frustum[plane][3] <= 0.0F && this.frustum[plane][0] * x2 + this.frustum[plane][1] * y1 + this.frustum[plane][2] * z1 + this.frustum[plane][3] <= 0.0F && this.frustum[plane][0] * x1 + this.frustum[plane][1] * y2 + this.frustum[plane][2] * z1 + this.frustum[plane][3] <= 0.0F && this.frustum[plane][0] * x2 + this.frustum[plane][1] * y2 + this.frustum[plane][2] * z1 + this.frustum[plane][3] <= 0.0F && this.frustum[plane][0] * x1 + this.frustum[plane][1] * y1 + this.frustum[plane][2] * z2 + this.frustum[plane][3] <= 0.0F && this.frustum[plane][0] * x2 + this.frustum[plane][1] * y1 + this.frustum[plane][2] * z2 + this.frustum[plane][3] <= 0.0F && this.frustum[plane][0] * x1 + this.frustum[plane][1] * y2 + this.frustum[plane][2] * z2 + this.frustum[plane][3] <= 0.0F && this.frustum[plane][0] * x2 + this.frustum[plane][1] * y2 + this.frustum[plane][2] * z2 + this.frustum[plane][3] <= 0.0F) {
				return false;
			}
		}

		return true;
	}

}
