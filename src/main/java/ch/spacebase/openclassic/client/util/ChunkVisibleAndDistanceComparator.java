package ch.spacebase.openclassic.client.util;

import java.util.Comparator;

import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.render.level.Chunk;

public class ChunkVisibleAndDistanceComparator implements Comparator<Chunk> {

	private Player player;

	public ChunkVisibleAndDistanceComparator(Player player) {
		this.player = player;
	}

	@Override
	public int compare(Chunk chunk, Chunk other) {
		if(chunk.visible || !other.visible) {
			if(other.visible) {
				float sqDist = chunk.distanceSquared(this.player);
				float otherSqDist = other.distanceSquared(this.player);

				if(sqDist == otherSqDist) {
					return 0;
				} else if(sqDist > otherSqDist) {
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