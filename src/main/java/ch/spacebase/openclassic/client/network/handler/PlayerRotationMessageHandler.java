package ch.spacebase.openclassic.client.network.handler;

import ch.spacebase.openclassic.api.network.msg.PlayerRotationMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

import com.mojang.minecraft.entity.player.net.NetworkPlayer;

public class PlayerRotationMessageHandler extends MessageHandler<PlayerRotationMessage> {

	@Override
	public void handle(ClassicSession session, Player player, PlayerRotationMessage message) {
		if(message.getPlayerId() >= 0) {
			NetworkPlayer moving = GeneralUtils.getMinecraft().netPlayers.get(message.getPlayerId());
			if(moving != null) {
				moving.queue(message.getYaw(), message.getPitch());
			}
		}
	}

}
