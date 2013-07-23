package com.mojang.minecraft.render;

import java.util.Comparator;

import com.mojang.minecraft.entity.player.LocalPlayer;

public final class ChunkDistanceComparator implements Comparator<Chunk> {

	private LocalPlayer player;

	public ChunkDistanceComparator(LocalPlayer player) {
		this.player = player;
	}

	@Override
	public int compare(Chunk chunk, Chunk other) {
		float sqDist = chunk.distanceSquared(this.player);
		float otherSqDist = other.distanceSquared(this.player);

		if(sqDist == otherSqDist) {
			return 0;
		} else if(sqDist > otherSqDist) {
			return -1;
		} else {
			return 1;
		}
	}
}