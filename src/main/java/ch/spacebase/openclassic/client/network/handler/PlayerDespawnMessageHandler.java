package ch.spacebase.openclassic.client.network.handler;

import com.mojang.minecraft.player.net.NetworkPlayer;

import ch.spacebase.openclassic.api.network.msg.PlayerDespawnMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

public class PlayerDespawnMessageHandler extends MessageHandler<PlayerDespawnMessage> {

	@Override
	public void handle(ClassicSession session, Player player, PlayerDespawnMessage message) {
		NetworkPlayer despawning = GeneralUtils.getMinecraft().netPlayers.remove(message.getPlayerId());
		if (message.getPlayerId() >= 0 && despawning != null) {
			despawning.clear();
			GeneralUtils.getMinecraft().level.removeEntity(despawning);
		}
	}

}
