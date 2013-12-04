package com.mojang.minecraft.gamemode;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.event.block.BlockBreakEvent;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.util.BlockUtils;
import ch.spacebase.openclassic.game.network.msg.PlayerSetBlockMessage;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.entity.player.LocalPlayer;
import com.zachsthings.onevent.EventManager;

public class GameMode {

	protected final Minecraft mc;
	public boolean creative = false;

	public GameMode(Minecraft mc) {
		this.mc = mc;
	}

	public void apply(ClientLevel level) {
	}

	public void openInventory() {
	}

	public void hitBlock(int x, int y, int z) {
		this.breakBlock(x, y, z);
	}

	public boolean canPlace(int block) {
		return true;
	}

	public void breakBlock(int x, int y, int z) {
		Block block = this.mc.level.getBlockAt(x, y, z);
		if(block == null) return;
		if(!this.mc.isInMultiplayer() && EventManager.callEvent(new BlockBreakEvent(block, OpenClassic.getClient().getPlayer(), this.mc.heldBlock.getBlock())).isCancelled()) {
			return;
		}

		BlockType old = block.getType();
		boolean success = this.mc.level.setBlockAt(x, y, z, VanillaBlock.AIR);
		if(old != null && success) {
			if(this.mc.isInMultiplayer()) {
				this.mc.session.send(new PlayerSetBlockMessage((short) x, (short) y, (short) z, false, (byte) this.mc.player.inventory.getSelected()));
			} else if(old.getPhysics() != null) {
				old.getPhysics().onBreak(block);
			}

			if(old.getStepSound() != StepSound.NONE) {
				if(old.getStepSound() == StepSound.SAND) {
					this.mc.audio.playSound(StepSound.GRAVEL.getSound(), x, y, z, (StepSound.GRAVEL.getVolume() + 1.0F) / 2.0F, StepSound.GRAVEL.getPitch() * 0.8F);
				} else {
					this.mc.audio.playSound(old.getStepSound().getSound(), x, y, z, (old.getStepSound().getVolume() + 1.0F) / 2.0F, old.getStepSound().getPitch() * 0.8F);
				}
			}

			BlockUtils.spawnDestructionParticles(old, this.mc.level, x, y, z);
		}
	}

	public void hitBlock(int x, int y, int z, int side) {
	}

	public void resetHits() {
	}

	public void applyBlockCracks(float time) {
	}

	public float getReachDistance() {
		return 5;
	}

	public boolean useItem(LocalPlayer player, int type) {
		return false;
	}

	public void preparePlayer(LocalPlayer player) {
	}

	public void spawnMobs() {
	}

	public void prepareLevel(ClientLevel level) {
	}

	public boolean isSurvival() {
		return true;
	}

	public void apply(LocalPlayer player) {
	}
	
}
