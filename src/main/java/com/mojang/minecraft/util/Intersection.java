package com.mojang.minecraft.util;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.model.Vector;

public class Intersection {

	public boolean hasEntity;
	public int x;
	public int y;
	public int z;
	public int side;
	public Vector pos;
	public Entity entity;

	public Intersection(int x, int y, int z, int side, Vector pos) {
		this.hasEntity = false;
		this.x = x;
		this.y = y;
		this.z = z;
		this.side = side;
		this.pos = new Vector(pos.x, pos.y, pos.z);
	}

	public Intersection(Entity entity) {
		this.hasEntity = true;
		this.entity = entity;
	}
	
}
