package com.mojang.minecraft.render;

import ch.spacebase.openclassic.api.block.BlockType;

import com.mojang.minecraft.Minecraft;

public final class HeldBlock {

	public Minecraft mc;
	public BlockType block = null;
	public float heldPosition = 0;
	public float lastPosition = 0;
	public int heldOffset = 0;
	public boolean moving = false;

	public HeldBlock(Minecraft mc) {
		this.mc = mc;
	}
}
