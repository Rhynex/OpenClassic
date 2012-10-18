package ch.spacebase.openclassic.client.util;

import java.util.Comparator;


import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.level.column.ClassicChunk;

public class ChunkSorter implements Comparator<ClassicChunk> {

	@Override
	public int compare(ClassicChunk o1, ClassicChunk o2) {
		ClientPlayer player = (ClientPlayer) OpenClassic.getClient().getPlayer();
		double dist1 = this.distanceSquared(o1.getX(), o1.getY(), o1.getZ(), player.getPosition().getBlockX() >> 4, player.getPosition().getBlockY() >> 4, player.getPosition().getBlockZ() >> 4);
		double dist2 = this.distanceSquared(o2.getX(), o2.getY(), o2.getZ(), player.getPosition().getBlockX() >> 4, player.getPosition().getBlockY() >> 4, player.getPosition().getBlockZ() >> 4);
		if(dist1 > dist2) {
			return -1;
		} else if(dist1 < dist2) {
			return 1;
		} else {
			return 0;
		}
	}
	
    private double distanceSquared(float x1, float y1, float z1, float x2, float y2, float z2) {
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2);
    }

}
