package com.mojang.minecraft.entity.item;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;

public class ItemModel {

	private BlockType block;

	public ItemModel(int block) {
		this.block = Blocks.fromId(block);
	}

	public void render() {
		GL11.glTranslatef(-0.1F, 0, -0.1F);
		this.block.getModel().renderScaled(0, 0, 0, 0.2F, 1);
		GL11.glTranslatef(0.1F, 0, 0.1F);
	}
}
