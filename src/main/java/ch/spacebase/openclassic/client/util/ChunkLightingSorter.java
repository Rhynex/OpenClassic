package ch.spacebase.openclassic.client.util;

import java.util.Comparator;


import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.level.column.ClassicChunk;

public class ChunkLightingSorter implements Comparator<ClassicChunk> {

	@Override
	public int compare(ClassicChunk o1, ClassicChunk o2) {
		ClientPlayer player = (ClientPlayer) OpenClassic.getClient().getPlayer();
		if(player == null) return 0;
		double dist1 = this.distanceSquared(o1.getX(), o1.getZ(), player.getPosition().getBlockX() >> 4, player.getPosition().getBlockZ() >> 4);
		double dist2 = this.distanceSquared(o2.getX(), o2.getZ(), player.getPosition().getBlockX() >> 4, player.getPosition().getBlockZ() >> 4);
		if(dist1 > dist2) {
			return 1;
		} else if(dist1 < dist2) {
			return -1;
		} else {
			if(o1.getY() > o2.getY()) {
				return 1;
			} else if(o1.getY() < o2.getY()) {
				return -1;
			} else {
				return 0;
			}
		}
	}
	
    private double distanceSquared(float x1, float z1, float x2, float z2) {
        return Math.pow(x1 - x2, 2) + Math.pow(z1 - z2, 2);
    }

}
