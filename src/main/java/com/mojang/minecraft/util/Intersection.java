package com.mojang.minecraft.util;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.model.Vector;

public class Intersection {

	public boolean entityPos;
	public int x;
	public int y;
	public int z;
	public int side;
	public Vector blockPos;
	public Entity entity;

	public Intersection(int x, int y, int z, int side, Vector blockPos) {
		this.entityPos = false;
		this.x = x;
		this.y = y;
		this.z = z;
		this.side = side;
		this.blockPos = new Vector(blockPos.x, blockPos.y, blockPos.z);
	}

	public Intersection(Entity entity) {
		this.entityPos = true;
		this.entity = entity;
	}
	
}
