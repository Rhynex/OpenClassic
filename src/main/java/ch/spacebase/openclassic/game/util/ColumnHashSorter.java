package ch.spacebase.openclassic.game.util;

import java.util.Comparator;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.util.storage.LongHash;
import ch.spacebase.openclassic.client.player.ClientPlayer;

public class ColumnHashSorter implements Comparator<Long> {

	@Override
	public int compare(Long o1, Long o2) {
		ClientPlayer player = (ClientPlayer) OpenClassic.getClient().getPlayer();
		double dist1 = this.distanceSquared(LongHash.getFirst(o1), LongHash.getSecond(o1), player.getPosition().getBlockX() >> 4, player.getPosition().getBlockZ() >> 4);
		double dist2 = this.distanceSquared(LongHash.getFirst(o2), LongHash.getSecond(o2), player.getPosition().getBlockX() >> 4, player.getPosition().getBlockZ() >> 4);
		if(dist1 > dist2) {
			return 1;
		} else if(dist1 < dist2) {
			return -1;
		} else {
			return 0;
		}
	}
	
    private double distanceSquared(float x1, float z1, float x2, float z2) {
        return Math.pow(x1 - x2, 2) + Math.pow(z1 - z2, 2);
    }

}
