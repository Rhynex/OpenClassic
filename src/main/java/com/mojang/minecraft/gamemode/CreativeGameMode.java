package com.mojang.minecraft.gamemode;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.client.gui.BlockSelectScreen;
import ch.spacebase.openclassic.client.level.ClientLevel;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.entity.player.LocalPlayer;

public class CreativeGameMode extends GameMode {

	public CreativeGameMode(Minecraft mc) {
		super(mc);
		this.creative = true;
	}

	public void openInventory() {
		OpenClassic.getClient().setActiveComponent(new BlockSelectScreen());
	}

	public void apply(ClientLevel level) {
		super.apply(level);
		level.removeAllNonCreativeModeEntities();
	}

	public void apply(LocalPlayer player) {
		int slot = 0;
		for(BlockType block : Blocks.getBlocks()) {
			if(slot >= 9) break;
			if(block != null && block.isSelectable()) {
				player.inventory.count[slot] = 1;
				player.inventory.slots[slot] = block.getId();
				slot++;
			}
		}
	}

	public boolean isSurvival() {
		return false;
	}
}
