package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.block.BlockType;

public class ScreenBlock {

	public final BlockType block;
	public final int x;
	public final int y;
	
	public ScreenBlock(BlockType block, int x, int y) {
		this.block = block;
		this.x = x;
		this.y = y;
	}
	
}
