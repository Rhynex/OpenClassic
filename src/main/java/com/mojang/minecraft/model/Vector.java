package com.mojang.minecraft.model;

public final class Vector {

	private static final float EPSILON = 0.0000001f;
	
	public float x;
	public float y;
	public float z;

	public Vector(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public final Vector subtract(Vector other) {
		return new Vector(this.x - other.x, this.y - other.y, this.z - other.z);
	}

	public final Vector normalize() {
		float len = (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
		return new Vector(this.x / len, this.y / len, this.z / len);
	}

	public final Vector add(float x, float y, float z) {
		return new Vector(this.x + x, this.y + y, this.z + z);
	}

	public final float distance(Vector other) {
		return (float) Math.sqrt(this.distanceSquared(other));
	}

	public final float distanceSquared(Vector other) {
		float x = other.x - this.x;
		float y = other.y - this.y;
		float z = other.z - this.z;
		return x * x + y * y + z * z;
	}

	public final Vector getXIntersection(Vector other, float intersectX) {
		float x = other.x - this.x;
		float y = other.y - this.y;
		float z = other.z - this.z;
		float ln = (intersectX - this.x) / x;
		return x * x < EPSILON ? null : (ln >= 0.0F && ln <= 1.0F ? new Vector(this.x + x * ln, this.y + y * ln, this.z + z * ln) : null);
	}

	public final Vector getYIntersection(Vector other, float intersectY) {
		float x = other.x - this.x;
		float y = other.y - this.y;
		float z = other.z - this.z;
		float ln = (intersectY - this.y) / y;
		return y * y < EPSILON ? null : (ln >= 0.0F && ln <= 1.0F ? new Vector(this.x + x * ln, this.y + y * ln, this.z + z * ln) : null);
	}

	public final Vector getZIntersection(Vector other, float intersectZ) {
		float x = other.x - this.x;
		float y = other.y - this.y;
		float z = other.z - this.z;
		float ln = (intersectZ - this.z) / z;
		return z * z < EPSILON ? null : (ln >= 0.0F && ln <= 1.0F ? new Vector(this.x + x * ln, this.y + y * ln, this.z + z * ln) : null);
	}

	public final String toString() {
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}
}
