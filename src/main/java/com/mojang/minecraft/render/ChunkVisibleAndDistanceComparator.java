package com.mojang.minecraft.render;

import com.mojang.minecraft.player.Player;

import java.io.Serializable;
import java.util.Comparator;

public final class ChunkVisibleAndDistanceComparator implements Comparator<com.mojang.minecraft.render.Chunk>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Player player;

	public ChunkVisibleAndDistanceComparator(Player player) {
		this.player = player;
	}

	@Override
	public int compare(Chunk chunk, Chunk other) {
		if (chunk.visible || !other.visible) {
			if (other.visible) {
				float sqDist = chunk.distanceSquared(this.player);
				float otherSqDist = other.distanceSquared(this.player);

				if(sqDist == otherSqDist) {
					return 0;
				} else if (sqDist > otherSqDist) {
					return -1;
				} else {
					return 1;
				}
			} else {
				return 1;
			}
		} else {
			return -1;
		}
	}
	
}